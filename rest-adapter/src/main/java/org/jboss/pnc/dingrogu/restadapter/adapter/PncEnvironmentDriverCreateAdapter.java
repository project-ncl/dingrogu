package org.jboss.pnc.dingrogu.restadapter.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.environmentdriver.dto.EnvironmentCreateRequest;
import org.jboss.pnc.api.environmentdriver.dto.EnvironmentCreateResponse;
import org.jboss.pnc.api.environmentdriver.dto.EnvironmentCreateResult;
import org.jboss.pnc.api.repositorydriver.dto.RepositoryCreateResponse;
import org.jboss.pnc.dingrogu.api.client.PncEnvironmentDriver;
import org.jboss.pnc.dingrogu.api.client.PncEnvironmentDriverProducer;
import org.jboss.pnc.dingrogu.api.dto.adapter.PncEnvironmentDriverCreateDTO;
import org.jboss.pnc.dingrogu.api.endpoint.AdapterEndpoint;
import org.jboss.pnc.dingrogu.api.endpoint.WorkflowEndpoint;
import org.jboss.pnc.dingrogu.common.TaskHelper;
import org.jboss.pnc.rex.api.CallbackEndpoint;
import org.jboss.pnc.rex.api.TaskEndpoint;
import org.jboss.pnc.rex.dto.ServerResponseDTO;
import org.jboss.pnc.rex.dto.TaskDTO;
import org.jboss.pnc.rex.model.requests.StartRequest;
import org.jboss.pnc.rex.model.requests.StopRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;

public class PncEnvironmentDriverCreateAdapter implements Adapter<PncEnvironmentDriverCreateDTO> {

    @ConfigProperty(name = "dingrogu.url")
    String dingroguUrl;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CallbackEndpoint callbackEndpoint;

    @Inject
    PncEnvironmentDriverProducer pncEnvironmentDriverProducer;

    @Inject
    PncRepositoryDriverSetupAdapter pncRepositoryDriverSetupAdapter;

    @Inject
    TaskEndpoint taskEndpoint;

    @Override
    public String getAdapterName() {
        return "pnc-environment-driver-create";
    }

    @Override
    public Optional<Object> start(String correlationId, StartRequest startRequest) {

        Request callback;
        try {
            callback = new Request(
                    Request.Method.POST,
                    new URI(AdapterEndpoint.getCallbackAdapterEndpoint(dingroguUrl, getAdapterName(), correlationId)),
                    TaskHelper.getHTTPHeaders(),
                    null);
        } catch (URISyntaxException e) {
            Log.error(e);
            throw new RuntimeException(e);
        }
        PncEnvironmentDriverCreateDTO dto = objectMapper.convertValue(startRequest.getPayload(),
                PncEnvironmentDriverCreateDTO.class);

        Map<String, Object> pastResults = startRequest.getTaskResults();
        Object repoDriverSetup = pastResults.get(pncRepositoryDriverSetupAdapter.getRexTaskName(correlationId));
        RepositoryCreateResponse repositoryResponse = objectMapper
                .convertValue(repoDriverSetup, RepositoryCreateResponse.class);

        PncEnvironmentDriver pncEnvironmentDriver = pncEnvironmentDriverProducer
                .getPncEnvironmentDriver(dto.getPncEnvironmentDriverUrl());

        EnvironmentCreateRequest environmentCreateRequest = EnvironmentCreateRequest.builder()
                .environmentLabel(dto.getEnvironmentLabel())
                .imageId(dto.getEnvironmentImage())
                .repositoryDependencyUrl(repositoryResponse.getRepositoryDependencyUrl())
                .repositoryDeployUrl(repositoryResponse.getRepositoryDeployUrl())
                .repositoryBuildContentId(dto.getBuildContentId())
                .podMemoryOverride(dto.getPodMemoryOverride())
                .allowSshDebug(dto.isDebugEnabled())
                .buildConfigId(dto.getBuildConfigId())
                .sidecarEnabled(repositoryResponse.isSidecarEnabled())
                .sidecarArchiveEnabled(repositoryResponse.isSidecarArchiveEnabled())
                .completionCallback(callback)
                .build();
        Log.infof("PNC environment create request: %s", environmentCreateRequest);

        EnvironmentCreateResponse response = pncEnvironmentDriver.build(environmentCreateRequest).toCompletableFuture().join();
        Log.infof("PNC initial response: %s", response);
        return Optional.ofNullable(response);
    }

    @Override
    public void callback(String correlationId, Object object) {
        try {
            EnvironmentCreateResult response = objectMapper.convertValue(object, EnvironmentCreateResult.class);
            Log.infof("PNC response: %s", response);
            try {
                callbackEndpoint.succeed(getRexTaskName(correlationId), response, null);
            } catch (Exception e) {
                Log.error("Error happened in callback adapter", e);
            }
        } catch (IllegalArgumentException e) {
            try {
                callbackEndpoint.fail(getRexTaskName(correlationId), object, null);
            } catch (Exception ex) {
                Log.error("Error happened in callback adapter", ex);
            }
        }
    }

    @Override
    public String getNotificationEndpoint(String adapterUrl) {
        return adapterUrl + WorkflowEndpoint.PNC_BUILD_REX_NOTIFY;
    }

    @Override
    public void cancel(String correlationId, StopRequest stopRequest) {

        // get own unique id created by pnc-environment-driver-create sent back to rex in the start method
        TaskDTO ownTask = taskEndpoint.getSpecific(getRexTaskName(correlationId));
        List<ServerResponseDTO> serverResponses = ownTask.getServerResponses();

        if (serverResponses.isEmpty()) {
            throw new RuntimeException("We didn't get any server response from " + getAdapterName() + ": " + correlationId);
        }

        ServerResponseDTO last = serverResponses.get(serverResponses.size() - 1);
        EnvironmentCreateResponse pncResponse = objectMapper.convertValue(last.getBody(), EnvironmentCreateResponse.class);

        PncEnvironmentDriverCreateDTO dto = objectMapper.convertValue(stopRequest.getPayload(),
                PncEnvironmentDriverCreateDTO.class);
        PncEnvironmentDriver pncEnvironmentDriver = pncEnvironmentDriverProducer
                .getPncEnvironmentDriver(dto.getPncEnvironmentDriverUrl());

        pncEnvironmentDriver.cancel(pncResponse.getEnvironmentId());
    }
}

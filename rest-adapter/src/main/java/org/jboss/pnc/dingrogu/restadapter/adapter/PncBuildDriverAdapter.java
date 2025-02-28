package org.jboss.pnc.dingrogu.restadapter.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.api.builddriver.dto.BuildCancelRequest;
import org.jboss.pnc.api.builddriver.dto.BuildCompleted;
import org.jboss.pnc.api.builddriver.dto.BuildRequest;
import org.jboss.pnc.api.builddriver.dto.BuildResponse;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.environmentdriver.dto.EnvironmentCreateResult;
import org.jboss.pnc.api.reqour.dto.AdjustResponse;
import org.jboss.pnc.dingrogu.api.client.PncBuildDriver;
import org.jboss.pnc.dingrogu.api.client.PncBuildDriverProducer;
import org.jboss.pnc.dingrogu.api.dto.adapter.PncBuildDriverDTO;
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

@ApplicationScoped
public class PncBuildDriverAdapter implements Adapter<PncBuildDriverDTO> {

    @ConfigProperty(name = "dingrogu.url")
    String dingroguUrl;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CallbackEndpoint callbackEndpoint;

    @Inject
    PncBuildDriverProducer pncBuildDriverProducer;

    @Inject
    PncReqourAdjustAdapter pncReqourAdjustAdapter;

    @Inject
    PncEnvironmentDriverCreateAdapter pncEnvironmentDriverCreateAdapter;

    @Inject
    TaskEndpoint taskEndpoint;

    @Override
    public String getAdapterName() {
        return "pnc-build-driver";
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
        PncBuildDriverDTO dto = objectMapper.convertValue(startRequest.getPayload(), PncBuildDriverDTO.class);

        Map<String, Object> pastResults = startRequest.getTaskResults();
        Object envDriverCreate = pastResults.get(pncEnvironmentDriverCreateAdapter.getRexTaskName(correlationId));
        EnvironmentCreateResult environmentCreateResponse = objectMapper
                .convertValue(envDriverCreate, EnvironmentCreateResult.class);

        Object reqourAdjust = pastResults.get(pncReqourAdjustAdapter.getRexTaskName(correlationId));
        AdjustResponse adjustResponse = objectMapper.convertValue(reqourAdjust, AdjustResponse.class);

        PncBuildDriver pncBuildDriver = pncBuildDriverProducer
                .getPncBuildDriver(dto.getPncBuildDriverUrl());

        BuildRequest buildRequest = BuildRequest.builder()
                .scmUrl(adjustResponse.getInternalUrl().getReadonlyUrl())
                .scmRevision(adjustResponse.getDownstreamCommit())
                .scmTag(adjustResponse.getTag())
                .command(dto.getBuildCommand())
                .workingDirectory(environmentCreateResponse.getWorkingDirectory())
                .environmentBaseUrl(environmentCreateResponse.getEnvironmentBaseUri().toString())
                .debugEnabled(dto.isDebugEnabled())
                .completionCallback(callback)
                .heartbeatConfig(null) // TODO
                .build();
        Log.infof("PNC build request: %s", buildRequest);

        BuildResponse response = pncBuildDriver.build(buildRequest).toCompletableFuture().join();
        Log.infof("PNC initial response: %s", response);
        return Optional.ofNullable(response);
    }

    @Override
    public void callback(String correlationId, Object object) {
        try {
            BuildCompleted response = objectMapper.convertValue(object, BuildCompleted.class);
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
        return adapterUrl + WorkflowEndpoint.KONFLUX_BUILD_REX_NOTIFY;
    }

    @Override
    public void cancel(String correlationId, StopRequest stopRequest) {

        // get own unique id created by pnc-build-driver sent back to rex in the start method
        TaskDTO ownTask = taskEndpoint.getSpecific(getRexTaskName(correlationId));
        List<ServerResponseDTO> serverResponses = ownTask.getServerResponses();

        if (serverResponses.isEmpty()) {
            throw new RuntimeException("We didn't get any server response from " + getAdapterName() + ": " + correlationId);
        }

        ServerResponseDTO last = serverResponses.get(serverResponses.size() - 1);
        BuildResponse pncResponse = objectMapper.convertValue(last.getBody(), BuildResponse.class);

        PncBuildDriverDTO dto = objectMapper.convertValue(stopRequest.getPayload(), PncBuildDriverDTO.class);
        PncBuildDriver pncBuildDriver = pncBuildDriverProducer
                .getPncBuildDriver(dto.getPncBuildDriverUrl());

        Map<String, Object> pastResults = stopRequest.getTaskResults();
        Object envDriverCreate = pastResults.get(pncEnvironmentDriverCreateAdapter.getRexTaskName(correlationId));
        EnvironmentCreateResult environmentCreateResponse = objectMapper
                .convertValue(envDriverCreate, EnvironmentCreateResult.class);

        BuildCancelRequest buildCancelRequest = BuildCancelRequest.builder()
                .buildEnvironmentBaseUrl(environmentCreateResponse.getEnvironmentBaseUri().toString())
                .buildExecutionId(pncResponse.getBuildExecutionId())
                .build();
        pncBuildDriver.cancel(buildCancelRequest);
    }

    @Override
    public boolean shouldGetResultsFromDependencies() {
        return true;
    }
}

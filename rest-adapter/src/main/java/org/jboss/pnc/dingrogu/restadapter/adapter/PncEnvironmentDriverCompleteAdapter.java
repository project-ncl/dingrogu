package org.jboss.pnc.dingrogu.restadapter.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.environmentdriver.dto.EnvironmentCompleteRequest;
import org.jboss.pnc.api.environmentdriver.dto.EnvironmentCompleteResponse;
import org.jboss.pnc.api.environmentdriver.dto.EnvironmentCreateResponse;
import org.jboss.pnc.dingrogu.api.client.PncEnvironmentDriver;
import org.jboss.pnc.dingrogu.api.client.PncEnvironmentDriverProducer;
import org.jboss.pnc.dingrogu.api.dto.adapter.PncEnvironmentDriverCompleteDTO;
import org.jboss.pnc.dingrogu.api.endpoint.AdapterEndpoint;
import org.jboss.pnc.dingrogu.api.endpoint.WorkflowEndpoint;
import org.jboss.pnc.dingrogu.common.TaskHelper;
import org.jboss.pnc.rex.api.TaskEndpoint;
import org.jboss.pnc.rex.dto.ServerResponseDTO;
import org.jboss.pnc.rex.dto.TaskDTO;
import org.jboss.pnc.rex.model.requests.StartRequest;
import org.jboss.pnc.rex.model.requests.StopRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;

public class PncEnvironmentDriverCompleteAdapter implements Adapter<PncEnvironmentDriverCompleteDTO> {

    @ConfigProperty(name = "dingrogu.url")
    String dingroguUrl;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    PncEnvironmentDriverProducer pncEnvironmentDriverProducer;

    @Inject
    PncEnvironmentDriverCreateAdapter pncEnvironmentDriverCreateAdapter;

    @Inject
    TaskEndpoint taskEndpoint;

    @Override
    public String getAdapterName() {
        return "pnc-environment-driver-complete";
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
        PncEnvironmentDriverCompleteDTO dto = objectMapper.convertValue(startRequest.getPayload(),
                PncEnvironmentDriverCompleteDTO.class);

        // get unique id created by pnc-environment-driver-create sent back to rex in the start method
        TaskDTO envDriverCreateTask = taskEndpoint.getSpecific(pncEnvironmentDriverCreateAdapter.getRexTaskName(correlationId));
        List<ServerResponseDTO> serverResponses = envDriverCreateTask.getServerResponses();

        if (serverResponses.isEmpty()) {
            throw new RuntimeException("We didn't get any server response from "
                    + pncEnvironmentDriverCreateAdapter.getAdapterName() + ": " + correlationId);
        }

        ServerResponseDTO last = serverResponses.get(serverResponses.size() - 1);
        EnvironmentCreateResponse pncResponse = objectMapper.convertValue(last.getBody(), EnvironmentCreateResponse.class);

        PncEnvironmentDriver pncEnvironmentDriver = pncEnvironmentDriverProducer
                .getPncEnvironmentDriver(dto.getPncEnvironmentDriverUrl());

        EnvironmentCompleteRequest environmentCompleteRequest = EnvironmentCompleteRequest.builder()
                .environmentId(pncResponse.getEnvironmentId())
                .enableDebug(dto.isDebugEnabled())
                .build();
        Log.infof("PNC environment complete request: %s", environmentCompleteRequest);

        EnvironmentCompleteResponse response = pncEnvironmentDriver.complete(environmentCompleteRequest).toCompletableFuture()
                .join();
        Log.infof("PNC initial response: %s", response);
        return Optional.ofNullable(response);
    }

    @Override
    public void callback(String correlationId, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNotificationEndpoint(String adapterUrl) {
        return adapterUrl + WorkflowEndpoint.PNC_BUILD_REX_NOTIFY;
    }

    @Override
    public void cancel(String correlationId, StopRequest stopRequest) {
        throw new UnsupportedOperationException();
    }
}

package org.jboss.pnc.dingrogu.restadapter.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.pnc.dingrogu.api.dto.adapter.OrchRepositoryCreationResultDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.RepourCreateRepoResponse;
import org.jboss.pnc.dingrogu.api.endpoint.WorkflowEndpoint;
import org.jboss.pnc.dingrogu.restadapter.client.OrchClient;
import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.rex.api.CallbackEndpoint;
import org.jboss.pnc.rex.model.requests.StartRequest;
import org.jboss.pnc.rex.model.requests.StopRequest;

import java.util.Map;
import java.util.Optional;

// TODO: replace this with rex notification
@ApplicationScoped
public class OrchRepositoryCreationResultAdapter implements Adapter<OrchRepositoryCreationResultDTO> {

    @ConfigProperty(name = "dingrogu.url")
    String dingroguUrl;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    CallbackEndpoint callbackEndpoint;

    @Inject
    OrchClient orchClient;

    @Inject
    ReqourCreateRepositoryAdapter repourCreate;

    @Override
    public String getAdapterName() {
        return "orch-repository-creation-result";
    }

    @Override
    public Optional<Object> start(String correlationId, StartRequest startRequest) {

        // grab payload DTO
        OrchRepositoryCreationResultDTO dto = objectMapper
                .convertValue(startRequest.getPayload(), OrchRepositoryCreationResultDTO.class);

        Map<String, Object> pastResults = startRequest.getTaskResults();
        Object pastResult = pastResults.get(repourCreate.getRexTaskName(correlationId));
        RepourCreateRepoResponse repourCreateResponse = objectMapper
                .convertValue(pastResult, RepourCreateRepoResponse.class);

        // generate result for Orch
        // TODO: adjust the status
        RepositoryCreationResult result = RepositoryCreationResult.builder()
                .status(ResultStatus.SUCCESS)
                .repoCreatedSuccessfully(true)
                .internalScmUrl(repourCreateResponse.getReadwriteUrl())
                .externalUrl(dto.getExternalUrl())
                .preBuildSyncEnabled(dto.isPreBuildSyncEnabled())
                .taskId(dto.getTaskId())
                .jobType(dto.getJobType())
                .buildConfiguration(dto.getBuildConfiguration())
                .build();
        orchClient.submitRepourRepositoryCreationResult(dto.getOrchUrl(), result);

        managedExecutor.submit(() -> {
            try {
                // sleep for 5 seconds to make sure that Rex has processed the successful start
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            try {
                callbackEndpoint.succeed(getRexTaskName(correlationId), null, null);
            } catch (Exception e) {
                Log.error("Error happened in rex client callback to Rex server for orch repository create", e);
            }
        });

        return Optional.empty();
    }

    @Override
    public void callback(String correlationId, Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * There's nothing to cancel, the request is synchronous
     * 
     * @param correlationId
     * @param stopRequest
     */
    @Override
    public void cancel(String correlationId, StopRequest stopRequest) {
        return;
    }

    @Override
    public String getNotificationEndpoint(String adapterUrl) {
        return adapterUrl + WorkflowEndpoint.REPOSITORY_CREATION_REX_NOTIFY;
    }

    /**
     * We read past results to build final request
     * 
     * @return true
     */
    @Override
    public boolean shouldGetResultsFromDependencies() {
        return true;
    }
}

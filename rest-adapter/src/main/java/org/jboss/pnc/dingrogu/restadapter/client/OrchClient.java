package org.jboss.pnc.dingrogu.restadapter.client;

import java.net.URI;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.pnc.api.causeway.dto.push.BuildPushCompleted;
import org.jboss.pnc.api.deliverablesanalyzer.dto.AnalysisResult;
import org.jboss.pnc.api.dto.OperationOutcome;
import org.jboss.pnc.dingrogu.common.TaskHelper;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.quarkus.client.auth.runtime.PNCClientAuth;

import io.quarkus.logging.Log;
import kong.unirest.core.ContentType;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;

@ApplicationScoped
public class OrchClient {

    @Inject
    PNCClientAuth pncClientAuth;

    @Retry
    public void submitBuildPushResult(String orchUrl, String buildId, BuildPushCompleted result) {
        String orchUrlWithoutPath = URI.create(orchUrl).resolve("/").toString();

        Log.infof("BuildPushResult for buildid: %s is: %s", buildId, result);
        HttpResponse<JsonNode> response = Unirest
                .post(orchUrlWithoutPath + "pnc-rest/v2/builds/" + buildId + "/brew-push/complete")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(pncClientAuth))
                .body(result)
                .asJson();

        if (!response.isSuccess()) {
            TaskHelper.LIVE_LOG
                    .error("Request didn't go through: HTTP {}, body: {}", response.getStatus(), response.getBody());
            throw new RuntimeException("Request didn't go through");
        }
    }

    @Retry
    public void submitDelAResult(String orchUrl, AnalysisResult result) {
        String orchUrlWithoutPath = URI.create(orchUrl).resolve("/").toString();

        Log.infof("Submit dela request: %s", result);
        HttpResponse<JsonNode> response = Unirest.post(orchUrlWithoutPath + "pnc-rest/v2/deliverable-analyses/complete")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(pncClientAuth))
                .body(result)
                .asJson();

        if (!response.isSuccess()) {
            TaskHelper.LIVE_LOG
                    .error("Request didn't go through: HTTP {}, body: {}", response.getStatus(), response.getBody());
            throw new RuntimeException("Request didn't go through");
        }
    }

    @Retry
    public Optional<Build> getBuildRecord(String orchUrl, String buildId) {
        String orchUrlWithoutPath = URI.create(orchUrl).resolve("/").toString();

        HttpResponse<Build> response = Unirest.get(orchUrlWithoutPath + "pnc-rest/v2/builds/" + buildId)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(pncClientAuth))
                .asObject(Build.class);

        return switch (response.getStatus()) {
            case 200 -> Optional.of(response.getBody());

            // some error cases do not produce all tags, therefore 204/404 should not throw exception
            case 204, 404 -> {
                Log.warnf(
                        "Orchestrator build %s missing: HTTP %d, body: %s",
                        buildId,
                        response.getStatus(),
                        response.getBody() == null ? response.getStatusText() : response.getBody().toString());
                yield Optional.empty();
            }
            default -> {
                Log.warnf(
                        "Request didn't go through: HTTP %d, body: %s",
                        response.getStatus(),
                        response.getBody() == null ? response.getStatusText() : response.getBody().toString());
                throw new RuntimeException("Request didn't go through");
            }
        };

    }

    @Retry
    public void completeRepositoryCreation(String orchUrl, RepositoryCreationResult result) {

        String orchUrlWithoutPath = URI.create(orchUrl).resolve("/").toString();

        Log.info("Sending Reqour repository to server: " + orchUrlWithoutPath);

        HttpResponse<JsonNode> response = Unirest
                .post(orchUrlWithoutPath + "pnc-rest/v2/bpm/repository-creation/completed")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(pncClientAuth))
                .body(result)
                .asJson();

        if (!response.isSuccess()) {
            TaskHelper.LIVE_LOG
                    .error("Request didn't go through: HTTP {}, body: {}", response.getStatus(), response.getBody());
            throw new RuntimeException("Request didn't go through");
        }
    }

    public void completeOperation(String orchUrl, OperationOutcome operationOutcome, String operationId) {
        String orchUrlWithoutPath = URI.create(orchUrl).resolve("/").toString();

        HttpResponse<JsonNode> response = Unirest
                .post(orchUrlWithoutPath + "pnc-rest/v2/operations/" + operationId + "/complete")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(pncClientAuth))
                .body(operationOutcome)
                .asJson();

        if (!response.isSuccess()) {
            TaskHelper.LIVE_LOG
                    .error("Request didn't go through: HTTP {}, body: {}", response.getStatus(), response.getBody());
            throw new RuntimeException("Request didn't go through");
        }
    }
}

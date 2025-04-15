package org.jboss.pnc.dingrogu.restadapter.client;

import kong.unirest.core.ContentType;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.pnc.api.repositorydriver.dto.RepositoryCreateRequest;
import org.jboss.pnc.api.repositorydriver.dto.RepositoryCreateResponse;
import org.jboss.pnc.api.repositorydriver.dto.RepositoryPromoteRequest;

import io.quarkus.logging.Log;
import io.quarkus.oidc.client.Tokens;

@ApplicationScoped
public class RepositoryDriverClient {

    @Inject
    Tokens tokens;

    @Retry
    public RepositoryCreateResponse setup(String repositoryDriverUrl, RepositoryCreateRequest request) {
        HttpResponse<RepositoryCreateResponse> response = Unirest.post(repositoryDriverUrl + "/create")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(tokens))
                .body(request)
                .asObject(RepositoryCreateResponse.class);

        if (!response.isSuccess()) {
            Log.errorf("Request didn't go through: HTTP %s, body: %s", response.getStatus(), response.getBody());
            throw new RuntimeException("Request didn't go through");
        }

        return response.getBody();
    }

    @Retry
    public void seal(String repositoryDriverUrl, String buildContentId) {
        HttpResponse<JsonNode> response = Unirest.put(repositoryDriverUrl + "/seal")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(tokens))
                .body(buildContentId)
                .asJson();

        if (!response.isSuccess()) {
            Log.errorf("Request didn't go through: HTTP %s, body: %s", response.getStatus(), response.getBody());
            throw new RuntimeException("Request didn't go through");
        }
    }

    @Retry
    public void promote(String repositoryDriverUrl, RepositoryPromoteRequest request) {
        HttpResponse<JsonNode> response = Unirest.put(repositoryDriverUrl + "/promote")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(tokens))
                .body(request)
                .asJson();

        if (!response.isSuccess()) {
            Log.errorf("Request didn't go through: HTTP %s, body: %s", response.getStatus(), response.getBody());
            throw new RuntimeException("Request didn't go through");
        }
    }
}

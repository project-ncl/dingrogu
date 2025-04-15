package org.jboss.pnc.dingrogu.restadapter.client;

import kong.unirest.core.ContentType;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.pnc.dingrogu.api.dto.dummy.DummyServiceRequestDTO;

import io.quarkus.logging.Log;
import io.quarkus.oidc.client.Tokens;

@ApplicationScoped
public class DummyClient {

    @Inject
    Tokens tokens;

    @Retry
    public void start(String dummyUrl, String callbackUrl) {
        Log.info("Sending dummy request to server: " + dummyUrl);

        DummyServiceRequestDTO request = DummyServiceRequestDTO.builder().callbackUrl(callbackUrl).build();
        HttpResponse<JsonNode> response = Unirest.post(dummyUrl)
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

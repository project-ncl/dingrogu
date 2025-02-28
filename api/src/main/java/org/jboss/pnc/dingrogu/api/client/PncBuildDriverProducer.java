package org.jboss.pnc.dingrogu.api.client;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

@ApplicationScoped
public class PncBuildDriverProducer {

    @Inject
    AuthorizationClientHttpFactory authorizationClientHttpFactory;

    public PncBuildDriver getPncBuildDriver(final String pncBuildDriverUrl) {
        return QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(pncBuildDriverUrl))
                .clientHeadersFactory(authorizationClientHttpFactory)
                .build(PncBuildDriver.class);
    }
}

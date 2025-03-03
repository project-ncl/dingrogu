package org.jboss.pnc.dingrogu.api.client;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

@ApplicationScoped
public class PncEnvironmentDriverProducer {

    @Inject
    AuthorizationClientHttpFactory authorizationClientHttpFactory;

    public PncEnvironmentDriver getPncEnvironmentDriver(final String pncEnvironmentDriverUrl) {
        return QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(pncEnvironmentDriverUrl))
                .clientHeadersFactory(authorizationClientHttpFactory)
                .build(PncEnvironmentDriver.class);
    }
}

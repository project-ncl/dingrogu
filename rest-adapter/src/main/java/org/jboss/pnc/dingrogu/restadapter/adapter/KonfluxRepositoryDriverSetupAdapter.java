package org.jboss.pnc.dingrogu.restadapter.adapter;

import static org.jboss.pnc.dingrogu.restadapter.adapter.KonfluxDiscriminator.KONFLUX_PREFIX;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.pnc.dingrogu.api.endpoint.WorkflowEndpoint;

@KonfluxDiscriminator
@ApplicationScoped
public class KonfluxRepositoryDriverSetupAdapter extends RepositoryDriverSetupAdapter {

    @Inject
    public KonfluxRepositoryDriverSetupAdapter(@KonfluxDiscriminator ReqourAdjustAdapter reqourAdjustAdapter) {
        super(reqourAdjustAdapter);
    }

    @Override
    public String getAdapterName() {
        return KONFLUX_PREFIX + super.getAdapterName();
    }

    @Override
    public String getNotificationEndpoint(String adapterUrl) {
        return adapterUrl + WorkflowEndpoint.KONFLUX_BUILD_REX_NOTIFY;
    }
}

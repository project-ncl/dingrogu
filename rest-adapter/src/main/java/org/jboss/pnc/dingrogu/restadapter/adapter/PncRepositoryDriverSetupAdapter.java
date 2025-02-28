package org.jboss.pnc.dingrogu.restadapter.adapter;

import static org.jboss.pnc.dingrogu.restadapter.adapter.PncDiscriminator.PNC_PREFIX;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.pnc.dingrogu.api.endpoint.WorkflowEndpoint;

@PncDiscriminator
@ApplicationScoped
public class PncRepositoryDriverSetupAdapter extends RepositoryDriverSetupAdapter {

    @Inject
    public PncRepositoryDriverSetupAdapter(@PncDiscriminator ReqourAdjustAdapter reqourAdjustAdapter) {
        super(reqourAdjustAdapter);
    }

    @Override
    public String getAdapterName() {
        return PNC_PREFIX + super.getAdapterName();
    }

    @Override
    public String getNotificationEndpoint(String adapterUrl) {
        return adapterUrl + WorkflowEndpoint.PNC_BUILD_REX_NOTIFY;
    }
}

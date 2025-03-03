package org.jboss.pnc.dingrogu.restworkflow.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.jboss.pnc.dingrogu.api.dto.CorrelationId;
import org.jboss.pnc.dingrogu.api.dto.workflow.BrewPushDTO;
import org.jboss.pnc.dingrogu.api.dto.workflow.BuildWorkDTO;
import org.jboss.pnc.dingrogu.api.dto.workflow.DeliverablesAnalysisWorkflowDTO;
import org.jboss.pnc.dingrogu.api.dto.workflow.DummyWorkflowDTO;
import org.jboss.pnc.dingrogu.api.dto.workflow.RepositoryCreationDTO;
import org.jboss.pnc.dingrogu.api.endpoint.WorkflowEndpoint;
import org.jboss.pnc.dingrogu.restworkflow.workflows.BrewPushWorkflow;
import org.jboss.pnc.dingrogu.restworkflow.workflows.DeliverablesAnalysisWorkflow;
import org.jboss.pnc.dingrogu.restworkflow.workflows.DummyWorkflow;
import org.jboss.pnc.dingrogu.restworkflow.workflows.KonfluxBuildWorkflow;
import org.jboss.pnc.dingrogu.restworkflow.workflows.PncBuildWorkflow;
import org.jboss.pnc.dingrogu.restworkflow.workflows.RepositoryCreationWorkflow;
import org.jboss.pnc.rex.model.requests.NotificationRequest;
import org.jboss.pnc.rex.model.requests.StartRequest;

/**
 * Implementation of the workflow endpoint
 *
 * Any new workflow will require the addition of a new endpoint here
 */
@ApplicationScoped
public class WorkflowEndpointImpl implements WorkflowEndpoint {

    @Inject
    BrewPushWorkflow brewPushWorkflow;

    @Inject
    RepositoryCreationWorkflow repositoryCreationWorkflow;

    @Inject
    KonfluxBuildWorkflow konfluxBuildWorkflow;

    @Inject
    PncBuildWorkflow pncBuildWorkflow;

    @Inject
    DeliverablesAnalysisWorkflow deliverablesAnalysisWorkflow;

    @Inject
    DummyWorkflow dummyWorkflow;

    @Override
    public CorrelationId startBrewPushWorkflow(BrewPushDTO brewPushDTO) {
        return brewPushWorkflow.submitWorkflow(brewPushDTO);
    }

    @Override
    public Response brewPushNotificationFromRex(NotificationRequest notificationRequest) {
        return brewPushWorkflow.rexNotification(notificationRequest);
    }

    @Override
    public Response repositoryCreationNotificationFromRex(NotificationRequest notificationRequest) {
        return brewPushWorkflow.rexNotification(notificationRequest);
    }

    @Override
    public CorrelationId startRepositoryCreationWorkflow(RepositoryCreationDTO repositoryCreationDTO) {
        return repositoryCreationWorkflow.submitWorkflow(repositoryCreationDTO);
    }

    @Override
    public CorrelationId startKonfluxBuildWorkflow(final BuildWorkDTO buildWorkDTO) {
        return konfluxBuildWorkflow.submitWorkflow(buildWorkDTO);
    }

    @Override
    public CorrelationId startKonfluxBuildWorkflowFromRex(final StartRequest startRequest) {
        return konfluxBuildWorkflow.submitWorkflow(startRequest);
    }

    @Override
    public Response buildKonfluxWorkflowNotificationFromRex(final NotificationRequest notificationRequest) {
        return konfluxBuildWorkflow.rexNotification(notificationRequest);
    }

    @Override
    public CorrelationId startPncBuildWorkflow(final BuildWorkDTO buildWorkDTO) {
        return pncBuildWorkflow.submitWorkflow(buildWorkDTO);
    }

    @Override
    public CorrelationId startPncBuildWorkflowFromRex(final StartRequest startRequest) {
        return pncBuildWorkflow.submitWorkflow(startRequest);
    }

    @Override
    public Response buildPncWorkflowNotificationFromRex(final NotificationRequest notificationRequest) {
        return pncBuildWorkflow.rexNotification(notificationRequest);
    }

    @Override
    public CorrelationId startDeliverablesAnalysisWorkflow(
            DeliverablesAnalysisWorkflowDTO deliverablesAnalysisWorkflowDTO) {
        return deliverablesAnalysisWorkflow.submitWorkflow(deliverablesAnalysisWorkflowDTO);
    }

    @Override
    public Response deliverablesAnalysisNotificationFromRex(NotificationRequest notificationRequest) {
        return deliverablesAnalysisWorkflow.rexNotification(notificationRequest);
    }

    @Override
    public CorrelationId startDummyWorkflow(DummyWorkflowDTO dummyWorkflowDTO) {
        return dummyWorkflow.submitWorkflow(dummyWorkflowDTO);
    }

    @Override
    public Response dummyNotificationFromRex(NotificationRequest notificationRequest) {
        return dummyWorkflow.rexNotification(notificationRequest);
    }

    @Override
    public Response cancelWorkflow(String correlationId) {
        // TODO: interact with Rex to submit cancel of the graph, and return an error for not found or rex not
        // responding
        throw new UnsupportedOperationException();
    }
}

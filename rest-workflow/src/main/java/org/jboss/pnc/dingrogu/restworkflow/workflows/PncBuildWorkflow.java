package org.jboss.pnc.dingrogu.restworkflow.workflows;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.pnc.common.log.MDCUtils;
import org.jboss.pnc.dingrogu.api.dto.CorrelationId;
import org.jboss.pnc.dingrogu.api.dto.workflow.BuildWorkDTO;
import org.jboss.pnc.dingrogu.restadapter.adapter.PncBuildDriverAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.PncDiscriminator;
import org.jboss.pnc.dingrogu.restadapter.adapter.PncEnvironmentDriverCompleteAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.PncEnvironmentDriverCreateAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.RepositoryDriverPromoteAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.RepositoryDriverSealAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.RepositoryDriverSetupAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.ReqourAdjustAdapter;
import org.jboss.pnc.rex.dto.ConfigurationDTO;
import org.jboss.pnc.rex.dto.CreateTaskDTO;
import org.jboss.pnc.rex.dto.EdgeDTO;
import org.jboss.pnc.rex.dto.requests.CreateGraphRequest;
import org.jboss.pnc.rex.model.requests.StartRequest;

/**
 * PNC build process workflow implementation
 */
@ApplicationScoped
public class PncBuildWorkflow extends BuildWorkflow {

    @Inject
    PncBuildDriverAdapter pncBuildDriverAdapter;

    @Inject
    PncEnvironmentDriverCreateAdapter environmentDriverCreateAdapter;

    @Inject
    PncEnvironmentDriverCompleteAdapter environmentDriverCompleteAdapter;

    public PncBuildWorkflow(@PncDiscriminator ReqourAdjustAdapter reqourAdjustAdapter,
                            @PncDiscriminator RepositoryDriverSetupAdapter repositoryDriverSetupAdapter,
                            @PncDiscriminator RepositoryDriverSealAdapter repositoryDriverSealAdapter,
                            @PncDiscriminator RepositoryDriverPromoteAdapter repositoryDriverPromoteAdapter) {
        super(reqourAdjustAdapter, repositoryDriverSetupAdapter, repositoryDriverSealAdapter, repositoryDriverPromoteAdapter);
    }

    @Override
    public CorrelationId submitWorkflow(BuildWorkDTO buildWorkDTO) throws WorkflowSubmissionException {
        CorrelationId correlationId;
        if (buildWorkDTO.getCorrelationId() == null) {
            correlationId = CorrelationId.generateUnique();
        } else {
            correlationId = new CorrelationId(buildWorkDTO.getCorrelationId());
        }

        try {
            CreateTaskDTO taskAdjustReqour = reqourAdjustAdapter
                    .generateRexTask(ownUrl, correlationId.getId(), buildWorkDTO, buildWorkDTO.toReqourAdjustDTO());
            CreateTaskDTO taskRepoSetup = repositoryDriverSetupAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    buildWorkDTO,
                    buildWorkDTO.toRepositoryDriverSetupDTO());

            CreateTaskDTO taskPncCreateEnv = environmentDriverCreateAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    buildWorkDTO,
                    buildWorkDTO.toPncEnvironmentDriverCreateDTO());
            CreateTaskDTO taskPncBuild = pncBuildDriverAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    buildWorkDTO,
                    buildWorkDTO.toPncBuildDriverDTO());
            CreateTaskDTO taskPncCompleteEnv = environmentDriverCompleteAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    buildWorkDTO,
                    buildWorkDTO.toPncEnvironmentDriverCompleteDTO());

            CreateTaskDTO taskRepoSeal = repositoryDriverSealAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    buildWorkDTO,
                    buildWorkDTO.toRepositoryDriverSealDTO());
            CreateTaskDTO taskRepoPromote = repositoryDriverPromoteAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    buildWorkDTO,
                    buildWorkDTO.toRepositoryDriverPromoteDTO());

            List<CreateTaskDTO> tasks = List
                    .of(taskAdjustReqour, taskRepoSetup, taskPncCreateEnv, taskPncBuild, taskPncCompleteEnv, taskRepoSeal,
                            taskRepoPromote);
            Map<String, CreateTaskDTO> vertices = getVertices(tasks);

            EdgeDTO adjustReqourToRepoSetup = EdgeDTO.builder()
                    .source(taskRepoSetup.name)
                    .target(taskAdjustReqour.name)
                    .build();
            EdgeDTO repoSetupToPncCreateEnv = EdgeDTO.builder()
                    .source(taskPncCreateEnv.name)
                    .target(taskRepoSetup.name)
                    .build();
            EdgeDTO pncCreateEnvToPncBuild = EdgeDTO.builder()
                    .source(taskPncBuild.name)
                    .target(taskPncCreateEnv.name)
                    .build();
            EdgeDTO adjustReqourToPncBuild = EdgeDTO.builder()
                    .source(taskPncBuild.name)
                    .target(taskAdjustReqour.name)
                    .build();
            EdgeDTO pncBuildToPncCompleteEnv = EdgeDTO.builder()
                    .source(taskPncCompleteEnv.name)
                    .target(taskPncBuild.name)
                    .build();
            EdgeDTO pncBuildToRepoSeal = EdgeDTO.builder()
                    .source(taskRepoSeal.name)
                    .target(taskPncBuild.name)
                    .build();
            EdgeDTO repoSealToRepoPromote = EdgeDTO.builder()
                    .source(taskRepoPromote.name)
                    .target(taskRepoSeal.name)
                    .build();

            Set<EdgeDTO> edges = Set.of(
                    adjustReqourToRepoSetup,
                    repoSetupToPncCreateEnv,
                    pncCreateEnvToPncBuild,
                    adjustReqourToPncBuild,
                    pncBuildToPncCompleteEnv,
                    pncBuildToRepoSeal,
                    repoSealToRepoPromote);

            ConfigurationDTO configurationDTO = ConfigurationDTO.builder()
                    .mdcHeaderKeyMapping(MDCUtils.HEADER_KEY_MAPPING)
                    .build();
            CreateGraphRequest graphRequest = new CreateGraphRequest(
                    correlationId.getId(),
                    rexQueueName,
                    configurationDTO,
                    edges,
                    vertices);
            setRexQueueSize(queueEndpoint, rexQueueName, rexQueueSize);
            taskEndpoint.start(graphRequest);

            return correlationId;

        } catch (Exception e) {
            throw new WorkflowSubmissionException(e);
        }
    }

    /**
     * Variant of submitWorkflow accepting the Rex startRequest
     *
     * @param startRequest
     * @return
     * @throws WorkflowSubmissionException
     */
    public CorrelationId submitWorkflow(StartRequest startRequest) throws WorkflowSubmissionException {
        BuildWorkDTO buildWorkDTO = objectMapper.convertValue(startRequest.getPayload(), BuildWorkDTO.class);
        CorrelationId correlationId = CorrelationId.generateUnique();

        try {
            CreateTaskDTO taskAdjustReqour = reqourAdjustAdapter
                    .generateRexTask(ownUrl, correlationId.getId(), startRequest, buildWorkDTO.toReqourAdjustDTO());
            CreateTaskDTO taskRepoSetup = repositoryDriverSetupAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    startRequest,
                    buildWorkDTO.toRepositoryDriverSetupDTO());

            CreateTaskDTO taskPncCreateEnv = environmentDriverCreateAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    startRequest,
                    buildWorkDTO.toPncEnvironmentDriverCreateDTO());
            CreateTaskDTO taskPncBuild = pncBuildDriverAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    startRequest,
                    buildWorkDTO.toPncBuildDriverDTO());
            CreateTaskDTO taskPncCompleteEnv = environmentDriverCompleteAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    startRequest,
                    buildWorkDTO.toPncEnvironmentDriverCompleteDTO());

            CreateTaskDTO taskRepoSeal = repositoryDriverSealAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    startRequest,
                    buildWorkDTO.toRepositoryDriverSealDTO());
            CreateTaskDTO taskRepoPromote = repositoryDriverPromoteAdapter.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    startRequest,
                    buildWorkDTO.toRepositoryDriverPromoteDTO());

            List<CreateTaskDTO> tasks = List
                    .of(taskAdjustReqour, taskRepoSetup, taskPncCreateEnv, taskPncBuild, taskPncCompleteEnv, taskRepoSeal,
                            taskRepoPromote);
            Map<String, CreateTaskDTO> vertices = getVertices(tasks);

            EdgeDTO adjustReqourToRepoSetup = EdgeDTO.builder()
                    .source(taskRepoSetup.name)
                    .target(taskAdjustReqour.name)
                    .build();
            EdgeDTO repoSetupToPncCreateEnv = EdgeDTO.builder()
                    .source(taskPncCreateEnv.name)
                    .target(taskRepoSetup.name)
                    .build();
            EdgeDTO pncCreateEnvToPncBuild = EdgeDTO.builder()
                    .source(taskPncBuild.name)
                    .target(taskPncCreateEnv.name)
                    .build();
            EdgeDTO adjustReqourToPncBuild = EdgeDTO.builder()
                    .source(taskPncBuild.name)
                    .target(taskAdjustReqour.name).build();
            EdgeDTO pncBuildToPncCompleteEnv = EdgeDTO.builder()
                    .source(taskPncCompleteEnv.name)
                    .target(taskPncBuild.name)
                    .build();
            EdgeDTO pncBuildToRepoSeal = EdgeDTO.builder()
                    .source(taskRepoSeal.name)
                    .target(taskPncBuild.name)
                    .build();
            EdgeDTO repoSealToRepoPromote = EdgeDTO.builder()
                    .source(taskRepoPromote.name)
                    .target(taskRepoSeal.name)
                    .build();

            Set<EdgeDTO> edges = Set.of(
                    adjustReqourToRepoSetup,
                    repoSetupToPncCreateEnv,
                    pncCreateEnvToPncBuild,
                    adjustReqourToPncBuild,
                    pncBuildToPncCompleteEnv,
                    pncBuildToRepoSeal,
                    repoSealToRepoPromote);

            ConfigurationDTO configurationDTO = ConfigurationDTO.builder()
                    .mdcHeaderKeyMapping(MDCUtils.HEADER_KEY_MAPPING)
                    .build();
            CreateGraphRequest graphRequest = new CreateGraphRequest(
                    correlationId.getId(),
                    rexQueueName,
                    configurationDTO,
                    edges,
                    vertices);
            setRexQueueSize(queueEndpoint, rexQueueName, rexQueueSize);
            taskEndpoint.start(graphRequest);

            return correlationId;

        } catch (Exception e) {
            throw new WorkflowSubmissionException(e);
        }
    }
}

package org.jboss.pnc.dingrogu.api.dto.workflow;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.BuildCategory;
import org.jboss.pnc.api.enums.BuildType;
import org.jboss.pnc.dingrogu.api.dto.adapter.BuildDriverDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.EnvironmentDriverCompleteDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.EnvironmentDriverCreateDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.RepositoryDriverPromoteDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.RepositoryDriverSealDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.RepositoryDriverSetupDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.RepourAdjustDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.ReqourAdjustDTO;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
@Builder
// TODO: use Mapstruct in the future?
public class BuildWorkDTO {

    String repourUrl;
    String reqourUrl;
    String repositoryDriverUrl;
    String buildDriverUrl;
    String environmentDriverUrl;

    String scmRepoURL;
    String scmRevision;
    boolean preBuildSyncEnabled;
    String originRepoURL;

    boolean tempBuild;

    AlignmentPreference alignmentPreference;
    @NotNull
    String buildContentId;
    String buildConfigName;
    BuildType buildType;
    BuildCategory buildCategory;
    String defaultAlignmentParams;
    boolean brewPullActive;
    Map<String, String> genericParameters;
    String buildConfigurationId;
    String correlationId;
    boolean debugEnabled;
    String environmentLabel;
    String environmentImage;

    String buildScript;
    String podMemoryOverride;

    public RepourAdjustDTO toRepourAdjustDTO() {
        String alignmentPreferenceName = null;
        if (alignmentPreference != null) {
            alignmentPreferenceName = alignmentPreference.name();
        }
        String buildTypeName = null;
        if (buildType != null) {
            buildTypeName = buildType.name();
        }
        return RepourAdjustDTO.builder()
                .repourUrl(repourUrl)
                .scmRepoURL(scmRepoURL)
                .scmRevision(scmRevision)
                .preBuildSyncEnabled(preBuildSyncEnabled)
                .originRepoURL(originRepoURL)
                .tempBuild(tempBuild)
                .alignmentPreference(alignmentPreferenceName)
                .id(buildContentId)
                .buildType(buildTypeName)
                .defaultAlignmentParams(defaultAlignmentParams)
                .brewPullActive(brewPullActive)
                .genericParameters(genericParameters)
                .build();
    }

    public ReqourAdjustDTO toReqourAdjustDTO() {
        return ReqourAdjustDTO.builder()
                .reqourUrl(reqourUrl)
                .scmRepoURL(scmRepoURL)
                .scmRevision(scmRevision)
                .preBuildSyncEnabled(preBuildSyncEnabled)
                .originRepoURL(originRepoURL)
                .tempBuild(tempBuild)
                .alignmentPreference(alignmentPreference)
                .id(buildContentId)
                .buildType(buildType)
                .defaultAlignmentParams(defaultAlignmentParams)
                .brewPullActive(brewPullActive)
                .genericParameters(genericParameters)
                .build();
    }

    public RepositoryDriverSetupDTO toRepositoryDriverSetupDTO() {
        String buildTypeName = null;
        if (buildType != null) {
            buildTypeName = buildType.name();
        }
        return RepositoryDriverSetupDTO.builder()
                .repositoryDriverUrl(repositoryDriverUrl)
                .buildContentId(buildContentId)
                .buildType(buildTypeName)
                .tempBuild(tempBuild)
                .brewPullActive(brewPullActive)
                .build();
    }

    public EnvironmentDriverCreateDTO toEnvironmentDriverCreateDTO() {
        return EnvironmentDriverCreateDTO.builder()
                .environmentDriverUrl(environmentDriverUrl)
                .environmentLabel(environmentLabel)
                .environmentImage(environmentImage)
                .buildContentId(buildContentId)
                .podMemoryOverride(podMemoryOverride)
                .debugEnabled(debugEnabled)
                .buildConfigId(buildConfigurationId)
                .build();
    }

    public BuildDriverDTO toBuildDriverDTO() {
        return BuildDriverDTO.builder()
                .buildDriverUrl(buildDriverUrl)
                .projectName(buildConfigName)
                .buildCommand(buildScript)
                .debugEnabled(debugEnabled)
                .build();
    }

    public EnvironmentDriverCompleteDTO toEnvironmentDriverCompleteDTO() {
        return EnvironmentDriverCompleteDTO.builder()
                .environmentDriverUrl(environmentDriverUrl)
                .debugEnabled(debugEnabled)
                .build();
    }

    public RepositoryDriverSealDTO toRepositoryDriverSealDTO() {
        return RepositoryDriverSealDTO.builder()
                .repositoryDriverUrl(repositoryDriverUrl)
                .buildContentId(buildContentId)
                .build();

    }

    public RepositoryDriverPromoteDTO toRepositoryDriverPromoteDTO() {
        return RepositoryDriverPromoteDTO.builder()
                .repositoryDriverUrl(repositoryDriverUrl)
                .buildContentId(buildContentId)
                .buildType(buildType)
                .buildCategory(buildCategory)
                .tempBuild(tempBuild)
                .buildConfigurationId(buildConfigurationId)
                .build();

    }
}

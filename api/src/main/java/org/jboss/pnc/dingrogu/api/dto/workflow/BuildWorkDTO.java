package org.jboss.pnc.dingrogu.api.dto.workflow;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.BuildCategory;
import org.jboss.pnc.api.enums.BuildType;
import org.jboss.pnc.dingrogu.api.dto.adapter.KonfluxBuildDriverDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.PncBuildDriverDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.PncEnvironmentDriverCompleteDTO;
import org.jboss.pnc.dingrogu.api.dto.adapter.PncEnvironmentDriverCreateDTO;
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
    String konfluxBuildDriverUrl;
    String pncBuildDriverUrl;
    String pncEnvironmentDriverUrl;

    String scmRepoURL;
    String scmRevision;
    boolean preBuildSyncEnabled;
    String originRepoURL;

    boolean tempBuild;

    AlignmentPreference alignmentPreference;
    @NotNull
    String buildContentId;
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
    String buildConfigId;

    String buildScript;
    String buildTool;
    String recipeImage;
    String podMemoryOverride;
    String namespace;
    String buildToolVersion;
    String javaVersion;
    String buildCommand;

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

    public KonfluxBuildDriverDTO toKonfluxBuildDriverDTO() {
        return KonfluxBuildDriverDTO.builder()
                .konfluxBuildDriverUrl(konfluxBuildDriverUrl)
                .buildContentId(buildContentId)
                .buildScript(buildScript)
                .buildTool(buildTool)
                .recipeImage(recipeImage)
                .podMemoryOverride(podMemoryOverride)
                .namespace(namespace)
                .buildToolVersion(buildToolVersion)
                .javaVersion(javaVersion)
                .build();
    }

    public PncEnvironmentDriverCreateDTO toPncEnvironmentDriverCreateDTO() {
        return PncEnvironmentDriverCreateDTO.builder()
                .pncEnvironmentDriverUrl(pncEnvironmentDriverUrl)
                .environmentLabel(environmentLabel)
                .environmentImage(environmentImage)
                .buildContentId(buildContentId)
                .podMemoryOverride(podMemoryOverride)
                .debugEnabled(debugEnabled)
                .buildConfigId(buildConfigId)
                .build();
    }

    public PncBuildDriverDTO toPncBuildDriverDTO() {
        return PncBuildDriverDTO.builder()
                .pncBuildDriverUrl(pncBuildDriverUrl)
                .buildCommand(buildCommand)
                .debugEnabled(debugEnabled)
                .build();
    }

    public PncEnvironmentDriverCompleteDTO toPncEnvironmentDriverCompleteDTO() {
        return PncEnvironmentDriverCompleteDTO.builder()
                .pncEnvironmentDriverUrl(pncEnvironmentDriverUrl)
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

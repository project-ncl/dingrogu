package org.jboss.pnc.dingrogu.api.dto.adapter;

public enum ProcessStage {
    REPO_SETTING_UP,
    BUILD_ENV_SETTING_UP,
    BUILD_SETTING_UP,
    SEALING_REPOSITORY_MANAGER_RESULTS,
    COLLECTING_RESULTS_FROM_REPOSITORY_MANAGER,
    FINALIZING_BUILD
}

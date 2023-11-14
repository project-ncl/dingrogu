package org.jboss.pnc.dingrogu.dto.adapter;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class RepourCreateRepositoryDTO {

    String repourUrl;
    String externalUrl;
}

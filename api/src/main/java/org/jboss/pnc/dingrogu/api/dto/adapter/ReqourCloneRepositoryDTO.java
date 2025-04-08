package org.jboss.pnc.dingrogu.api.dto.adapter;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
@Builder
public class ReqourCloneRepositoryDTO {

    String reqourUrl;
    String externalUrl;
    String ref;
}

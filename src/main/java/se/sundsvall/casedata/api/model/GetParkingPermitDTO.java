package se.sundsvall.casedata.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetParkingPermitDTO {

    private String artefactPermitNumber;

    private String artefactPermitStatus;

    private Long errandId;

    private DecisionDTO errandDecision;

}

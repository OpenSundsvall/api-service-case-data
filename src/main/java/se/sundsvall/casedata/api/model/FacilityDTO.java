package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import se.sundsvall.casedata.integration.db.model.enums.FacilityType;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FacilityDTO extends BaseDTO {

    @Schema(example = "En fritextbeskrivning av facility.")
    @Size(max = 255)
    private String description;

    @Valid
    private AddressDTO address;

    @Schema(description = "The name on the sign.", example = "Sundsvalls testfabrik")
    @Size(max = 255)
    private String facilityCollectionName;

    private boolean mainFacility;

    private FacilityType facilityType;

    private Map<String, String> extraParameters = new HashMap<>();

}

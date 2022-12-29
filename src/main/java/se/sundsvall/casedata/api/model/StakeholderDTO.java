package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class StakeholderDTO extends BaseDTO {

    @NotNull
    private StakeholderType type;

    @Size(max = 255)
    @Schema(example = "Test")
    private String firstName;

    @Size(max = 255)
    @Schema(example = "Testorsson")
    private String lastName;

    @ValidUuid(nullable = true)
    @Schema(example = "3ed5bc30-6308-4fd5-a5a7-78d7f96f4438")
    private String personId;

    @Size(max = 255)
    @Schema(example = "Sundsvalls testfabrik")
    private String organizationName;

    @Size(max = 255)
    @Pattern(regexp = Constants.ORGNR_PATTERN_REGEX, message = Constants.ORGNR_PATTERN_MESSAGE)
    @Schema(description = "Organization number with 10 or 12 digits.", example = "19901010-1234")
    private String organizationNumber;

    @Size(max = 255)
    @Schema(example = "Test Testorsson")
    private String authorizedSignatory;

    @Schema(description = "AD-account")
    @Size(max = 36)
    private String adAccount;

    @NotNull
    @Schema(description = "An stakeholder can have one or more roles.")
    private List<StakeholderRole> roles = new ArrayList<>();

    @Valid
    @Schema(description = "An stakeholder may have one or more addresses. For example one POSTAL_ADDRESS and another INVOICE_ADDRESS.")
    private List<AddressDTO> addresses = new ArrayList<>();

    private List<ContactInformationDTO> contactInformation = new ArrayList<>();

    private Map<String, String> extraParameters = new HashMap<>();

}
package se.sundsvall.casedata.api.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AddressDTO {

    private AddressCategory addressCategory;

    @Schema(example = "Testvägen")
    @Size(max = 255)
    private String street;

    @Schema(example = "18")
    @Size(max = 255)
    private String houseNumber;

    @Schema(example = "123 45")
    @Size(max = 255)
    private String postalCode;

    @Schema(example = "Sundsvall")
    @Size(max = 255)
    private String city;

    @Schema(example = "Sverige")
    @Size(max = 255)
    private String country;

    @Schema(description = "c/o", example = "Test Testorsson")
    @Size(max = 255)
    private String careOf;

    @Schema(example = "Test Testorsson")
    @Size(max = 255)
    private String attention;

    @Schema(example = "SUNDSVALL LJUSTA 7:2")
    @Size(max = 255)
    private String propertyDesignation;

    @Schema(example = "LGH 1001")
    @Size(max = 255)
    private String apartmentNumber;

    private Boolean isZoningPlanArea;

    @Schema(description = "Only in combination with addressCategory: INVOICE_ADDRESS")
    @Size(max = 255)
    private String invoiceMarking;

    private CoordinatesDTO location;


}

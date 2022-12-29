package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CoordinatesDTO {

    @Schema(description = "Decimal Degrees (DD)", example = "62.390205")
    private Double latitude;
    @Schema(description = "Decimal Degrees (DD)", example = "17.306616")
    private Double longitude;

}
package se.sundsvall.casedata.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.casedata.integration.db.model.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PatchErrandDTO {

    @Schema(description = "Case ID from the client.", example = "caa230c6-abb4-4592-ad9a-34e263c2787b")
    @Size(max = 255)
    private String externalCaseId;

    private CaseType caseType;

    @Schema(example = "MEDIUM")
    private Priority priority;

    @Schema(example = "Some description of the case.")
    @Size(max = 255)
    private String description;

    @Schema(description = "Additions to the case title. Right now only applicable to cases of CaseType: NYBYGGNAD_ANSOKAN_OM_BYGGLOV.", example = "Eldstad/rökkanal, Skylt")
    @Size(max = 255)
    private String caseTitleAddition;

    @Size(max = 255)
    private String diaryNumber;

    @Schema(example = "Aktualisering")
    @Size(max = 255)
    private String phase;

    @Size(max = 255)
    private String municipalityId;

    @Schema(description = "Start date for the business.", format = "date", example = "2022-01-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "End date of the business if it is time-limited.", format = "date", example = "2022-06-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "The time the application was received.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime applicationReceived;

    private Map<String, String> extraParameters = new HashMap<>();

}

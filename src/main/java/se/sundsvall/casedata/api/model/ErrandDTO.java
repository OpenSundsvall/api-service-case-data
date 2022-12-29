package se.sundsvall.casedata.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.casedata.integration.db.model.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErrandDTO extends BaseDTO {

    @Schema(example = "PRH-2022-000001", accessMode = Schema.AccessMode.READ_ONLY)
    private String errandNumber;

    @Schema(description = "Case ID from the client.", example = "caa230c6-abb4-4592-ad9a-34e263c2787b")
    @Size(max = 255)
    private String externalCaseId;

    @NotNull
    private CaseType caseType;

    @Schema(defaultValue = "MEDIUM")
    private Priority priority = Priority.MEDIUM;

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

    private List<StatusDTO> statuses = new ArrayList<>();

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

    @Schema(description = "Process-ID from ProcessEngine",example = "c3cb9123-4ed2-11ed-ac7c-0242ac110003", accessMode = Schema.AccessMode.READ_ONLY)
    private String processId;

    @Valid
    private List<StakeholderDTO> stakeholders = new ArrayList<>();

    @Valid
    private List<FacilityDTO> facilities = new ArrayList<>();

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Valid
    private List<AttachmentDTO> attachments = new ArrayList<>();

    @Valid
    private List<DecisionDTO> decisions = new ArrayList<>();

    @Valid
    private List<NoteDTO> notes = new ArrayList<>();

    @Schema(description = "Messages connected to this errand. Get message information from Message-API.", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> messageIds = new ArrayList<>();

    @Schema(description = "The client who created the errand. WSO2-username.", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdByClient;

    @Schema(description = "The most recent client who updated the errand. WSO2-username.", accessMode = Schema.AccessMode.READ_ONLY)
    private String updatedByClient;

    @Schema(description = "The user who created the errand.", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdBy;

    @Schema(description = "The most recent user who updated the errand.", accessMode = Schema.AccessMode.READ_ONLY)
    private String updatedBy;

    private Map<String, String> extraParameters = new HashMap<>();

}

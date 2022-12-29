package se.sundsvall.casedata.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.api.model.history.HistoryDTO;
import se.sundsvall.casedata.service.AttachmentService;
import se.sundsvall.casedata.service.DecisionService;
import se.sundsvall.casedata.service.ErrandService;
import se.sundsvall.casedata.service.FacilityService;
import se.sundsvall.casedata.service.NoteService;
import se.sundsvall.casedata.service.StakeholderService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@Validated
@RequestMapping("/")
@Tag(name = "History", description = "History operations")
@ApiResponse(responseCode = "200", description = "OK - Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = HistoryDTO.class))))
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class HistoryResource {
    private final AttachmentService attachmentService;
    private final DecisionService decisionService;
    private final ErrandService errandService;
    private final FacilityService facilityService;
    private final NoteService noteService;
    private final StakeholderService stakeholderService;

    public HistoryResource(AttachmentService attachmentService, DecisionService decisionService, ErrandService errandService, FacilityService facilityService, NoteService noteService, StakeholderService stakeholderService) {
        this.attachmentService = attachmentService;
        this.decisionService = decisionService;
        this.errandService = errandService;
        this.facilityService = facilityService;
        this.noteService = noteService;
        this.stakeholderService = stakeholderService;
    }

    @GetMapping(path = "attachments/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(description = "Get attachment history.")
    public ResponseEntity<String> getAttachmentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(attachmentService.findHistory(id));
    }

    @GetMapping(path = "decisions/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(description = "Get decision history.")
    public ResponseEntity<String> getDecisionHistory(@PathVariable Long id) {
        return ResponseEntity.ok(decisionService.findHistory(id));
    }

    @GetMapping(path = "errands/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(description = "Get errand history.")
    public ResponseEntity<String> getErrandHistory(@PathVariable Long id) {
        return ResponseEntity.ok(errandService.findHistory(id));
    }

    @GetMapping(path = "facilities/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(description = "Get facility history.")
    public ResponseEntity<String> getFacilityHistory(@PathVariable Long id) {
        return ResponseEntity.ok(facilityService.findHistory(id));
    }

    @GetMapping(path = "notes/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(description = "Get note history.")
    public ResponseEntity<String> getNoteHistory(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.findHistory(id));
    }

    @GetMapping(path = "stakeholders/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(description = "Get stakeholder history.")
    public ResponseEntity<String> getStakeholderHistory(@PathVariable Long id) {
        return ResponseEntity.ok(stakeholderService.findHistory(id));
    }
}

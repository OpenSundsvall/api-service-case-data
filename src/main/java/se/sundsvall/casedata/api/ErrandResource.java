package se.sundsvall.casedata.api;

import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.ExtraParameterDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.StatusDTO;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.service.ErrandService;

import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.turkraft.springfilter.FilterParameters.LOCALDATE_FORMATTER;
import static com.turkraft.springfilter.FilterParameters.OFFSETDATETIME_FORMATTER;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@Validated
@RequestMapping("errands")
@Tag(name = "Errands", description = "Errand operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class ErrandResource {

    private final ErrandService errandService;

    public ErrandResource(ErrandService errandService) {
        this.errandService = errandService;

        // Spring-filter config
        OFFSETDATETIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
        LOCALDATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    }

    // GET

    @Operation(description = "Get errand by ID.")
    @GetMapping(path = "/{id}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK - Successful operation")
    public ResponseEntity<ErrandDTO> getErrandById(@PathVariable Long id) {
        return ResponseEntity.ok(errandService.findById(id));
    }

    @GetMapping(produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(description = "Get errands with or without query. The query is very flexible and allows you as a client to control a lot yourself.")
    @ApiResponse(responseCode = "200", description = "OK - Successful operation")
    public ResponseEntity<Page<ErrandDTO>> getErrands(
            @Parameter(
                    description = "Syntax description: [spring-filter](https://github.com/turkraft/spring-filter/blob/85730f950a5f8623159cc0eb4d737555f9382bb7/README.md#syntax)",
                    example = "caseType:'PARKING_PERMIT' and stakeholders.firstName~'*mar*' and applicationReceived>'2022-09-08T12:18:03.747+02:00'",
                    schema = @Schema(implementation = String.class))
            @Filter Specification<Errand> filter,
            @Parameter(description = "extraParameters on errand. Use like this: extraParameters[artefact.permit.number]=12345&extraParameters[disability.aid]=Rullstol") Optional<ExtraParameterDTO> extraParameterDTO,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(errandService.findAll(filter, extraParameterDTO.orElse(new ExtraParameterDTO()).getExtraParameters(), pageable));
    }

    @Operation(description = "Get all messages on an errand.")
    @GetMapping(path = "/{id}/message-ids", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK - Successful operation")
    public ResponseEntity<List<String>> getMessagesOnErrand(@PathVariable Long id) {
        return ResponseEntity.ok(errandService.findMessagesOnErrand(id));
    }

    @Operation(description = "Get decisions on errand.")
    @GetMapping(path = "/{id}/decisions", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK - Successful operation")
    public ResponseEntity<List<DecisionDTO>> getDecision(@PathVariable Long id) {
        return ResponseEntity.ok(errandService.findDecisionsOnErrand(id));
    }

    // POST

    @Operation(description = "Create errand (without attachments). Add attachments to errand with PATCH /errands/{id}/attachments afterwards.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."))
    public ResponseEntity<Void> postErrands(UriComponentsBuilder uriComponentsBuilder, @RequestBody @Valid ErrandDTO errandDTO) {
        ErrandDTO result = errandService.saveErrandAndStartProcess(errandDTO);
        return ResponseEntity.created(uriComponentsBuilder.path("/errands/{id}").buildAndExpand(result.getId()).toUri())
                .build();
    }


    // PATCH

    @Operation(description = "Update errand.")
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> patchErrand(@PathVariable Long id, @RequestBody @Valid PatchErrandDTO patchErrandDTO) {
        errandService.patchErrand(id, patchErrandDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Create and add attachment to errand.")
    @PatchMapping(path = "/{id}/attachments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."))
    public ResponseEntity<Void> patchErrandWithAttachment(UriComponentsBuilder uriComponentsBuilder, @PathVariable Long id, @RequestBody @Valid AttachmentDTO attachmentDTO) {
        Attachment result = errandService.patchErrand(id, attachmentDTO);
        return ResponseEntity.created(uriComponentsBuilder.path("/attachments/{id}").buildAndExpand(result.getId()).toUri())
                .build();
    }

    @Operation(description = "Add status to errand.")
    @PatchMapping(path = "/{id}/statuses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> patchErrandWithStatus(@PathVariable Long id, @RequestBody @Valid StatusDTO statusDTO) {
        errandService.patchErrand(id, statusDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Create and add note to errand.")
    @PatchMapping(path = "/{id}/notes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."))
    public ResponseEntity<Void> patchErrandWithNote(UriComponentsBuilder uriComponentsBuilder, @PathVariable Long id, @RequestBody @Valid NoteDTO noteDTO) {
        Note result = errandService.patchErrand(id, noteDTO);
        return ResponseEntity.created(uriComponentsBuilder.path("/notes/{id}").buildAndExpand(result.getId()).toUri())
                .build();
    }

    @Operation(description = "Create and add decision to errand.")
    @PatchMapping(path = "/{id}/decisions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."))
    public ResponseEntity<Void> patchErrandWithDecision(UriComponentsBuilder uriComponentsBuilder, @PathVariable Long id, @RequestBody @Valid DecisionDTO decisionDTO) {
        Decision result = errandService.patchErrand(id, decisionDTO);
        return ResponseEntity.created(uriComponentsBuilder.path("/decisions/{id}").buildAndExpand(result.getId()).toUri())
                .build();
    }

    @Operation(description = "Add messageIds to errand.")
    @PatchMapping(path = "/{id}/message-ids", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> patchErrandWithMessage(@PathVariable Long id, @RequestBody List<String> messageIds) {
        errandService.patchErrandWithMessage(id, messageIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Create and add stakeholder to errand.")
    @PatchMapping(path = "/{id}/stakeholders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."))
    public ResponseEntity<Void> patchErrandWithStakeholder(UriComponentsBuilder uriComponentsBuilder, @PathVariable Long id, @RequestBody @Valid StakeholderDTO stakeholderDTO) {
        Stakeholder result = errandService.patchErrand(id, stakeholderDTO);
        return ResponseEntity.created(uriComponentsBuilder.path("/stakeholders/{id}").buildAndExpand(result.getId()).toUri())
                .build();
    }

    // PUT

    @Operation(description = "Add/replace status on errand.")
    @PutMapping(path = "/{id}/statuses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> putStatusOnErrand(@PathVariable Long id, @RequestBody @Valid List<StatusDTO> statusDTOList) {
        errandService.putStatusesOnErrand(id, statusDTOList);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Replace attachments on errand.")
    @PutMapping(path = "/{id}/attachments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> putAttachmentsOnErrand(@PathVariable Long id, @RequestBody @Valid List<AttachmentDTO> attachmentDTOList) {
        errandService.putAttachmentsOnErrand(id, attachmentDTOList);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Replace stakeholders on errand.")
    @PutMapping(path = "/{id}/stakeholders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> putStakeholdersOnErrand(@PathVariable Long id, @RequestBody @Valid List<StakeholderDTO> stakeholderDTOList) {
        errandService.putStakeholdersOnErrand(id, stakeholderDTOList);
        return ResponseEntity.noContent().build();
    }

    // DELETE
    @Operation(description = "Delete attachment on errand.")
    @DeleteMapping(path = "/{id}/attachments/{attachmentId}", produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        errandService.deleteAttachmentOnErrand(id, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Delete stakeholder on errand.")
    @DeleteMapping(path = "/{id}/stakeholders/{stakeholderId}", produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> deleteStakeholder(@PathVariable Long id, @PathVariable Long stakeholderId) {
        errandService.deleteStakeholderOnErrand(id, stakeholderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Delete decision on errand.")
    @DeleteMapping(path = "/{id}/decisions/{decisionId}", produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> deleteDecision(@PathVariable Long id, @PathVariable Long decisionId) {
        errandService.deleteDecisionOnErrand(id, decisionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Delete note on errand.")
    @DeleteMapping(path = "/{id}/notes/{noteId}", produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id, @PathVariable Long noteId) {
        errandService.deleteNoteOnErrand(id, noteId);
        return ResponseEntity.noContent().build();
    }
}


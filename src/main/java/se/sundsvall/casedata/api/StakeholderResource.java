package se.sundsvall.casedata.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.service.StakeholderService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@Validated
@RequestMapping("stakeholders")
@Tag(name="Stakeholders", description = "Stakeholder operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class StakeholderResource {

    private final StakeholderService stakeholderService;

    public StakeholderResource(StakeholderService stakeholderService) {
        this.stakeholderService = stakeholderService;
    }

    @Operation(description = "Get stakeholder by ID.")
    @GetMapping(path = "/{id}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK - Successful operation")
    public ResponseEntity<StakeholderDTO> getStakeholders(@PathVariable Long id) {
        return ResponseEntity.ok(stakeholderService.findById(id));
    }

    @GetMapping(produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "Successful operation")
    public ResponseEntity<List<StakeholderDTO>> getStakeholders(@RequestParam(required = false) Optional<StakeholderRole> stakeholderRole) {
        return stakeholderRole.map(role -> ResponseEntity.ok(stakeholderService.findStakeholdersByRole(role))).orElseGet(() -> ResponseEntity.ok(stakeholderService.findAllStakeholders()));
    }

    @Operation(description = "Update stakeholder.")
    @PatchMapping(path = "/{stakeholderId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> patchStakeholder(@PathVariable Long stakeholderId, @RequestBody @Valid StakeholderDTO stakeholderDTO) {
        stakeholderService.patch(stakeholderId, stakeholderDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Replace stakeholder.")
    @PutMapping(path = "/{stakeholderId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> putStakeholder(@PathVariable Long stakeholderId, @RequestBody @Valid StakeholderDTO stakeholderDTO) {
        stakeholderService.put(stakeholderId, stakeholderDTO);
        return ResponseEntity.noContent().build();
    }
}

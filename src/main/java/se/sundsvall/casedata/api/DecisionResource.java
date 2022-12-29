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
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.service.DecisionService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
@RestController
@Validated
@RequestMapping("decisions")
@Tag(name = "Decisions", description = "Decision operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class DecisionResource {
    private final DecisionService decisionService;

    public DecisionResource(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @Operation(description = "Get decision by ID.")
    @GetMapping(path = "/{id}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK - Successful operation")
    public ResponseEntity<DecisionDTO> getDecisionById(@PathVariable Long id) {
        return ResponseEntity.ok(decisionService.findById(id));
    }

    @Operation(description = "Update decision.")
    @PatchMapping(path = "/{decisionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> patchDecision(@PathVariable Long decisionId, @RequestBody @Valid PatchDecisionDTO patchDecisionDTO) {
        decisionService.patch(decisionId, patchDecisionDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Replace decision.")
    @PutMapping(path = "/{decisionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful operation")
    public ResponseEntity<Void> putDecision(@PathVariable Long decisionId, @RequestBody @Valid DecisionDTO decisionDTO) {
        decisionService.put(decisionId, decisionDTO);
        return ResponseEntity.noContent().build();
    }
}

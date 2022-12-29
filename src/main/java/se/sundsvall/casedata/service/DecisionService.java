package se.sundsvall.casedata.service;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;
import se.sundsvall.casedata.service.util.mappers.PutMapper;

@Service
public class DecisionService {
    private static final ThrowableProblem DECISION_NOT_FOUND_PROBLEM = Problem.valueOf(Status.NOT_FOUND, "Decision not found");

    private final Javers javers;
    private final DecisionRepository decisionRepository;

    public DecisionService(Javers javers, DecisionRepository decisionRepository) {
        this.javers = javers;
        this.decisionRepository = decisionRepository;
    }

    public String findHistory(Long id) {
        QueryBuilder query = QueryBuilder.byInstanceId(id, Decision.class).withChildValueObjects();
        Changes changes = javers.findChanges(query.build());
        if (changes.isEmpty()) {
            throw DECISION_NOT_FOUND_PROBLEM;
        }
        return javers.getJsonConverter().toJson(changes);
    }

    public void put(Long id, DecisionDTO dto) {
        var entity = getDecision(id);
        PutMapper.INSTANCE.putDecision(entity, dto);
        decisionRepository.save(entity);
    }

    public void patch(Long id, PatchDecisionDTO dto) {
        var decision = getDecision(id);
        PatchMapper.INSTANCE.updateDecision(decision, dto);
        decisionRepository.save(decision);
    }

    public DecisionDTO findById(Long id) {
        return EntityDtoMapper.INSTANCE.decisionToDto(getDecision(id));
    }

    private Decision getDecision(Long id) {
        return decisionRepository.findById(id).orElseThrow(() -> DECISION_NOT_FOUND_PROBLEM);
    }
}

package se.sundsvall.casedata.service;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.integration.db.model.Facility;

@Service
public class FacilityService {
    private static final ThrowableProblem FACILITY_NOT_FOUND_PROBLEM = Problem.valueOf(Status.NOT_FOUND, "Facility not found");

    private final Javers javers;

    public FacilityService(Javers javers) {
        this.javers = javers;
    }

    public String findHistory(Long id) {
        QueryBuilder query = QueryBuilder.byInstanceId(id, Facility.class).withChildValueObjects();
        Changes changes = javers.findChanges(query.build());
        if (changes.isEmpty()) {
            throw FACILITY_NOT_FOUND_PROBLEM;
        }
        return javers.getJsonConverter().toJson(changes);
    }
}

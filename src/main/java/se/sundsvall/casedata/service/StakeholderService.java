package se.sundsvall.casedata.service;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;
import se.sundsvall.casedata.service.util.mappers.PutMapper;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class StakeholderService {

    private static final ThrowableProblem STAKEHOLDER_NOT_FOUND_PROBLEM = Problem.valueOf(Status.NOT_FOUND, "Stakeholder not found");
    private final Javers javers;
    private final StakeholderRepository stakeholderRepository;


    public StakeholderService(Javers javers, StakeholderRepository stakeholderRepository) {
        this.javers = javers;
        this.stakeholderRepository = stakeholderRepository;
    }

    public List<StakeholderDTO> findAllStakeholders() {
        List<Stakeholder> stakeholderList = stakeholderRepository.findAll();
        if (stakeholderList.isEmpty()) {
            throw STAKEHOLDER_NOT_FOUND_PROBLEM;
        }
        return stakeholderList.stream().map(EntityDtoMapper.INSTANCE::stakeholderToDto).toList();
    }

    public List<StakeholderDTO> findStakeholdersByRole(StakeholderRole stakeholderRole) {
        List<Stakeholder> stakeholderList = stakeholderRepository.findByRoles(stakeholderRole);

        if (stakeholderList.isEmpty()) {
            throw STAKEHOLDER_NOT_FOUND_PROBLEM;
        }

        return stakeholderList.stream().map(EntityDtoMapper.INSTANCE::stakeholderToDto).toList();
    }

    public StakeholderDTO findById(Long id) {
        return EntityDtoMapper.INSTANCE.stakeholderToDto(stakeholderRepository.findById(id).orElseThrow(() -> STAKEHOLDER_NOT_FOUND_PROBLEM));
    }

    public String findHistory(Long id) {
        QueryBuilder query = QueryBuilder.byInstanceId(id, Stakeholder.class).withChildValueObjects();
        Changes changes = javers.findChanges(query.build());
        if (changes.isEmpty()) {
            throw STAKEHOLDER_NOT_FOUND_PROBLEM;
        }
        return javers.getJsonConverter().toJson(changes);
    }

    public void patch(Long stakeholderId, StakeholderDTO stakeholderDTO) {
        var entity = getStakeholder(stakeholderId);
        PatchMapper.INSTANCE.updateStakeholder(entity, stakeholderDTO);
        stakeholderRepository.save(entity);
    }

    public void put(Long id, StakeholderDTO dto) {
        var entity = getStakeholder(id);
        PutMapper.INSTANCE.putStakeholder(entity, dto);
        stakeholderRepository.save(entity);
    }

    private Stakeholder getStakeholder(Long id) {
        return stakeholderRepository.findById(id).orElseThrow(() -> STAKEHOLDER_NOT_FOUND_PROBLEM);
    }

}

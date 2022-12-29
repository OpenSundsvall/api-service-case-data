package se.sundsvall.casedata.service;

import generated.client.processengine.ParkingPermitResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.StatusDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ErrandService {

    private static final String ERRAND_WAS_NOT_FOUND = "Errand was not found";
    private static final ThrowableProblem ERRAND_NOT_FOUND_PROBLEM = Problem.valueOf(Status.NOT_FOUND, ERRAND_WAS_NOT_FOUND);
    private static final String DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID = "Decision was not found on errand with id: {0}";
    private static final String DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Decision with id: {0} was not found on errand with id: {1}";
    private static final String NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Note with id: {0} was not found on errand with id: {1}";
    private static final String STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Stakeholder with id: {0} was not found on errand with id: {1}";
    private static final String ATTACHMENT_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Attachment with id: {0} was not found on errand with id: {1}";
    private final ErrandRepository errandRepository;

    private final Javers javers;
    private final ProcessEngineService processEngineService;

    public ErrandService(ErrandRepository errandRepository, Javers javers, ProcessEngineService processEngineService) {
        this.errandRepository = errandRepository;
        this.javers = javers;
        this.processEngineService = processEngineService;
    }

    //////////////////////////////
    // GET operations
    //////////////////////////////

    public String findHistory(Long id) {
        QueryBuilder query = QueryBuilder.byInstanceId(id, Errand.class).withChildValueObjects();
        Changes changes = javers.findChanges(query.build());
        if (changes.isEmpty()) {
            throw ERRAND_NOT_FOUND_PROBLEM;
        }
        return javers.getJsonConverter().toJson(changes);
    }

    public List<DecisionDTO> findDecisionsOnErrand(Long id) {
        List<Decision> decisionList = getErrand(id).getDecisions();
        if (decisionList == null || decisionList.isEmpty()) {
            throw Problem.valueOf(Status.NOT_FOUND, MessageFormat.format(DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID, id));
        } else {
            return decisionList.stream().map(EntityDtoMapper.INSTANCE::decisionToDto).toList();
        }
    }

    public List<String> findMessagesOnErrand(Long id) {
        var errand = getErrand(id);

        if (errand.getMessageIds() == null || errand.getMessageIds().isEmpty()) {
            throw Problem.valueOf(Status.NOT_FOUND, MessageFormat.format("Messages was not found on errand with id: {0}", id));
        } else {
            return errand.getMessageIds();
        }
    }

    public ErrandDTO findById(Long id) {
        return EntityDtoMapper.INSTANCE.errandToDto(errandRepository.findById(id).orElseThrow(() -> ERRAND_NOT_FOUND_PROBLEM));
    }

    /**
     * @return Page of ErrandDTO without duplicates
     */
    public Page<ErrandDTO> findAll(Specification<Errand> specification, Map<String, String> extraParameters, Pageable pageable) {
        // Extract all ID's and remove duplicates
        List<Long> allIds = errandRepository.findAll(specification).stream()
                .filter(errandDTO -> hashmapContainsAllKeyAndValues(errandDTO.getExtraParameters(), extraParameters))
                .map(Errand::getId)
                .collect(Collectors.toSet())
                .stream().toList();

        // Get errands without duplicates
        Page<ErrandDTO> errandDTOPage = errandRepository.findAllByIdIn(allIds, pageable)
                .map(EntityDtoMapper.INSTANCE::errandToDto);

        if (errandDTOPage.isEmpty()) {
            throw ERRAND_NOT_FOUND_PROBLEM;
        }

        return errandDTOPage;
    }

    private boolean hashmapContainsAllKeyAndValues(Map<String, String> map, Map<String, String> mapToCheck) {
        for (Map.Entry<String, String> entry : mapToCheck.entrySet()) {
            String mapValue = map.get(entry.getKey());
            if (!entry.getValue().equals(mapValue)) {
                return false;
            }
        }
        return true;
    }

    public Errand getErrand(Long errandId) {
        return errandRepository.findById(errandId).orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, MessageFormat.format("Errand with id: {0} was not found", errandId)));
    }

    //////////////////////////////
    // POST operations
    //////////////////////////////
    public ErrandDTO saveErrandAndStartProcess(ErrandDTO errandDTO) {
        var errand = EntityDtoMapper.INSTANCE.dtoToErrand(errandDTO);

        Errand resultErrand = errandRepository.save(errand);

        try {
            ParkingPermitResponse parkingPermitResponse = processEngineService.startProcess(resultErrand.getId());
            resultErrand.setProcessId(parkingPermitResponse.getProcessId());
            errandRepository.save(resultErrand);
        } catch (Exception e) {
            errandRepository.delete(resultErrand);
            throw e;
        }

        return EntityDtoMapper.INSTANCE.errandToDto(resultErrand);
    }

    //////////////////////////////
    // DELETE operations
    //////////////////////////////

    @Retry(name = "OptimisticLocking")
    public void deleteAttachmentOnErrand(Long errandId, Long attachmentId) {
        Errand errand = getErrand(errandId);
        var attachmentToRemove = errand.getAttachments().stream().filter(attachment -> attachment.getId().equals(attachmentId)).findAny();

        if (attachmentToRemove.isPresent()) {
            errand.getAttachments().remove(attachmentToRemove.get());
        } else {
            throw Problem.valueOf(Status.NOT_FOUND, MessageFormat.format(ATTACHMENT_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, attachmentId, errandId));
        }

        saveErrandAndUpdateProcess(errand);
    }

    @Retry(name = "OptimisticLocking")
    public void deleteStakeholderOnErrand(Long errandId, Long stakeholderId) {
        Errand errand = getErrand(errandId);
        var stakeholderToRemove = errand.getStakeholders().stream().filter(stakeholder -> stakeholder.getId().equals(stakeholderId)).findAny();

        if (stakeholderToRemove.isPresent()) {
            errand.getStakeholders().remove(stakeholderToRemove.get());
        } else {
            throw Problem.valueOf(Status.NOT_FOUND, MessageFormat.format(STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, stakeholderId, errandId));
        }

        saveErrandAndUpdateProcess(errand);
    }

    @Retry(name = "OptimisticLocking")
    public void deleteDecisionOnErrand(Long errandId, Long decisionId) {
        Errand errand = getErrand(errandId);
        Decision decisionToRemove = errand.getDecisions().stream().filter(decision -> decision.getId().equals(decisionId)).findAny().orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, MessageFormat.format(DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, decisionId, errandId)));

        errand.getDecisions().remove(decisionToRemove);

        saveErrandAndUpdateProcess(errand);
    }

    @Retry(name = "OptimisticLocking")
    public void deleteNoteOnErrand(Long errandId, Long noteId) {
        Errand errand = getErrand(errandId);
        Note noteToRemove = errand.getNotes().stream().filter(note -> note.getId().equals(noteId)).findAny().orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, MessageFormat.format(NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, noteId, errandId)));

        errand.getNotes().remove(noteToRemove);

        saveErrandAndUpdateProcess(errand);
    }
    //////////////////////////////
    // PATCH operations
    //////////////////////////////

    @Retry(name = "OptimisticLocking")
    public void patchErrand(Long id, PatchErrandDTO patchErrandDTO) {
        Errand oldErrand = getErrand(id);
        PatchMapper.INSTANCE.updateErrand(oldErrand, patchErrandDTO);
        saveErrandAndUpdateProcess(oldErrand);
    }

    @Retry(name = "OptimisticLocking")
    public Attachment patchErrand(Long id, AttachmentDTO attachmentDTO) {
        var oldErrand = getErrand(id);
        oldErrand.addAttachment(EntityDtoMapper.INSTANCE.dtoToAttachment(attachmentDTO));
        var result = saveErrandAndUpdateProcess(oldErrand).getAttachments();
        result.sort(Comparator.comparing(Attachment::getId).reversed());
        return result.get(0);
    }

    @Retry(name = "OptimisticLocking")
    public Stakeholder patchErrand(Long id, StakeholderDTO stakeholderDTO) {
        var oldErrand = getErrand(id);
        oldErrand.addStakeholder(EntityDtoMapper.INSTANCE.dtoToStakeholder(stakeholderDTO));
        var result = saveErrandAndUpdateProcess(oldErrand).getStakeholders();
        result.sort(Comparator.comparing(Stakeholder::getId).reversed());
        return result.get(0);
    }

    @Retry(name = "OptimisticLocking")
    public void patchErrand(Long id, StatusDTO statusDTO) {
        var oldErrand = getErrand(id);
        oldErrand.getStatuses().add(EntityDtoMapper.INSTANCE.dtoToStatus(statusDTO));
        saveErrandAndUpdateProcess(oldErrand);
    }

    @Retry(name = "OptimisticLocking")
    public Note patchErrand(Long id, NoteDTO noteDTO) {
        Errand oldErrand = getErrand(id);
        oldErrand.addNote(EntityDtoMapper.INSTANCE.dtoToNote(noteDTO));
        var result = saveErrandAndUpdateProcess(oldErrand).getNotes();
        result.sort(Comparator.comparing(Note::getId).reversed());
        return result.get(0);
    }

    @Retry(name = "OptimisticLocking")
    public Decision patchErrand(Long id, DecisionDTO decisionDTO) {
        var oldErrand = getErrand(id);
        oldErrand.addDecision(EntityDtoMapper.INSTANCE.dtoToDecision(decisionDTO));
        var result = saveErrandAndUpdateProcess(oldErrand).getDecisions();
        result.sort(Comparator.comparing(Decision::getId).reversed());
        return result.get(0);
    }

    @Retry(name = "OptimisticLocking")
    public void patchErrandWithMessage(Long id, List<String> messageIds) {
        var oldErrand = getErrand(id);
        oldErrand.getMessageIds().addAll(messageIds);
        saveErrandAndUpdateProcess(oldErrand);
    }

    /**
     * Use this method to save(update) existing errand and update the process in ProcessEngine
     */
    private Errand saveErrandAndUpdateProcess(Errand errand) {
        Errand result = errandRepository.save(errand);
        processEngineService.updateProcess(errand.getId());
        return result;
    }

    //////////////////////////////
    // PUT operations
    //////////////////////////////

    @Retry(name = "OptimisticLocking")
    public void putStatusesOnErrand(Long id, List<StatusDTO> statusDTOList) {
        var oldErrand = getErrand(id);
        oldErrand.getStatuses().clear();
        oldErrand.getStatuses().addAll(statusDTOList.stream().map(EntityDtoMapper.INSTANCE::dtoToStatus).toList());
        saveErrandAndUpdateProcess(oldErrand);
    }

    public void putAttachmentsOnErrand(Long id, List<AttachmentDTO> attachmentDTOList) {
        Errand oldErrand = getErrand(id);
        oldErrand.setAttachments(attachmentDTOList.stream().map(EntityDtoMapper.INSTANCE::dtoToAttachment).toList());
        saveErrandAndUpdateProcess(oldErrand);
    }

    public void putStakeholdersOnErrand(Long id, List<StakeholderDTO> stakeholderDTOList) {
        Errand oldErrand = getErrand(id);
        oldErrand.setStakeholders(stakeholderDTOList.stream().map(EntityDtoMapper.INSTANCE::dtoToStakeholder).toList());
        saveErrandAndUpdateProcess(oldErrand);
    }
}

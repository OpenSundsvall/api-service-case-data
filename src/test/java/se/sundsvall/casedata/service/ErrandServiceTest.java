package se.sundsvall.casedata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.turkraft.springfilter.boot.FilterSpecification;
import generated.client.processengine.ParkingPermitResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createAttachmentDTO;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.TestUtil.createStatusDTO;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderRole;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderType;

@ExtendWith(MockitoExtension.class)
class ErrandServiceTest {

    @Mock
    private ErrandRepository errandRepositoryMock;

    @Mock
    private ProcessEngineService processEngineServiceMock;

    @InjectMocks
    ErrandService errandService;

    @Captor
    private ArgumentCaptor<List<Long>> idListCapture;

    @Captor
    private ArgumentCaptor<Errand> errandCaptor;

    @Test
    void post() {
        ErrandDTO inputErrandDTO = createErrandDTO();
        Errand inputErrand = EntityDtoMapper.INSTANCE.dtoToErrand(inputErrandDTO);
        inputErrand.setId(new Random().nextLong(1, 1000));

        // Mock
        doReturn(inputErrand).when(errandRepositoryMock).save(any());
        ParkingPermitResponse parkingPermitResponse = new ParkingPermitResponse();
        parkingPermitResponse.setProcessId(UUID.randomUUID().toString());
        doReturn(parkingPermitResponse).when(processEngineServiceMock).startProcess(inputErrand.getId());

        errandService.saveErrandAndStartProcess(inputErrandDTO);

        verify(errandRepositoryMock, times(1)).save(inputErrand);
    }

    @Test
    void findById() {
        Errand errand = mockErrandFindById();

        errandService.findById(errand.getId());
        verify(errandRepositoryMock, times(1)).findById(errand.getId());
    }

    @Test
    void findByIdNotFound() {
        ErrandDTO errandDTO = createErrandDTO();
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(errandDTO);
        errand.setId(new Random().nextLong(1, 1000));
        doReturn(Optional.empty()).when(errandRepositoryMock).findById(any());

        Long id = errand.getId();
        ThrowableProblem problem = Assertions.assertThrows(ThrowableProblem.class, () -> errandService.findById(id));

        assertEquals(Status.NOT_FOUND, problem.getStatus());
        verify(errandRepositoryMock, times(1)).findById(errand.getId());
    }

    @Test
    void patchErrand() throws IOException {
        Errand errand = mockErrandFindById();

        PatchErrandDTO patchDTO = new PatchErrandDTO();
        patchDTO.setExternalCaseId(UUID.randomUUID().toString());
        patchDTO.setExtraParameters(createExtraParameters());

        Errand patchedErrand = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(errand), Errand.class);
        patchedErrand.setExternalCaseId(patchDTO.getExternalCaseId());
        patchedErrand.getExtraParameters().putAll(patchDTO.getExtraParameters());
        doReturn(patchedErrand).when(errandRepositoryMock).save(patchedErrand);

        errandService.patchErrand(errand.getId(), patchDTO);

        verify(errandRepositoryMock, times(1)).save(patchedErrand);
    }

    @Test
    void patchErrandWithStakeholder() throws JsonProcessingException {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Stakeholder stakeholder_1 = new Stakeholder();
        stakeholder_1.setId(1L);
        Stakeholder stakeholder_2 = new Stakeholder();
        stakeholder_2.setId(2L);
        errand.setStakeholders(List.of(stakeholder_1, stakeholder_2));
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

        StakeholderDTO patchStakeholderDTO = createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole()));

        Errand patchedErrandResult = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(errand), Errand.class);
        Stakeholder stakeholderResult = EntityDtoMapper.INSTANCE.dtoToStakeholder(patchStakeholderDTO);
        stakeholderResult.setId(3L);
        patchedErrandResult.getStakeholders().add(stakeholderResult);
        doReturn(patchedErrandResult).when(errandRepositoryMock).save(any());

        Stakeholder result = errandService.patchErrand(errand.getId(), patchStakeholderDTO);
        assertEquals(3L, result.getId());

        verify(errandRepositoryMock, times(1)).save(errandCaptor.capture());

        assertTrue(errandCaptor.getValue().getStakeholders().contains(EntityDtoMapper.INSTANCE.dtoToStakeholder(patchStakeholderDTO)));
    }

    @Test
    void deleteStakeholderOnErrand() {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        int sizeBeforeDelete = errand.getStakeholders().size();
        // Set ID on every stakeholder
        errand.getStakeholders().forEach(s -> s.setId(new Random().nextLong(1, 1000)));

        Long errandId = new Random().nextLong(1, 1000);
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(errandId);

        Stakeholder stakeholder = errand.getStakeholders().get(0);

        errandService.deleteStakeholderOnErrand(errandId, stakeholder.getId());

        ArgumentCaptor<Errand> errandCaptor = ArgumentCaptor.forClass(Errand.class);
        verify(errandRepositoryMock).save(errandCaptor.capture());
        Errand persistedErrand = errandCaptor.getValue();
        int sizeAfterDelete = persistedErrand.getStakeholders().size();

        assertTrue(sizeAfterDelete < sizeBeforeDelete);
        assertFalse(persistedErrand.getStakeholders().contains(stakeholder));
    }

    @Test
    void patchErrandWithStatus() throws JsonProcessingException {
        Errand errand = mockErrandFindById();

        var statusDTO = createStatusDTO();

        Errand patchedErrand = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(errand), Errand.class);
        patchedErrand.getStatuses().add(EntityDtoMapper.INSTANCE.dtoToStatus(statusDTO));
        doReturn(patchedErrand).when(errandRepositoryMock).save(patchedErrand);

        errandService.patchErrand(errand.getId(), statusDTO);

        verify(errandRepositoryMock, times(1)).save(patchedErrand);
    }

    @Test
    void putErrandStatuses() throws JsonProcessingException {
        Errand errand = mockErrandFindById();

        var statusDTOList = List.of(createStatusDTO(), createStatusDTO(), createStatusDTO());

        Errand patchedErrand = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(errand), Errand.class);
        patchedErrand.setStatuses(statusDTOList.stream().map(EntityDtoMapper.INSTANCE::dtoToStatus).toList());
        doReturn(patchedErrand).when(errandRepositoryMock).save(patchedErrand);

        errandService.putStatusesOnErrand(errand.getId(), statusDTOList);

        verify(errandRepositoryMock, times(1)).save(patchedErrand);
    }

    @Test
    void putAttachmentsOnErrand() {
        Errand errand = mockErrandFindById();
        List<AttachmentDTO> attachmentDTOList = List.of(createAttachmentDTO(AttachmentCategory.SIGNATURE), createAttachmentDTO(AttachmentCategory.PASSPORT_PHOTO), createAttachmentDTO(AttachmentCategory.MEDICAL_CONFIRMATION));
        errandService.putAttachmentsOnErrand(errand.getId(), attachmentDTOList);

        verify(errandRepositoryMock, times(1)).save(errandCaptor.capture());
        Errand persistedErrand = errandCaptor.getValue();
        assertEquals(attachmentDTOList.size(), persistedErrand.getAttachments().size());
        assertTrue(persistedErrand.getAttachments().containsAll(attachmentDTOList.stream().map(EntityDtoMapper.INSTANCE::dtoToAttachment).toList()));
    }

    @Test
    void putStakeholdersOnErrand() {
        Errand errand = mockErrandFindById();
        List<StakeholderDTO> stakeholderDTOList = List.of(createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())), createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())), createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())));
        errandService.putStakeholdersOnErrand(errand.getId(), stakeholderDTOList);

        verify(errandRepositoryMock, times(1)).save(errandCaptor.capture());
        Errand persistedErrand = errandCaptor.getValue();
        assertEquals(stakeholderDTOList.size(), persistedErrand.getStakeholders().size());
        assertTrue(persistedErrand.getStakeholders().containsAll(stakeholderDTOList.stream().map(EntityDtoMapper.INSTANCE::dtoToStakeholder).toList()));
    }

    @Test
    void patchErrandWithNote() throws JsonProcessingException {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Note note_1 = new Note();
        note_1.setId(1L);
        Note note_2 = new Note();
        note_2.setId(2L);
        errand.setNotes(List.of(note_1, note_2));
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

        NoteDTO patchNoteDTO = createNoteDTO();

        Errand patchedErrandResult = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(errand), Errand.class);
        Note noteResult = EntityDtoMapper.INSTANCE.dtoToNote(patchNoteDTO);
        noteResult.setId(3L);
        patchedErrandResult.getNotes().add(noteResult);
        doReturn(patchedErrandResult).when(errandRepositoryMock).save(any());

        Note result = errandService.patchErrand(errand.getId(), patchNoteDTO);
        assertEquals(3L, result.getId());

        verify(errandRepositoryMock, times(1)).save(errandCaptor.capture());

        assertTrue(errandCaptor.getValue().getNotes().contains(EntityDtoMapper.INSTANCE.dtoToNote(patchNoteDTO)));
    }

    @Test
    void patchErrandWithMessage() throws JsonProcessingException {
        Errand errand = mockErrandFindById();

        Errand patchedErrand = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(errand), Errand.class);
        String message = RandomStringUtils.random(10, true, false);
        patchedErrand.getMessageIds().add(message);
        doReturn(patchedErrand).when(errandRepositoryMock).save(patchedErrand);

        errandService.patchErrandWithMessage(errand.getId(), List.of(message));

        verify(errandRepositoryMock, times(1)).save(patchedErrand);
    }

    @Test
    void findAllWithoutDuplicates() {
        ErrandDTO errandDTO = createErrandDTO();
        List<Errand> returnErrands = Stream.of(errandDTO, errandDTO, errandDTO, errandDTO, errandDTO)
                .map(EntityDtoMapper.INSTANCE::dtoToErrand)
                .toList();

        doReturn(returnErrands).when(errandRepositoryMock).findAll(any(Specification.class));
        doReturn(new PageImpl<>(List.of(returnErrands.get(0)))).when(errandRepositoryMock).findAllByIdIn(anyList(), any(Pageable.class));

        Specification<Errand> specification = new FilterSpecification<>("(stakeholders.firstName ~ '*kim*' or stakeholders.lastName ~ '*kim*' or stakeholders.contactInformation.value ~ '*kim*')");
        Pageable pageable = PageRequest.of(0, 20);
        errandService.findAll(specification, new HashMap<>(), pageable);

        verify(errandRepositoryMock, times(1)).findAllByIdIn(idListCapture.capture(), any(Pageable.class));

        assertEquals(1, idListCapture.getValue().size());
        assertEquals(errandDTO.getId(), idListCapture.getValue().get(0));
    }

    @Test
    void patchErrandWithAttachment() throws IOException {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Attachment attachment_1 = new Attachment();
        attachment_1.setId(1L);
        Attachment attachment_2 = new Attachment();
        attachment_2.setId(2L);
        errand.setAttachments(List.of(attachment_1, attachment_2));
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

        AttachmentDTO patchAttachmentDTO = createAttachmentDTO(AttachmentCategory.SIGNATURE);

        Errand patchedErrandResult = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(errand), Errand.class);
        Attachment attachmentResult = EntityDtoMapper.INSTANCE.dtoToAttachment(patchAttachmentDTO);
        attachmentResult.setId(3L);
        patchedErrandResult.getAttachments().add(attachmentResult);
        doReturn(patchedErrandResult).when(errandRepositoryMock).save(any());

        Attachment result = errandService.patchErrand(errand.getId(), patchAttachmentDTO);
        assertEquals(3L, result.getId());

        verify(errandRepositoryMock, times(1)).save(errandCaptor.capture());

        assertTrue(errandCaptor.getValue().getAttachments().contains(EntityDtoMapper.INSTANCE.dtoToAttachment(patchAttachmentDTO)));
    }

    @Test
    void deleteAttachmentOnErrand() {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        int attachmentSizeBeforeDelete = errand.getAttachments().size();
        // Set ID on every attachment
        errand.getAttachments().forEach(a -> a.setId(new Random().nextLong(1, 1000)));

        Long errandId = new Random().nextLong(1, 1000);
        Attachment attachment = errand.getAttachments().get(0);

        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(errandId);

        errandService.deleteAttachmentOnErrand(errandId, attachment.getId());

        ArgumentCaptor<Errand> errandCaptor = ArgumentCaptor.forClass(Errand.class);
        verify(errandRepositoryMock).save(errandCaptor.capture());
        Errand persistedErrand = errandCaptor.getValue();
        int attachmentSizeAfterDelete = persistedErrand.getAttachments().size();

        assertTrue(attachmentSizeAfterDelete < attachmentSizeBeforeDelete);
        assertFalse(persistedErrand.getAttachments().contains(attachment));
    }

    @Test
    void getDecisionsOnErrand() {
        Errand errand = mockErrandFindById();
        var result = errandService.findDecisionsOnErrand(errand.getId());
        assertEquals(errand.getDecisions().stream().map(EntityDtoMapper.INSTANCE::decisionToDto).toList(), result);
    }

    @Test
    void getDecisionsOnErrandNotFound() {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        errand.setDecisions(new ArrayList<>());
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

        var id = errand.getId();
        Assertions.assertThrows(ThrowableProblem.class, () -> errandService.findDecisionsOnErrand(id));
    }

    @Test
    void patchErrandWithDecision() throws JsonProcessingException {

        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Decision decision_1 = new Decision();
        decision_1.setId(1L);
        Decision decision_2 = new Decision();
        decision_2.setId(2L);
        errand.setDecisions(List.of(decision_1, decision_2));
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

        DecisionDTO patchDecisionDTO = createDecisionDTO();

        Errand patchedErrandResult = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(errand), Errand.class);
        Decision decisionResult = EntityDtoMapper.INSTANCE.dtoToDecision(patchDecisionDTO);
        decisionResult.setId(3L);
        patchedErrandResult.getDecisions().add(decisionResult);
        doReturn(patchedErrandResult).when(errandRepositoryMock).save(any());

        Decision result = errandService.patchErrand(errand.getId(), patchDecisionDTO);
        assertEquals(3L, result.getId());

        verify(errandRepositoryMock, times(1)).save(errandCaptor.capture());

        assertTrue(errandCaptor.getValue().getDecisions().contains(EntityDtoMapper.INSTANCE.dtoToDecision(patchDecisionDTO)));
    }

    @Test
    void deleteDecisionOnErrand() {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        int sizeBeforeDelete = errand.getDecisions().size();
        // Set ID on every decision
        errand.getDecisions().forEach(d -> d.setId(new Random().nextLong()));

        Long errandId = new Random().nextLong(1, 1000);
        Decision decision = errand.getDecisions().get(0);

        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(errandId);

        errandService.deleteDecisionOnErrand(errandId, decision.getId());

        ArgumentCaptor<Errand> errandCaptor = ArgumentCaptor.forClass(Errand.class);
        verify(errandRepositoryMock).save(errandCaptor.capture());
        Errand persistedErrand = errandCaptor.getValue();
        int sizeAfterDelete = persistedErrand.getDecisions().size();

        assertTrue(sizeAfterDelete < sizeBeforeDelete);
        assertFalse(persistedErrand.getDecisions().contains(decision));
    }

    @Test
    void deleteNoteOnErrand() {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        int sizeBeforeDelete = errand.getNotes().size();
        // Set ID on every note
        errand.getNotes().forEach(note -> note.setId(new Random().nextLong()));

        Long errandId = new Random().nextLong(1, 1000);
        Note note = errand.getNotes().get(0);

        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(errandId);

        errandService.deleteNoteOnErrand(errandId, note.getId());

        ArgumentCaptor<Errand> errandCaptor = ArgumentCaptor.forClass(Errand.class);
        verify(errandRepositoryMock).save(errandCaptor.capture());
        Errand persistedErrand = errandCaptor.getValue();
        int sizeAfterDelete = persistedErrand.getNotes().size();

        assertTrue(sizeAfterDelete < sizeBeforeDelete);
        assertFalse(persistedErrand.getNotes().contains(note));
    }

    private Errand mockErrandFindById() {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());
        return errand;
    }
}

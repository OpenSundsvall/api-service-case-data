package se.sundsvall.casedata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createAttachmentDTO;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.getRandomOfEnum;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    @Captor
    private ArgumentCaptor<Attachment> attachmentArgumentCaptor;

    @Test
    void testFindById() {
        Long id = new Random().nextLong();
        var attachment = EntityDtoMapper.INSTANCE.dtoToAttachment(createAttachmentDTO((AttachmentCategory) getRandomOfEnum(AttachmentCategory.class)));
        doReturn(Optional.of(attachment)).when(attachmentRepository).findById(id);

        var result = attachmentService.findById(id);
        assertEquals(EntityDtoMapper.INSTANCE.attachmentToDto(attachment), result);

        verify(attachmentRepository, times(1)).findById(id);
    }

    @Test
    void testFindByIdNotFound() {
        Long id = new Random().nextLong();
        doReturn(Optional.empty()).when(attachmentRepository).findById(id);

        var problem = assertThrows(ThrowableProblem.class, () -> attachmentService.findById(id));

        assertEquals(Status.NOT_FOUND, problem.getStatus());
        verify(attachmentRepository, times(1)).findById(id);
    }

    @Test
    void putAttachment() throws JsonProcessingException {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Attachment attachment = EntityDtoMapper.INSTANCE.dtoToAttachment(createAttachmentDTO((AttachmentCategory) getRandomOfEnum(AttachmentCategory.class)));
        attachment.setId(new Random().nextLong());
        errand.addAttachment(attachment);

        var mockAttachment = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(attachment), Attachment.class);
        mockAttachment.setErrand(errand);
        doReturn(Optional.of(mockAttachment)).when(attachmentRepository).findById(any());

        AttachmentDTO putDTO = createAttachmentDTO((AttachmentCategory) getRandomOfEnum(AttachmentCategory.class));

        attachmentService.put(attachment.getId(), putDTO);
        verify(attachmentRepository).save(attachmentArgumentCaptor.capture());
        Attachment persistedAttachment = attachmentArgumentCaptor.getValue();

        assertThat(putDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        "id", "version", "created", "updated")
                .isEqualTo(EntityDtoMapper.INSTANCE.attachmentToDto(persistedAttachment));
    }
}

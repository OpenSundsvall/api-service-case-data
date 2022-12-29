package se.sundsvall.casedata.service;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;
import se.sundsvall.casedata.service.util.mappers.PutMapper;

import javax.transaction.Transactional;

@Service
@Transactional
public class AttachmentService {

    private static final ThrowableProblem ATTACHMENT_NOT_FOUND_PROBLEM = Problem.valueOf(Status.NOT_FOUND, "Attachment not found");
    private final AttachmentRepository attachmentRepository;

    private final Javers javers;

    public AttachmentService(AttachmentRepository attachmentRepository, Javers javers) {
        this.attachmentRepository = attachmentRepository;
        this.javers = javers;
    }

    public AttachmentDTO findById(Long id) {
        return EntityDtoMapper.INSTANCE.attachmentToDto(getAttachment(id));
    }

    public String findHistory(Long id) {
        QueryBuilder query = QueryBuilder.byInstanceId(id, Attachment.class).withChildValueObjects();
        Changes changes = javers.findChanges(query.build());
        if (changes.isEmpty()) {
            throw ATTACHMENT_NOT_FOUND_PROBLEM;
        }
        return javers.getJsonConverter().toJson(changes);
    }

    private Attachment getAttachment(Long id) {
        return attachmentRepository.findById(id).orElseThrow(() -> ATTACHMENT_NOT_FOUND_PROBLEM);
    }

    public void put(Long attachmentId, AttachmentDTO attachmentDTO) {
        var attachment = getAttachment(attachmentId);
        PutMapper.INSTANCE.putAttachment(attachment, attachmentDTO);
        attachmentRepository.save(attachment);
    }
}

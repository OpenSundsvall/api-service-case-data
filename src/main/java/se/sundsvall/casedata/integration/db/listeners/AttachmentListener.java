package se.sundsvall.casedata.integration.db.listeners;

import org.springframework.stereotype.Component;
import se.sundsvall.casedata.integration.db.model.Attachment;

import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Component
public class AttachmentListener {

    private final ErrandListener errandListener;

    public AttachmentListener(ErrandListener errandListener) {
        this.errandListener = errandListener;
    }

    @PostPersist
    private void postPersist(Attachment attachment) {
        errandListener.updateErrandFields(attachment.getErrand());
    }

    @PreUpdate
    @PreRemove
    private void preUpdate(Attachment attachment) {
        errandListener.updateErrandFields(attachment.getErrand());
    }
}

package se.sundsvall.casedata.integration.db.listeners;

import org.springframework.stereotype.Component;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.model.Note;

import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Component
public class NoteListener {

    private final IncomingRequestFilter incomingRequestFilter;
    private final ErrandListener errandListener;

    public NoteListener(IncomingRequestFilter incomingRequestFilter, ErrandListener errandListener) {
        this.incomingRequestFilter = incomingRequestFilter;
        this.errandListener = errandListener;
    }

    @PostPersist
    private void postPersist(Note note) {
        note.setCreatedBy(incomingRequestFilter.getAdUser());
        errandListener.updateErrandFields(note.getErrand());
    }

    @PreUpdate
    @PreRemove
    private void preUpdate(Note note) {
        note.setUpdatedBy(incomingRequestFilter.getAdUser());
        errandListener.updateErrandFields(note.getErrand());
    }

}

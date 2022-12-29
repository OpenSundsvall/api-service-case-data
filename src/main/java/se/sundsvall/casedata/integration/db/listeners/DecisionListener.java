package se.sundsvall.casedata.integration.db.listeners;

import org.springframework.stereotype.Component;
import se.sundsvall.casedata.integration.db.model.Decision;

import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Component
public class DecisionListener {

    private final ErrandListener errandListener;

    public DecisionListener(ErrandListener errandListener) {
        this.errandListener = errandListener;
    }

    @PostPersist
    private void postPersist(Decision decision) {
        errandListener.updateErrandFields(decision.getErrand());
    }

    @PreUpdate
    @PreRemove
    private void preUpdate(Decision decision) {
        errandListener.updateErrandFields(decision.getErrand());
    }
}

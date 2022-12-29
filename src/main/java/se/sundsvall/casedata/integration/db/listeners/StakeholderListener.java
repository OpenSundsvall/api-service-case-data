package se.sundsvall.casedata.integration.db.listeners;

import org.springframework.stereotype.Component;
import se.sundsvall.casedata.integration.db.model.Stakeholder;

import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Component
public class StakeholderListener {

    private final ErrandListener errandListener;

    public StakeholderListener(ErrandListener errandListener) {
        this.errandListener = errandListener;
    }

    @PostPersist
    private void postPersist(Stakeholder stakeholder) {
        errandListener.updateErrandFields(stakeholder.getErrand());
    }

    @PreUpdate
    @PreRemove
    private void preUpdate(Stakeholder stakeholder) {
        errandListener.updateErrandFields(stakeholder.getErrand());
    }
}

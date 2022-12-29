package se.sundsvall.casedata.integration.db.listeners;

import org.springframework.stereotype.Component;
import se.sundsvall.casedata.integration.db.model.Facility;

import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Component
public class FacilityListener {

    private final ErrandListener errandListener;

    public FacilityListener(ErrandListener errandListener) {
        this.errandListener = errandListener;
    }

    @PostPersist
    private void postPersist(Facility facility) {
        errandListener.updateErrandFields(facility.getErrand());
    }

    @PreUpdate
    @PreRemove
    private void preUpdate(Facility facility) {
        errandListener.updateErrandFields(facility.getErrand());
    }
}

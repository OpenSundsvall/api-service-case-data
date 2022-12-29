package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.casedata.integration.db.listeners.ErrandListener;
import se.sundsvall.casedata.integration.db.model.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity(name = "errand")
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_errand_errand_number", columnNames = { "errand_number" }) })
@EntityListeners(ErrandListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Errand extends BaseEntity {

    @Column(name = "errand_number", nullable = false)
    private String errandNumber;

    @Column(name = "external_case_id")
    private String externalCaseId;

    @Column(name = "case_type")
    @Enumerated(EnumType.STRING)
    private CaseType caseType;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "description")
    private String description;

    @Column(name = "case_title_addition")
    private String caseTitleAddition;

    @Column(name = "diary_number")
    private String diaryNumber;

    @Column(name = "phase")
    private String phase;

    @ElementCollection
    @CollectionTable(name = "errand_statuses",
            joinColumns = @JoinColumn(name = "errand_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_errand_statuses_errand_id")))
    @OrderColumn(name = "status_order")
    private List<Status> statuses = new ArrayList<>();

    @Column(name = "municipality_id")
    private String municipalityId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "application_received")
    private OffsetDateTime applicationReceived;

    @DiffIgnore
    @Column(name = "process_id")
    private String processId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
    @JsonManagedReference
    private List<Stakeholder> stakeholders = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
    @JsonManagedReference
    private List<Facility> facilities = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
    @JsonManagedReference
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
    @JsonManagedReference
    private List<Decision> decisions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
    @JsonManagedReference
    private List<Note> notes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "errand_message_ids",
            joinColumns = @JoinColumn(name = "errand_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_errand_message_ids_errand_id")))
    private List<String> messageIds = new ArrayList<>();

    // WSO2-client
    @Column(name = "created_by_client")
    @DiffIgnore
    private String createdByClient;

    // WSO2-client
    @Column(name = "updated_by_client")
    @DiffIgnore
    private String updatedByClient;

    // AD-user
    @Column(name = "created_by")
    @Size(max = 36)
    @DiffIgnore
    private String createdBy;

    // AD-user
    @Column(name = "updated_by")
    @Size(max = 36)
    @DiffIgnore
    private String updatedBy;

    @ElementCollection
    @CollectionTable(name = "errand_extra_parameters",
            joinColumns = @JoinColumn(name = "errand_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_errand_extra_parameters_errand_id")))
    @MapKeyColumn(name = "extra_parameter_key")
    @Column(name = "extra_parameter_value")
    private Map<String, String> extraParameters = new HashMap<>();

    public void addStakeholder(Stakeholder stakeholder) {
        stakeholders.add(stakeholder);
        stakeholder.setErrand(this);
    }

    public void removeStakeholder(Stakeholder stakeholder) {
        stakeholders.remove(stakeholder);
        stakeholder.setErrand(null);
    }

    public void setStakeholders(List<Stakeholder> stakeholders) {
        this.stakeholders.clear();
        if (stakeholders != null) {
            stakeholders.forEach(this::addStakeholder);
        }
    }

    public void addFacility(Facility facility) {
        facilities.add(facility);
        facility.setErrand(this);
    }

    public void removeFacility(Facility facility) {
        facilities.remove(facility);
        facility.setErrand(null);
    }

    public void setFacilities(List<Facility> facilities) {
        this.facilities.clear();
        if (facilities != null) {
            facilities.forEach(this::addFacility);
        }
    }

    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        attachment.setErrand(this);
    }

    public void removeAttachment(Attachment attachment) {
        attachments.remove(attachment);
        attachment.setErrand(null);
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments.clear();
        if (attachments != null) {
            attachments.forEach(this::addAttachment);
        }
    }

    public void addDecision(Decision decision) {
        decisions.add(decision);
        decision.setErrand(this);
    }

    public void removeDecision(Decision decision) {
        decisions.remove(decision);
        decision.setErrand(null);
    }

    public void setDecisions(List<Decision> decisions) {
        this.decisions.clear();
        if (decisions != null) {
            decisions.forEach(this::addDecision);
        }
    }

    public void addNote(Note note) {
        notes.add(note);
        note.setErrand(this);
    }

    public void removeNote(Note note) {
        notes.remove(note);
        note.setErrand(null);
    }

    public void setNotes(List<Note> notes) {
        this.notes.clear();
        if (notes != null) {
            notes.forEach(this::addNote);
        }
    }

    @Override
    public String toString() {
        return "Errand{" +
                "errandNumber='" + errandNumber + '\'' +
                ", externalCaseId='" + externalCaseId + '\'' +
                ", caseType=" + caseType +
                ", priority=" + priority +
                ", description='" + description + '\'' +
                ", caseTitleAddition='" + caseTitleAddition + '\'' +
                ", diaryNumber='" + diaryNumber + '\'' +
                ", phase='" + phase + '\'' +
                ", statuses=" + statuses +
                ", municipalityId='" + municipalityId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", applicationReceived=" + applicationReceived +
                ", processId='" + processId + '\'' +
                ", stakeholders=" + stakeholders +
                ", facilities=" + facilities +
                ", attachments=" + attachments +
                ", decisions=" + decisions +
                ", notes=" + notes +
                ", messageIds=" + messageIds +
                ", createdByClient='" + createdByClient + '\'' +
                ", updatedByClient='" + updatedByClient + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", extraParameters=" + extraParameters +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Errand errand)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(errandNumber, errand.errandNumber) && Objects.equals(externalCaseId, errand.externalCaseId) && caseType == errand.caseType && priority == errand.priority && Objects.equals(description, errand.description) && Objects.equals(caseTitleAddition, errand.caseTitleAddition) && Objects.equals(diaryNumber, errand.diaryNumber) && Objects.equals(phase, errand.phase) && Objects.equals(statuses, errand.statuses) && Objects.equals(municipalityId, errand.municipalityId) && Objects.equals(startDate, errand.startDate) && Objects.equals(endDate, errand.endDate) && Objects.equals(applicationReceived, errand.applicationReceived) && Objects.equals(processId, errand.processId) && Objects.equals(stakeholders, errand.stakeholders) && Objects.equals(facilities, errand.facilities) && Objects.equals(attachments, errand.attachments) && Objects.equals(decisions, errand.decisions) && Objects.equals(notes, errand.notes) && Objects.equals(messageIds, errand.messageIds) && Objects.equals(createdByClient, errand.createdByClient) && Objects.equals(updatedByClient, errand.updatedByClient) && Objects.equals(createdBy, errand.createdBy) && Objects.equals(updatedBy, errand.updatedBy) && Objects.equals(extraParameters, errand.extraParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), errandNumber, externalCaseId, caseType, priority, description, caseTitleAddition, diaryNumber, phase, statuses, municipalityId, startDate, endDate, applicationReceived, processId, stakeholders, facilities, attachments, decisions, notes, messageIds, createdByClient, updatedByClient, createdBy, updatedBy, extraParameters);
    }

}

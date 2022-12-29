package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.casedata.integration.db.listeners.DecisionListener;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

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
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity(name = "decision")
@EntityListeners(DecisionListener.class)
@Getter
@Setter
public class Decision extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_decision_errand_id"))
    @JsonBackReference
    private Errand errand;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type")
    private DecisionType decisionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_outcome")
    private DecisionOutcome decisionOutcome;

    @Column(name = "description")
    @Size(max = 100000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "decision_laws",
            joinColumns = @JoinColumn(name = "decision_id", foreignKey = @ForeignKey(name = "FK_decision_laws_decision_id")))
    @OrderColumn(name = "law_order")
    private List<Law> law = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "decided_by_id", foreignKey = @ForeignKey(name = "FK_decision_decided_by_id"))
    private Stakeholder decidedBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "valid_from")
    private OffsetDateTime validFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "valid_to")
    private OffsetDateTime validTo;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "appeal_id", foreignKey = @ForeignKey(name = "FK_decision_appeal_id"))
    private Appeal appeal;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "decision_id", foreignKey = @ForeignKey(name = "FK_decision_id"))
    private List<Attachment> attachments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "decision_extra_parameters",
            joinColumns = @JoinColumn(name = "decision_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_decision_extra_parameters_decision_id")))
    @MapKeyColumn(name = "extra_parameter_key")
    @Column(name = "extra_parameter_value")
    private Map<String, String> extraParameters = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Decision decision)) return false;
        if (!super.equals(o)) return false;
        return decisionType == decision.decisionType && decisionOutcome == decision.decisionOutcome && Objects.equals(description, decision.description) && Objects.equals(law, decision.law) && Objects.equals(decidedBy, decision.decidedBy) && Objects.equals(decidedAt, decision.decidedAt) && Objects.equals(validFrom, decision.validFrom) && Objects.equals(validTo, decision.validTo) && Objects.equals(appeal, decision.appeal) && Objects.equals(attachments, decision.attachments) && Objects.equals(extraParameters, decision.extraParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), decisionType, decisionOutcome, description, law, decidedBy, decidedAt, validFrom, validTo, appeal, attachments, extraParameters);
    }

    @Override
    public String toString() {
        long errandId = errand == null ? 0 : errand.getId();

        return "Decision{" +
                "errand.id=" + errandId +
                ", decisionType=" + decisionType +
                ", decisionOutcome=" + decisionOutcome +
                ", description='" + description + '\'' +
                ", law=" + law +
                ", decidedBy=" + decidedBy +
                ", decidedAt=" + decidedAt +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", appeal=" + appeal +
                ", attachments=" + attachments +
                ", extraParameters=" + extraParameters +
                "} " + super.toString();
    }

}

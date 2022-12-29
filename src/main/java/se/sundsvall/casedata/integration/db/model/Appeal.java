package se.sundsvall.casedata.integration.db.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity(name = "appeal")
@Getter
@Setter
public class Appeal extends BaseEntity {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "appealed_by_id", foreignKey = @ForeignKey(name = "FK_appeal_appealed_by_id"))
    private Stakeholder appealedBy;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "judicial_authorisation_id", foreignKey = @ForeignKey(name = "FK_appeal_judicial_authorisation_id"))
    private Stakeholder judicialAuthorisation;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "appeal_id", foreignKey = @ForeignKey(name = "FK_appeal_id"))
    private List<Attachment> attachments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "appeal_extra_parameters",
            joinColumns = @JoinColumn(name = "appeal_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_appeal_extra_parameters_appeal_id")))
    @MapKeyColumn(name = "extra_parameter_key")
    @Column(name = "extra_parameter_value")
    private Map<String, String> extraParameters = new HashMap<>();

    @Override
    public String toString() {
        return "Appeal{" +
                "appealedBy=" + appealedBy +
                ", judicialAuthorisation=" + judicialAuthorisation +
                ", attachments=" + attachments +
                ", extraParameters=" + extraParameters +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appeal appeal)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(appealedBy, appeal.appealedBy) && Objects.equals(judicialAuthorisation, appeal.judicialAuthorisation) && Objects.equals(attachments, appeal.attachments) && Objects.equals(extraParameters, appeal.extraParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), appealedBy, judicialAuthorisation, attachments, extraParameters);
    }

}

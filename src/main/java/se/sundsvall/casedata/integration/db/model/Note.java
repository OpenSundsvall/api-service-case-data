package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.DiffIgnore;
import se.sundsvall.casedata.integration.db.listeners.NoteListener;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity(name = "note")
@EntityListeners(NoteListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Note extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_note_errand_id"))
    @JsonBackReference
    private Errand errand;

    @Column(name = "title")
    private String title;

    @Column(name = "text")
    @Size(max = 10000)
    private String text;

    @Column(name = "created_by")
    @Size(max = 36)
    @DiffIgnore
    private String createdBy;

    @Column(name = "updated_by")
    @Size(max = 36)
    @DiffIgnore
    private String updatedBy;

    @ElementCollection
    @CollectionTable(name = "note_extra_parameters",
            joinColumns = @JoinColumn(name = "note_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_note_extra_parameters_note_id")))
    @MapKeyColumn(name = "extra_parameter_key")
    @Column(name = "extra_parameter_value")
    private Map<String, String> extraParameters = new HashMap<>();

    @Override
    public String toString() {
        long errandId = errand == null ? 0 : errand.getId();
        return "Note{" +
                "errand.id=" + errandId +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", extraParameters=" + extraParameters +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note note)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(title, note.title) && Objects.equals(text, note.text) && Objects.equals(createdBy, note.createdBy) && Objects.equals(updatedBy, note.updatedBy) && Objects.equals(extraParameters, note.extraParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, text, createdBy, updatedBy, extraParameters);
    }

}

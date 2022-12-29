package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import se.sundsvall.casedata.integration.db.listeners.AttachmentListener;
import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity(name = "attachment")
@EntityListeners(AttachmentListener.class)
@Getter
@Setter
public class Attachment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_attachment_errand_id"))
    @JsonBackReference
    private Errand errand;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private AttachmentCategory category;

    @Column(name = "name")
    private String name;

    @Column(name = "note")
    @Size(max = 1000)
    private String note;

    @Column(name = "extension")
    private String extension;

    @Column(name = "mime_type")
    private String mimeType;

    @Lob
    @Column(name = "file")
    private String file;

    @ElementCollection
    @CollectionTable(name = "attachment_extra_parameters",
            joinColumns = @JoinColumn(name = "attachment_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_attachment_extra_parameters_attachment_id")))
    @MapKeyColumn(name = "extra_parameter_key")
    @Column(name = "extra_parameter_value")
    private Map<String, String> extraParameters = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attachment that)) return false;
        if (!super.equals(o)) return false;
        return category == that.category && Objects.equals(name, that.name) && Objects.equals(note, that.note) && Objects.equals(extension, that.extension) && Objects.equals(mimeType, that.mimeType) && Objects.equals(file, that.file) && Objects.equals(extraParameters, that.extraParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), category, name, note, extension, mimeType, file, extraParameters);
    }

    @Override
    public String toString() {
        long errandId = errand == null ? 0 : errand.getId();

        return "Attachment{" +
                "errand.id=" + errandId +
                ", category=" + category +
                ", name='" + name + '\'' +
                ", note='" + note + '\'' +
                ", extension='" + extension + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", file='" + file + '\'' +
                ", extraParameters=" + extraParameters +
                "} " + super.toString();
    }
}

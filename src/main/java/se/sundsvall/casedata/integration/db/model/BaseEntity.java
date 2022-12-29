package se.sundsvall.casedata.integration.db.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.time.OffsetDateTime;
import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    @DiffIgnore
    private Long id;
    @Version
    @Column(name="version")
    @DiffIgnore
    private int version;
    @CreationTimestamp
    @Column(name="created")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @DiffIgnore
    private OffsetDateTime created;
    @UpdateTimestamp
    @Column(name="updated")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @DiffIgnore
    private OffsetDateTime updated;

    @Override
    public String toString() {
        return "BaseEntity{" +
                "id=" + id +
                ", version=" + version +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity that)) return false;
        return version == that.version && Objects.equals(id, that.id) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, created, updated);
    }

}

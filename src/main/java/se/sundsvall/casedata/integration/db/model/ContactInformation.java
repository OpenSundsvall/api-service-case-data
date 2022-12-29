package se.sundsvall.casedata.integration.db.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;

@Getter
@Setter
@ToString
@Embeddable
public class ContactInformation {
    @Enumerated(EnumType.STRING)
    @Column(name="contact_type")
    private ContactType contactType;

    @Column(name="value")
    private String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactInformation that)) return false;
        return contactType == that.contactType && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactType, value);
    }
}

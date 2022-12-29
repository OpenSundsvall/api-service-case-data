package se.sundsvall.casedata.integration.db.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.OffsetDateTime;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@ToString
public class Status {

    @Column(name = "status_type")
    private String statusType;

    @Column(name = "description")
    private String description;

    @Column(name = "date_time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime dateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Status status)) return false;
        return Objects.equals(statusType, status.statusType) && Objects.equals(description, status.description) && Objects.equals(dateTime, status.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusType, description, dateTime);
    }
}

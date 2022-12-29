package se.sundsvall.casedata.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContactInformationDTO {
    private ContactType contactType;
    @Size(max = 255)
    private String value;

}

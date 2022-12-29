package se.sundsvall.casedata.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO  extends BaseDTO {

    @Enumerated(EnumType.STRING)
    private AttachmentCategory category;

    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String note;

    @Size(max = 255)
    private String extension;

    @Size(max = 255)
    private String mimeType;

    private String file;

    private Map<String, String> extraParameters = new HashMap<>();

}

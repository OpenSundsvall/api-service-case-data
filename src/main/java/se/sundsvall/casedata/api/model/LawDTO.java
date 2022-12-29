package se.sundsvall.casedata.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LawDTO {

    // Rubrik
    @Size(max = 255)
    private String heading;
    // Svensk författningssamling, (SFS)
    @Size(max = 255)
    private String sfs;
    // kapitel
    @Size(max = 255)
    private String chapter;
    // paragraf
    @Size(max = 255)
    private String article;

}

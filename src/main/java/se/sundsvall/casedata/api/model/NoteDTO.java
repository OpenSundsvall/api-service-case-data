package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class NoteDTO extends BaseDTO {

    @Schema(example = "Motivering till bifall")
    @Size(max = 255)
    private String title;

    @Schema(example = "Den sökande har rätt till parkeringstillstånd eftersom alla kriterier uppfylls.")
    @Size(max = 10000)
    private String text;

    @Schema(description = "AD-account for the user who created the note.", example = "user",
        accessMode = Schema.AccessMode.READ_ONLY)
    @Size(max = 36)
    private String createdBy;

    @Schema(description = "AD-account for the user who last modified the note.", example = "user",
        accessMode = Schema.AccessMode.READ_ONLY)
    @Size(max = 36)
    private String updatedBy;

    private Map<String, String> extraParameters = new HashMap<>();

}

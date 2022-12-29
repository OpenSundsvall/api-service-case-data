package se.sundsvall.casedata.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppealDTO extends BaseDTO {

    private StakeholderDTO appealedBy;

    private StakeholderDTO judicialAuthorisation;

    private List<AttachmentDTO> attachments = new ArrayList<>();

    private Map<String, String> extraParameters = new HashMap<>();
}

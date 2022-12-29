package se.sundsvall.casedata.api.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ExtraParameterDTO {
    private Map<String, String> extraParameters = new HashMap<>();
}

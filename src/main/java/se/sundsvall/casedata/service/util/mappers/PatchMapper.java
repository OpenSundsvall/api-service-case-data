package se.sundsvall.casedata.service.util.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;

import java.util.Map;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PatchMapper {

    PatchMapper INSTANCE = Mappers.getMapper(PatchMapper.class);

    /**
     * Works like a PATCH-operation. Ignore fields with null value.
     */
    @Mapping(target = "extraParameters", qualifiedByName = "extraParameterMapping")
    void updateErrand(@MappingTarget Errand oldErrand, PatchErrandDTO patchErrandDTO);

    /**
     * Works like a PATCH-operation. Ignore fields with null value.
     */
    @Mapping(target = "extraParameters", qualifiedByName = "extraParameterMapping")
    void updateDecision(@MappingTarget Decision old, PatchDecisionDTO patch);

    /**
     * Works like a PATCH-operation. Ignore fields with null value.
     */
    @Mapping(target = "extraParameters", qualifiedByName = "extraParameterMapping")
    void updateStakeholder(@MappingTarget Stakeholder old, StakeholderDTO patch);

    /**
     * Works like a PATCH-operation. Ignore fields with null value.
     */
    @Mapping(target = "extraParameters", qualifiedByName = "extraParameterMapping")
    void updateNote(@MappingTarget Note old, NoteDTO patch);

    @Named("extraParameterMapping")
    default Map<String, String> extraParameterMapping(@MappingTarget Map<String, String> target, Map<String, String> source) {
        target.putAll(source);

        return target;
    }
}

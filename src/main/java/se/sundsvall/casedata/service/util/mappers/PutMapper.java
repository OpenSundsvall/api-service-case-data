package se.sundsvall.casedata.service.util.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.model.Appeal;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;

import java.util.Map;

/**
 * Works like a PUT-operation.
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface PutMapper {

    PutMapper INSTANCE = Mappers.getMapper(PutMapper.class);

    @Mapping(target = "extraParameters", qualifiedByName = "extraParameterMapping")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "version", ignore = true)
    void putAttachment(@MappingTarget Attachment oldAttachment, AttachmentDTO newAttachment);

    @Mapping(target = "extraParameters", qualifiedByName = "extraParameterMapping")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "errand", ignore = true)
    void putStakeholder(@MappingTarget Stakeholder oldStakeholder, StakeholderDTO newStakeholder);

    @Mapping(target = "extraParameters", qualifiedByName = "extraParameterMapping")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "version", ignore = true)
    void putDecision(@MappingTarget Decision oldDecision, DecisionDTO newDecision);

    @Mapping(target = "extraParameters", qualifiedByName = "extraParameterMapping")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "version", ignore = true)
    void putAppeal(@MappingTarget Appeal old, AppealDTO newDTO);

    @Mapping(target = "extraParameters", qualifiedByName = "extraParameterMapping")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "version", ignore = true)
    void putNote(@MappingTarget Note old, NoteDTO newNoteDTO);

    @Named("extraParameterMapping")
    default Map<String, String> extraParameterMapping(@MappingTarget Map<String, String> target, Map<String, String> source) {
        target.clear();
        target.putAll(source);

        return target;
    }
}

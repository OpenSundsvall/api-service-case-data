package se.sundsvall.casedata.service.util.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.StatusDTO;
import se.sundsvall.casedata.integration.db.model.Appeal;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Facility;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.Status;

@Mapper
public interface EntityDtoMapper {
    EntityDtoMapper INSTANCE = Mappers.getMapper(EntityDtoMapper.class);

    ErrandDTO errandToDto(Errand errand);

    Errand dtoToErrand(ErrandDTO errandDTO);

    Stakeholder dtoToStakeholder(StakeholderDTO stakeholderDTO);

    StakeholderDTO stakeholderToDto(Stakeholder stakeholder);

    Attachment dtoToAttachment(AttachmentDTO attachmentDTO);

    AttachmentDTO attachmentToDto(Attachment attachment);

    Decision dtoToDecision(DecisionDTO decisionDTO);
    DecisionDTO decisionToDto(Decision decision);

    Status dtoToStatus(StatusDTO statusDTO);

    NoteDTO noteToDto(Note note);
    Note dtoToNote(NoteDTO noteDTO);

    Appeal dtoToAppeal(AppealDTO appealDTO);

    Facility dtoToFacility(FacilityDTO facilityDTO);
}

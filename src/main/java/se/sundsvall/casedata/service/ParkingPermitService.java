package se.sundsvall.casedata.service;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.GetParkingPermitDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static se.sundsvall.casedata.service.util.Constants.PERMIT_NUMBER_EXTRA_PARAMETER_KEY;
import static se.sundsvall.casedata.service.util.Constants.PERMIT_STATUS_EXTRA_PARAMETER_KEY;

@Service
public class ParkingPermitService {
    private final ErrandRepository errandRepository;

    public ParkingPermitService(ErrandRepository errandRepository) {
        this.errandRepository = errandRepository;
    }

    public List<GetParkingPermitDTO> findAll(String personId) {
        List<GetParkingPermitDTO> parkingPermitsDTOList = new ArrayList<>();

        List<Errand> allErrands = personId == null ? errandRepository.findAll() : findAllErrandsWithApplicant(personId);

        List<ErrandDTO> allErrandsWithPrh = allErrands.stream()
                .filter(errand -> errand.getExtraParameters().containsKey(PERMIT_NUMBER_EXTRA_PARAMETER_KEY))
                .map(EntityDtoMapper.INSTANCE::errandToDto)
                .toList();

        if (allErrandsWithPrh.isEmpty()) {
            throw Problem.valueOf(Status.NOT_FOUND, "No parking permits found");
        }

        allErrandsWithPrh.forEach(errand -> parkingPermitsDTOList.add(GetParkingPermitDTO.builder()
                .artefactPermitNumber(errand.getExtraParameters().get(PERMIT_NUMBER_EXTRA_PARAMETER_KEY))
                .artefactPermitStatus(errand.getExtraParameters().get(PERMIT_STATUS_EXTRA_PARAMETER_KEY))
                .errandId(errand.getId())
                .errandDecision(errand.getDecisions().stream().filter(decisionDTO -> DecisionType.FINAL.equals(decisionDTO.getDecisionType())).findFirst().orElse(null))
                .build()));

        return parkingPermitsDTOList;
    }

    /**
     * @param personId of the applicant
     * @return all errands with stakeholder who has the role APPLICANT and matching personId
     */
    private List<Errand> findAllErrandsWithApplicant(String personId) {
        return errandRepository.findAll().stream()
                .filter(errand -> errand.getStakeholders().stream()
                        .filter(stakeholder -> nonNull(stakeholder.getPersonId()))
                        .anyMatch(stakeholder -> stakeholder.getPersonId().equals(personId) && stakeholder.getRoles().contains(StakeholderRole.APPLICANT)))
                .toList();
    }
}

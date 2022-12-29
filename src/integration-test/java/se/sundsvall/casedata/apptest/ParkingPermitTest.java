package se.sundsvall.casedata.apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.api.model.GetParkingPermitDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.service.util.Constants.PERMIT_NUMBER_EXTRA_PARAMETER_KEY;
import static se.sundsvall.casedata.service.util.Constants.PERMIT_STATUS_EXTRA_PARAMETER_KEY;
import static se.sundsvall.dept44.util.DateUtils.toOffsetDateTimeWithLocalOffset;

@WireMockAppTestSuite(files = "classpath:/ParkingPermitTest", classes = CaseDataApplication.class)
class ParkingPermitTest extends CustomAbstractAppTest {

    private static final String[] EXCLUDE_FIELDS = {
            "id",
            "version",
            "created",
            "updated",
            ".*\\.id",
            ".*\\.version",
            ".*\\.created",
            ".*\\.updated"
    };


    @Autowired
    private ErrandRepository errandRepository;

    @BeforeEach
    void beforeEach() {
        // Clear db between tests
        errandRepository.deleteAll();
    }

    @Test
    void testGetAllParkingPermitsNotFound() {
        setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/parking-permits")
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();

    }

    @Test
    void testGetAllParkingPermits() throws JsonProcessingException, ClassNotFoundException {

        var postErrand = createErrandDTO();
        // Set decision to final so it populates the GetParkingPermitDTO-object
        postErrand.getDecisions().get(0).setDecisionType(DecisionType.FINAL);
        postErrand.getExtraParameters().put(PERMIT_NUMBER_EXTRA_PARAMETER_KEY, RandomStringUtils.random(10, true, true));
        postErrand.getExtraParameters().put(PERMIT_STATUS_EXTRA_PARAMETER_KEY, RandomStringUtils.random(10, true, true));

        setupCall()
                .withServicePath("/errands")
                .withHttpMethod(HttpMethod.POST)
                .withRequest(OBJECT_MAPPER.writeValueAsString(postErrand))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        var getParkingPermitDTOList = Arrays.asList(
                setupCall()
                        .withHttpMethod(HttpMethod.GET)
                        .withServicePath("/parking-permits")
                        .withExpectedResponseStatus(HttpStatus.OK)
                        .sendRequestAndVerifyResponse()
                        .andReturnBody(GetParkingPermitDTO[].class));
        assertEquals(1, getParkingPermitDTOList.size());
        var getParkingPermitDTO = getParkingPermitDTOList.get(0);
        // Set offset on datetime
        getParkingPermitDTO.getErrandDecision().setDecidedAt(toOffsetDateTimeWithLocalOffset(getParkingPermitDTO.getErrandDecision().getDecidedAt()));
        getParkingPermitDTO.getErrandDecision().setValidFrom(toOffsetDateTimeWithLocalOffset(getParkingPermitDTO.getErrandDecision().getValidFrom()));
        getParkingPermitDTO.getErrandDecision().setValidTo(toOffsetDateTimeWithLocalOffset(getParkingPermitDTO.getErrandDecision().getValidTo()));

        assertEquals(postErrand.getExtraParameters().get(PERMIT_NUMBER_EXTRA_PARAMETER_KEY), getParkingPermitDTO.getArtefactPermitNumber());
        assertEquals(postErrand.getExtraParameters().get(PERMIT_STATUS_EXTRA_PARAMETER_KEY), getParkingPermitDTO.getArtefactPermitStatus());
        assertThat(postErrand.getDecisions().get(0))
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(EXCLUDE_FIELDS)
                .isEqualTo(getParkingPermitDTO.getErrandDecision());

    }

    @Test
    void testGetAllParkingPermitsByPersonId() throws JsonProcessingException, ClassNotFoundException {
        // Post errand that we will find
        var postErrand = createErrandDTO();
        // Set decision to final so it populates the GetParkingPermitDTO-object
        postErrand.getDecisions().get(0).setDecisionType(DecisionType.FINAL);
        var stakeholder = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        postErrand.setStakeholders(List.of(stakeholder));
        postErrand.getExtraParameters().put(PERMIT_NUMBER_EXTRA_PARAMETER_KEY, RandomStringUtils.random(10, true, true));
        postErrand.getExtraParameters().put(PERMIT_STATUS_EXTRA_PARAMETER_KEY, RandomStringUtils.random(10, true, true));

        setupCall()
                .withServicePath("/errands")
                .withHttpMethod(HttpMethod.POST)
                .withRequest(OBJECT_MAPPER.writeValueAsString(postErrand))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        // Post another random errand that we will not find
        var randomPostErrand = createErrandDTO();
        var randomStakeholder1 = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT));
        var randomStakeholder2 = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        randomPostErrand.setStakeholders(List.of(randomStakeholder1, randomStakeholder2));
        randomPostErrand.getExtraParameters().put(PERMIT_NUMBER_EXTRA_PARAMETER_KEY, RandomStringUtils.random(10, true, true));
        setupCall()
                .withServicePath("/errands")
                .withHttpMethod(HttpMethod.POST)
                .withRequest(OBJECT_MAPPER.writeValueAsString(randomPostErrand))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        var getParkingPermitDTOList = Arrays.asList(
                setupCall()
                        .withHttpMethod(HttpMethod.GET)
                        .withServicePath(MessageFormat.format("/parking-permits?personId={0}", stakeholder.getPersonId()))
                        .withExpectedResponseStatus(HttpStatus.OK)
                        .sendRequestAndVerifyResponse()
                        .andReturnBody(GetParkingPermitDTO[].class));
        assertEquals(1, getParkingPermitDTOList.size());
        var getParkingPermitDTO = getParkingPermitDTOList.get(0);
        // Set offset on datetime
        getParkingPermitDTO.getErrandDecision().setDecidedAt(toOffsetDateTimeWithLocalOffset(getParkingPermitDTO.getErrandDecision().getDecidedAt()));
        getParkingPermitDTO.getErrandDecision().setValidFrom(toOffsetDateTimeWithLocalOffset(getParkingPermitDTO.getErrandDecision().getValidFrom()));
        getParkingPermitDTO.getErrandDecision().setValidTo(toOffsetDateTimeWithLocalOffset(getParkingPermitDTO.getErrandDecision().getValidTo()));

        assertEquals(postErrand.getExtraParameters().get(PERMIT_NUMBER_EXTRA_PARAMETER_KEY), getParkingPermitDTO.getArtefactPermitNumber());
        assertEquals(postErrand.getExtraParameters().get(PERMIT_STATUS_EXTRA_PARAMETER_KEY), getParkingPermitDTO.getArtefactPermitStatus());
        assertThat(postErrand.getDecisions().get(0))
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(EXCLUDE_FIELDS)
                .isEqualTo(getParkingPermitDTO.getErrandDecision());

    }

}

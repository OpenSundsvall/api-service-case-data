package se.sundsvall.casedata.apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.SUBSCRIBER;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;
import static se.sundsvall.dept44.util.DateUtils.toOffsetDateTimeWithLocalOffset;

@WireMockAppTestSuite(
        files = "classpath:/DecisionTest/",
        classes = CaseDataApplication.class
)
class DecisionTest extends CustomAbstractAppTest {

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

    @Test
    void test1_postErrandAndPatchWithDecisions() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = postErrand(createErrandDTO());

        DecisionDTO inputDecision = createDecisionDTO();

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/decisions", errandDTO.getId()))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
                .withRequest(OBJECT_MAPPER.writeValueAsString(inputDecision))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        List<DecisionDTO> getDecisionResult = Arrays.asList(setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}/decisions", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(createDecisionDTO()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(DecisionDTO[].class));

        getDecisionResult.forEach(d -> {
            d.setDecidedAt(toOffsetDateTimeWithLocalOffset(d.getDecidedAt()));
            d.setValidFrom(toOffsetDateTimeWithLocalOffset(d.getValidFrom()));
            d.setValidTo(toOffsetDateTimeWithLocalOffset(d.getValidTo()));
        });

        assertThat(inputDecision)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(EXCLUDE_FIELDS)
                .isEqualTo(getDecisionResult.stream().max(Comparator.comparing(DecisionDTO::getCreated)).orElseThrow());

        assertErrandWasUpdatedAfterChange(errandDTO);
    }

    @Test
    void test2_postErrandAndPatchExistingDecision() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = postErrand(createErrandDTO());
        DecisionDTO decisionDTO = errandDTO.getDecisions().get(0);

        PatchDecisionDTO patch = new PatchDecisionDTO();
        patch.setDescription(RandomStringUtils.random(10, true, false));

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/decisions/{0}", decisionDTO.getId()))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
                .withRequest(OBJECT_MAPPER.writeValueAsString(patch))
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        List<DecisionDTO> getDecisionResult = Arrays.asList(setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}/decisions", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(createDecisionDTO()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(DecisionDTO[].class));


        DecisionDTO correctDto = OBJECT_MAPPER.readValue(TestUtil.OBJECT_MAPPER.writeValueAsString(decisionDTO), DecisionDTO.class);
        correctDto.setDescription(patch.getDescription());

        assertThat(correctDto)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(EXCLUDE_FIELDS)
                .isEqualTo(getDecisionResult.get(0));

        assertErrandWasUpdatedAfterChange(errandDTO);
    }

    @Test
    void test3_postErrandAndPutOnExistingDecision() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = postErrand(createErrandDTO());
        DecisionDTO decisionDTO = errandDTO.getDecisions().get(0);

        DecisionDTO patch = new DecisionDTO();
        patch.setDescription(RandomStringUtils.random(10, true, false));

        setupCall()
                .withHttpMethod(HttpMethod.PUT)
                .withServicePath(MessageFormat.format("/decisions/{0}", decisionDTO.getId()))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
                .withRequest(OBJECT_MAPPER.writeValueAsString(patch))
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        List<DecisionDTO> getDecisionResult = Arrays.asList(setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}/decisions", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(createDecisionDTO()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(DecisionDTO[].class));

        getDecisionResult.forEach(d -> {
            d.setDecidedAt(toOffsetDateTimeWithLocalOffset(d.getDecidedAt()));
            d.setValidFrom(toOffsetDateTimeWithLocalOffset(d.getValidFrom()));
            d.setValidTo(toOffsetDateTimeWithLocalOffset(d.getValidTo()));
        });

        DecisionDTO correctDto = new DecisionDTO();
        correctDto.setDescription(patch.getDescription());

        assertThat(correctDto)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(EXCLUDE_FIELDS)
                .isEqualTo(getDecisionResult.get(0));

        assertErrandWasUpdatedAfterChange(errandDTO);
    }

    @Test
    void test4_postErrandAndDeleteDecision() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = postErrand(createErrandDTO());

        setupCall()
                .withHttpMethod(HttpMethod.DELETE)
                .withServicePath(MessageFormat.format("/errands/{0}/decisions/{1}", errandDTO.getId(), errandDTO.getDecisions().get(0).getId()))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}/decisions", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(createDecisionDTO()))
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();

        assertErrandWasUpdatedAfterChange(errandDTO);
    }

    private ErrandDTO postErrand(ErrandDTO errandDTO) throws JsonProcessingException {
        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        List<ErrandDTO> allErrands = new ArrayList<>(findAllErrands());
        allErrands.sort(Comparator.comparing(ErrandDTO::getCreated).reversed());
        return allErrands.get(0);
    }

    private List<ErrandDTO> findAllErrands() throws JsonProcessingException {
        Page<ErrandDTO> result = setupCall().
                withHttpMethod(HttpMethod.GET)
                .withServicePath("/errands")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(new TypeReference<RestResponsePage<ErrandDTO>>() {
                });

        return result.getContent();
    }

    private void assertErrandWasUpdatedAfterChange(ErrandDTO errandDTO) throws JsonProcessingException, ClassNotFoundException {
        var errandAfterPut = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class);
        assertTrue(errandAfterPut.getUpdated().isAfter(errandDTO.getUpdated()));
        assertNotEquals(errandDTO.getUpdatedByClient(), errandAfterPut.getUpdatedByClient());
        assertEquals(SUBSCRIBER, errandAfterPut.getUpdatedByClient());
        assertNotEquals(errandDTO.getUpdatedBy(), errandAfterPut.getUpdatedBy());
        assertEquals(AD_USER, errandAfterPut.getUpdatedBy());
    }
}

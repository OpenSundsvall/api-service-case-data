package se.sundsvall.casedata.apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.CaseType;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

@WireMockAppTestSuite(files = "classpath:/AttachmentResourceTest", classes = CaseDataApplication.class)
class ErrandListenerTest extends CustomAbstractAppTest {

    @Autowired
    private ErrandRepository errandRepository;

    @BeforeEach
    void beforeEach() {
        errandRepository.deleteAll();
    }

    @Test
    void test1_persistErrandUnknown() throws JsonProcessingException {
        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(createErrandDTO()))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        Errand errand = errandRepository.findAll().stream().max(Comparator.comparing(Errand::getCreated)).orElseThrow();

        assertEquals(UNKNOWN, errand.getCreatedByClient());
        assertEquals(UNKNOWN, errand.getCreatedBy());
        assertNotNull(errand.getErrandNumber());
        // The errand gets an update directly because we update with processId
        assertEquals(UNKNOWN, errand.getUpdatedByClient());
        assertEquals(UNKNOWN, errand.getUpdatedBy());
    }

    @Test
    void test2_updateErrand() throws JsonProcessingException {
        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(createErrandDTO()))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        Errand errandBeforePatch = errandRepository.findAll().stream().max(Comparator.comparing(Errand::getCreated)).orElseThrow();

        PatchErrandDTO patchErrandDTO = new PatchErrandDTO();
        patchErrandDTO.setDiaryNumber("Patch");

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}", errandBeforePatch.getId()))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .withHeader(Constants.AD_USER_HEADER_KEY, "PatchUser")
                .withRequest(OBJECT_MAPPER.writeValueAsString(patchErrandDTO))
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        Errand errandAfterPatch = errandRepository.findAll().stream().max(Comparator.comparing(Errand::getCreated)).orElseThrow();

        assertNotEquals(errandBeforePatch.getUpdatedByClient(), errandAfterPatch.getUpdatedByClient());
        assertNotEquals(errandBeforePatch.getUpdatedBy(), errandAfterPatch.getUpdatedBy());
    }

    @Test
    void test3_generateErrandNumberForSameAbbreviation() throws JsonProcessingException {

        ErrandDTO errandDTO1 = createErrandDTO();
        errandDTO1.setCaseType(CaseType.PARKING_PERMIT);
        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO1))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        ErrandDTO errandDTO2 = createErrandDTO();
        errandDTO2.setCaseType(CaseType.LOST_PARKING_PERMIT);
        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO2))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        ErrandDTO errandDTO3 = createErrandDTO();
        errandDTO3.setCaseType(CaseType.PARKING_PERMIT_RENEWAL);
        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO3))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        List<Errand> resultList = errandRepository.findAll();

        for (int i = 0; i < resultList.size(); i++) {
            assertEquals(MessageFormat.format("{0}-{1}-{2}", CaseType.PARKING_PERMIT.getAbbreviation(), String.valueOf(LocalDate.now().getYear()), String.format("%06d", i + 1)), resultList.get(i).getErrandNumber());
        }
        resultList.forEach(errand -> assertTrue(errand.getErrandNumber().startsWith(CaseType.PARKING_PERMIT.getAbbreviation())));
    }

    @Test
    void test4_generateErrandNumberForDifferentAbbreviation() throws JsonProcessingException {

        for (CaseType caseType : CaseType.values()) {

            ErrandDTO errandDTO1 = createErrandDTO();
            errandDTO1.setCaseType(caseType);
            setupCall()
                    .withHttpMethod(HttpMethod.POST)
                    .withServicePath("/errands")
                    .withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO1))
                    .withExpectedResponseStatus(HttpStatus.CREATED)
                    .sendRequestAndVerifyResponse();
        }

        List<Errand> resultList = errandRepository.findAll();
        resultList.forEach(errand -> assertFalse(errand.getErrandNumber() == null || errand.getErrandNumber().isBlank()));
    }
}

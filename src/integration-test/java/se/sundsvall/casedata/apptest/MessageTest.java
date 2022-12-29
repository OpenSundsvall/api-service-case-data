package se.sundsvall.casedata.apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.SUBSCRIBER;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

@WireMockAppTestSuite(
        files = "classpath:/MessageTest/",
        classes = CaseDataApplication.class
)
class MessageTest extends CustomAbstractAppTest {

    @Autowired
    private ErrandRepository errandRepository;

    @BeforeEach
    void beforeEach() {
        errandRepository.deleteAll();
    }

    @Test
    void testPatchErrandWithMessage() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO persistedErrandDTO = postErrand(createErrandDTO());

        String message = RandomStringUtils.random(10, true, true);

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/message-ids", persistedErrandDTO.getId()))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
                .withRequest(OBJECT_MAPPER.writeValueAsString(List.of(message)))
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        ErrandDTO resultPatch = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", persistedErrandDTO.getId().toString()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class);

        assertNotEquals(persistedErrandDTO, resultPatch);
        assertTrue(resultPatch.getMessageIds().contains(message));

        assertErrandWasUpdatedAfterChange(persistedErrandDTO);
    }

    @Test
    void testGetMessagesOnErrand() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO persistedErrand = postErrand(createErrandDTO());

        List<String> messageResultList = Arrays.asList(setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}/message-ids", persistedErrand.getId().toString()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(String[].class));

        List<String> persistedMessages = new ArrayList<>(persistedErrand.getMessageIds());
        List<String> fetchedMessages = new ArrayList<>(messageResultList);

        assertEquals(persistedMessages, fetchedMessages);
    }

    @Test
    void testGetMessagesOnErrandNotFound() {
        setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}/message-ids", String.valueOf(new Random().nextLong())))
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();
    }

    private ErrandDTO postErrand(ErrandDTO errandDTO) throws JsonProcessingException, ClassNotFoundException {
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

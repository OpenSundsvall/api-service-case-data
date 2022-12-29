package se.sundsvall.casedata.apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createAttachmentDTO;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.getRandomOfEnum;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.SUBSCRIBER;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

@WireMockAppTestSuite(files = "classpath:/AttachmentResourceTest", classes = CaseDataApplication.class)
class AttachmentResourceTest extends CustomAbstractAppTest {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Test
    void test1_GetAttachment() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = postAndGetErrand(createErrandDTO());

        AttachmentDTO attachment = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/attachments/" + errandDTO.getAttachments().get(0).getId())
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(AttachmentDTO.class);

        assertNotNull(attachment);
    }

    @Test
    void test2_GetAttachmentNotFound() {
        setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/attachments/" + 1000)
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();
    }

    @Test
    void test3_PutAttachment() throws JsonProcessingException, ClassNotFoundException {

        ErrandDTO errandDTO = postAndGetErrand(createErrandDTO());
        AttachmentDTO attachmentBefore = errandDTO.getAttachments().get(0);
        AttachmentDTO inputAttachmentDTO = createAttachmentDTO((AttachmentCategory) getRandomOfEnum(AttachmentCategory.class));

        setupCall()
                .withHttpMethod(HttpMethod.PUT)
                .withServicePath(MessageFormat.format("/attachments/{0}", attachmentBefore.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(inputAttachmentDTO))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        AttachmentDTO attachmentAfter = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/attachments/{0}", attachmentBefore.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(AttachmentDTO.class);

        assertEquals(attachmentBefore.getId(), attachmentAfter.getId());
        assertEquals(attachmentBefore.getCreated(), attachmentAfter.getCreated());
        assertTrue(attachmentBefore.getUpdated().isBefore(attachmentAfter.getUpdated()));
        assertEquals(attachmentBefore.getVersion() + 1, attachmentAfter.getVersion());
        assertEquals(inputAttachmentDTO.getCategory(), attachmentAfter.getCategory());
        assertEquals(inputAttachmentDTO.getExtension(), attachmentAfter.getExtension());
        assertEquals(inputAttachmentDTO.getFile(), attachmentAfter.getFile());
        assertEquals(inputAttachmentDTO.getMimeType(), attachmentAfter.getMimeType());
        assertEquals(inputAttachmentDTO.getName(), attachmentAfter.getName());
        assertEquals(inputAttachmentDTO.getNote(), attachmentAfter.getNote());
        assertEquals(inputAttachmentDTO.getExtraParameters(), attachmentAfter.getExtraParameters());

        assertErrandWasUpdatedAfterChange(errandDTO);
    }

    @Test
    void test4_PutAttachmentNotFound() throws JsonProcessingException {
        AttachmentDTO inputAttachmentDTO = createAttachmentDTO((AttachmentCategory) getRandomOfEnum(AttachmentCategory.class));

        setupCall()
                .withHttpMethod(HttpMethod.PUT)
                .withServicePath(MessageFormat.format("/attachments/{0}", String.valueOf(new Random().nextLong())))
                .withRequest(OBJECT_MAPPER.writeValueAsString(inputAttachmentDTO))
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();
    }

    @Test
    void test5_DeleteAttachmentOnErrand() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = postAndGetErrand(createErrandDTO());
        Long errandId = errandDTO.getId();
        Long attachmentID = errandDTO.getAttachments().get(0).getId();

        Assertions.assertTrue(errandDTO.getAttachments().stream().anyMatch(attachmentDTO -> attachmentDTO.getId().equals(attachmentID)));
        Assertions.assertTrue(attachmentRepository.findById(attachmentID).isPresent());

        setupCall()
                .withHttpMethod(HttpMethod.DELETE)
                .withServicePath(MessageFormat.format("/errands/{0}/attachments/{1}", errandId, attachmentID))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        List<ErrandDTO> errandListAfterDelete = findAllErrands();

        Assertions.assertFalse(errandListAfterDelete.get(0).getAttachments().stream().anyMatch(attachmentDTO -> attachmentDTO.getId().equals(attachmentID)));
        Assertions.assertFalse(attachmentRepository.findById(attachmentID).isPresent());

        assertErrandWasUpdatedAfterChange(errandDTO);
    }

    @Test
    void test6_DeleteAttachmentOnErrandNotFound_1() throws JsonProcessingException {
        ErrandDTO errandDTO = postAndGetErrand(createErrandDTO());

        setupCall()
                .withHttpMethod(HttpMethod.DELETE)
                .withServicePath(MessageFormat.format("/errands/{0}/attachments/{1}", String.valueOf(new Random().nextInt(100, 10000)), errandDTO.getAttachments().get(0).getId()))
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();
    }

    @Test
    void test7_DeleteAttachmentOnErrandNotFound_2() throws JsonProcessingException {
        ErrandDTO errandDTO = postAndGetErrand(createErrandDTO());

        setupCall()
                .withHttpMethod(HttpMethod.DELETE)
                .withServicePath(MessageFormat.format("/errands/{0}/attachments/{1}", errandDTO.getId(), String.valueOf(new Random().nextInt(100, 10000))))
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();
    }

    @Test
    void test8_PutAttachmentsOnErrand() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO postErrandDTO = createErrandDTO();
        postErrandDTO.setAttachments(List.of(createAttachmentDTO(AttachmentCategory.SIGNATURE), createAttachmentDTO(AttachmentCategory.PASSPORT_PHOTO)));
        ErrandDTO errandDTO = postAndGetErrand(postErrandDTO);

        List<AttachmentDTO> attachmentDTOList = List.of(createAttachmentDTO(AttachmentCategory.SIGNATURE), createAttachmentDTO(AttachmentCategory.PASSPORT_PHOTO), createAttachmentDTO(AttachmentCategory.MEDICAL_CONFIRMATION));

        setupCall()
                .withHttpMethod(HttpMethod.PUT)
                .withServicePath(MessageFormat.format("/errands/{0}/attachments", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(attachmentDTOList))
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        var result = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class);

        assertEquals(attachmentDTOList.size(), result.getAttachments().size());
    }

    private ErrandDTO postAndGetErrand(ErrandDTO errandDTO) throws JsonProcessingException {
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

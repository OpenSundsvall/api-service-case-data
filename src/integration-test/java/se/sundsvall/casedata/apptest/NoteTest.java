package se.sundsvall.casedata.apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.SUBSCRIBER;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

@WireMockAppTestSuite(
        files = "classpath:/NoteTest/",
        classes = CaseDataApplication.class
)
class NoteTest extends CustomAbstractAppTest {

    private static final String AD_USER_HEADER_VALUE = "test";

    private static final String[] EXCLUDE_FIELDS = {
            "id",
            "version",
            "created",
            "updated",
            "createdBy",
            "updatedBy"};

    @Test
    void test1_patchErrandWithNote() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandBeforePatch = postErrand(createErrandDTO());

        var noteDTO = createErrandDTO().getNotes().get(0);

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/notes", errandBeforePatch.getId()))
                .withHeader(AD_USER_HEADER_KEY, AD_USER_HEADER_VALUE)
                .withRequest(OBJECT_MAPPER.writeValueAsString(noteDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        ErrandDTO errandAfterPatch = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", errandBeforePatch.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class);

        // Errand assertions
        assertNotEquals(errandBeforePatch, errandAfterPatch);
        assertTrue(errandAfterPatch.getUpdated().isAfter(errandBeforePatch.getUpdated()));
        assertEquals(UNKNOWN, errandAfterPatch.getCreatedBy());
        assertEquals(AD_USER_HEADER_VALUE, errandAfterPatch.getUpdatedBy());
        assertTrue(errandAfterPatch.getNotes().size() > errandBeforePatch.getNotes().size());

        // Note assertions
        errandBeforePatch.getNotes().forEach(note -> {
            assertEquals(UNKNOWN, note.getCreatedBy());
        });
        var patchedNote = errandAfterPatch.getNotes().stream().max(Comparator.comparing(NoteDTO::getUpdated)).orElseThrow();
        assertNotEquals(noteDTO, patchedNote);
        assertTrue(patchedNote.getVersion() > noteDTO.getVersion());
        assertEquals(AD_USER_HEADER_VALUE, patchedNote.getCreatedBy());
        assertEquals(AD_USER_HEADER_VALUE, patchedNote.getUpdatedBy());

        assertThat(noteDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(patchedNote);
    }

    @Test
    void test2_patchErrandWithNoteUnknownUser() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = postErrand(createErrandDTO());

        var noteDTO = createErrandDTO().getNotes().get(0);

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/notes", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(noteDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        ErrandDTO resultErrand = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class);

        var patchedNote = resultErrand.getNotes().stream().max(Comparator.comparing(NoteDTO::getUpdated)).orElseThrow();
        assertNotEquals(noteDTO, patchedNote);
        assertTrue(patchedNote.getVersion() > noteDTO.getVersion());
        assertEquals(UNKNOWN, patchedNote.getCreatedBy());
        assertEquals(UNKNOWN, patchedNote.getUpdatedBy());

        assertThat(noteDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(resultErrand.getNotes().stream().max(Comparator.comparing(NoteDTO::getUpdated)).orElseThrow());
    }

    @Test
    void test3_patchNoteOnErrand() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = postErrand(createErrandDTO());

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/notes", errandDTO.getId()))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .withHeader(AD_USER_HEADER_KEY, AD_USER)
                .withRequest(OBJECT_MAPPER.writeValueAsString(createNoteDTO()))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        assertErrandWasUpdatedAfterChange(errandDTO, SUBSCRIBER, AD_USER);

        ErrandDTO resultErrandBeforePatch = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class);

        resultErrandBeforePatch.getNotes().sort(Comparator.comparing(NoteDTO::getCreated).reversed());
        var resultNoteBeforePatch = resultErrandBeforePatch.getNotes().get(0);

        var patch = new NoteDTO();
        patch.setText("This is a patch");

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/notes/{0}", resultNoteBeforePatch.getId()))
                .withHeader(X_JWT_ASSERTION_HEADER_KEY, null)
                .withHeader(AD_USER_HEADER_KEY, null)
                .withRequest(OBJECT_MAPPER.writeValueAsString(patch))
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        assertErrandWasUpdatedAfterChange(resultErrandBeforePatch, UNKNOWN, UNKNOWN);

        ErrandDTO resultErrand = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class);

        // Verify the errand contains this note
        assertThat(resultErrand.getNotes())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(ArrayUtils.addAll(new String[]{"updatedBy", "text"}, EXCLUDE_FIELDS))
                .contains(resultNoteBeforePatch);
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

    private List<ErrandDTO> findAllErrands() throws JsonProcessingException, ClassNotFoundException {
        Page<ErrandDTO> result = setupCall().
                withHttpMethod(HttpMethod.GET)
                .withServicePath("/errands")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(new TypeReference<RestResponsePage<ErrandDTO>>() {
                });

        return result.getContent();
    }

    private void assertErrandWasUpdatedAfterChange(ErrandDTO errandDTO, String subscriber, String adUser) throws JsonProcessingException, ClassNotFoundException {
        var errandAfter = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class);
        assertTrue(errandAfter.getUpdated().isAfter(errandDTO.getUpdated()));
        assertNotEquals(errandDTO.getUpdatedByClient(), errandAfter.getUpdatedByClient());
        assertEquals(subscriber, errandAfter.getUpdatedByClient());
        assertNotEquals(errandDTO.getUpdatedBy(), errandAfter.getUpdatedBy());
        assertEquals(adUser, errandAfter.getUpdatedBy());
    }
}

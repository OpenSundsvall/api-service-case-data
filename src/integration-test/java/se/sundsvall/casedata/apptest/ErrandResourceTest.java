package se.sundsvall.casedata.apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.model.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createAttachmentDTO;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.TestUtil.createStatusDTO;
import static se.sundsvall.casedata.TestUtil.getRandomOffsetDateTime;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.PROCESS_ENGINE_URL;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

@WireMockAppTestSuite(files = "classpath:/ErrandResourceTest", classes = CaseDataApplication.class)
class ErrandResourceTest extends CustomAbstractAppTest {

    private static final String AD_USER_HEADER_VALUE = "user";
    private static final String[] EXCLUDE_FIELDS = {
            "id",
            "version",
            "created",
            "updated",
            ".*\\.id",
            ".*\\.version",
            ".*\\.created",
            ".*\\.updated",
            "processId",
            "errandNumber",
            "createdByClient",
            "updatedByClient",
            "createdBy",
            "updatedBy",
            "note.*\\.createdBy",
            "note.*\\.updatedBy"
    };

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ErrandRepository errandRepository;

    @BeforeEach
    void beforeEach() {
        errandRepository.deleteAll();
    }

    @Test
    void testPostErrand() {
        ErrandDTO inputErrandDTO = createErrandDTO();
        String id = postErrand(inputErrandDTO);

        ErrandDTO getErrandDTO = webTestClient.get().uri("/errands/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(ErrandDTO.class).getResponseBody().blockFirst();

        assertNotNull(Objects.requireNonNull(getErrandDTO).getProcessId());

        assertThat(inputErrandDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(getErrandDTO);

        verify(1, postRequestedFor(urlEqualTo(PROCESS_ENGINE_URL)).withRequestBody(new ContainsPattern(id)));
    }

    @Test
    void testPostMinimalErrand() {
        ErrandDTO inputErrandDTO = new ErrandDTO();
        inputErrandDTO.setCaseType(CaseType.PARKING_PERMIT);
        String id = postErrand(inputErrandDTO);

        ErrandDTO getErrandDTO = webTestClient.get().uri("/errands/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(ErrandDTO.class).getResponseBody().blockFirst();

        assertNotNull(Objects.requireNonNull(getErrandDTO).getProcessId());

        assertThat(inputErrandDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(getErrandDTO);
    }

    @Test
    void testPatchErrand() {

        ErrandDTO inputPostErrandDTO = createErrandDTO();
        String id = postErrand(inputPostErrandDTO);

        // Get posted object
        ErrandDTO resultPostErrandDTO = webTestClient.get().uri("/errands/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(ErrandDTO.class).getResponseBody().blockFirst();

        // Create patch object
        PatchErrandDTO inputPatchErrandDTO = new PatchErrandDTO();
        inputPatchErrandDTO.setDiaryNumber("A new patched diary number");
        inputPatchErrandDTO.setApplicationReceived(getRandomOffsetDateTime());
        inputPatchErrandDTO.setExtraParameters(createExtraParameters());

        // Patch the object
        webTestClient.patch().uri("/errands/{id}", id)
                .bodyValue(inputPatchErrandDTO)
                .exchange()
                .expectStatus().isNoContent();

        // Get patched object
        ErrandDTO resultPatchErrandDTO = webTestClient.get().uri("/errands/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(ErrandDTO.class).getResponseBody().blockFirst();

        // Update fields of the originally posted object, so we can compare with the patched object.
        assertNotNull(resultPostErrandDTO);
        resultPostErrandDTO.setDiaryNumber(inputPatchErrandDTO.getDiaryNumber());
        resultPostErrandDTO.setApplicationReceived(inputPatchErrandDTO.getApplicationReceived());
        resultPostErrandDTO.setUpdatedByClient(Constants.UNKNOWN);
        resultPostErrandDTO.setUpdatedBy(Constants.UNKNOWN);
        resultPostErrandDTO.getExtraParameters().putAll(inputPatchErrandDTO.getExtraParameters());

        assertThat(resultPostErrandDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(resultPatchErrandDTO);
    }

    @Test
    void testGetWithOneQueryParam() throws JsonProcessingException {

        ErrandDTO inputPostErrandDTO = createErrandDTO();
        // Create initial errand
        postErrand(inputPostErrandDTO);

        createSomeErrands(5);

        Page<ErrandDTO> resultList = webTestClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path("errands")
                                .queryParam("filter", "externalCaseId:'%s'".formatted(inputPostErrandDTO.getExternalCaseId()))
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).getTotalElements());
        ErrandDTO result = resultList.getContent().get(0);

        assertNotNull(result.getProcessId());

        assertThat(inputPostErrandDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(result);
    }

    @Test
    void testGetWithExtraParameter() throws JsonProcessingException {
        ErrandDTO inputPostErrandDTO = createErrandDTO();
        Map<String, String> extraParameters = new HashMap<>();
        extraParameters.put("key 1", "value 1");
        extraParameters.put("key 2", "value 2");
        inputPostErrandDTO.setExtraParameters(extraParameters);
        // Create initial errand
        postErrand(inputPostErrandDTO);

        createSomeErrands(5);

        Page<ErrandDTO> resultList = webTestClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path("errands")
                                .queryParam("extraParameters[key 1]", "value 1")
                                .queryParam("extraParameters[key 2]", "value 2")
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).getTotalElements());
        ErrandDTO result = resultList.getContent().get(0);

        assertNotNull(result.getProcessId());

        assertThat(inputPostErrandDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(result);
    }

    @Test
    void testGetWithExtraParameterMismatch() throws JsonProcessingException {
        ErrandDTO inputPostErrandDTO = createErrandDTO();
        Map<String, String> extraParameters = new HashMap<>();
        extraParameters.put("key 1", "value 1");
        extraParameters.put("key 2", "value 2");
        inputPostErrandDTO.setExtraParameters(extraParameters);
        // Create initial errand
        postErrand(inputPostErrandDTO);

        createSomeErrands(5);

        webTestClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path("errands")
                                .queryParam("extraParameters[key 1]", "value 1")
                                // One of the extra parameters is wrong
                                .queryParam("extraParameters[key 2]", "value 3")
                                .build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetWithExtraParameterAndFilterMismatch() throws JsonProcessingException {
        ErrandDTO inputPostErrandDTO = createErrandDTO();
        Map<String, String> extraParameters = new HashMap<>();
        extraParameters.put("key 1", "value 1");
        extraParameters.put("key 2", "value 2");
        inputPostErrandDTO.setExtraParameters(extraParameters);
        // Create initial errand
        postErrand(inputPostErrandDTO);

        createSomeErrands(5);

        webTestClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path("errands")
                                .queryParam("extraParameters[key 1]", "value 1")
                                // Filter is wrong
                                .queryParam("filter", "externalCaseId:'%s'".formatted(RandomStringUtils.randomNumeric(10)))
                                .build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetWithFilterPageableAndExtraParameters() throws JsonProcessingException {
        ErrandDTO inputPostErrandDTO = createErrandDTO();
        Map<String, String> extraParameters = new HashMap<>();
        extraParameters.put("key 1", "value 1");
        inputPostErrandDTO.setExtraParameters(extraParameters);
        // Create initial errand
        postErrand(inputPostErrandDTO);

        createSomeErrands(5);

        Page<ErrandDTO> resultList = webTestClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path("errands")
                                .queryParam("filter", "externalCaseId:'%s'".formatted(inputPostErrandDTO.getExternalCaseId()))
                                .queryParam("extraParameters[key 1]", "value 1")
                                .queryParam("page", "0")
                                .queryParam("size", "10")
                                .queryParam("sort", "id,desc")
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).getTotalElements());
        ErrandDTO result = resultList.getContent().get(0);

        assertNotNull(result.getProcessId());

        assertThat(inputPostErrandDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(result);
    }

    @Test
    void testGetWithQueryParamTextContains() throws JsonProcessingException {

        final String NAME_PREFIX = "abc";
        final String WORD_IN_THE_MIDDLE = "word-in-the-middle";

        ErrandDTO inputPostErrandDTO = createErrandDTO();
        inputPostErrandDTO.getStakeholders().get(0).setFirstName(NAME_PREFIX + WORD_IN_THE_MIDDLE + RandomStringUtils.random(10));
        postErrand(inputPostErrandDTO);

        ErrandDTO anotherErrandWithSameFirstName = createErrandDTO();
        anotherErrandWithSameFirstName.getStakeholders().get(0).setFirstName(NAME_PREFIX + WORD_IN_THE_MIDDLE + RandomStringUtils.random(10));
        postErrand(anotherErrandWithSameFirstName);

        createSomeErrands(5);

        Page<ErrandDTO> resultPage = webTestClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path("errands")
                                .queryParam("filter", "stakeholders.firstName ~ '*%s*'".formatted(WORD_IN_THE_MIDDLE))
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(2, Objects.requireNonNull(resultPage).getTotalElements());
        var resultList = resultPage.getContent();

        assertThat(inputPostErrandDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(resultList.stream().min(Comparator.comparing(ErrandDTO::getCreated)).orElseThrow());

        assertThat(anotherErrandWithSameFirstName)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(resultList.stream().max(Comparator.comparing(ErrandDTO::getCreated)).orElseThrow());
    }

    @Test
    void testGetWithOneQueryParam404() {
        webTestClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path("errands")
                                .queryParam("filter", "externalCaseId:'%s'".formatted(UUID.randomUUID()))
                                .build())
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    void testGetWithMultipleQueryParams() throws JsonProcessingException {

        ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
        String id = postErrand(inputPostErrandDTO_1);

        ErrandDTO resultPostErrandDTO_1 = webTestClient.get().uri("/errands/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(ErrandDTO.class).getResponseBody().blockFirst();

        // Create some test data
        createSomeErrands(5);

        // Get only the first one with query params
        Page<ErrandDTO> resultList = webTestClient.get().uri(
                        uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
                                .path("errands")
                                .queryParam("filter", "externalCaseId:'%s'".formatted(inputPostErrandDTO_1.getExternalCaseId()) +
                                        "and " +
                                        "caseType:'%s'".formatted(inputPostErrandDTO_1.getCaseType()) +
                                        "and " +
                                        "priority:'%s'".formatted(inputPostErrandDTO_1.getPriority()) +
                                        "and " +
                                        "description:'%s'".formatted(inputPostErrandDTO_1.getDescription()) +
                                        "and " +
                                        "caseTitleAddition:'%s'".formatted(inputPostErrandDTO_1.getCaseTitleAddition()) +
                                        "and " +
                                        "applicationReceived:'%s'".formatted("{applicationReceived}") +
                                        "and " +
                                        "created:'%s'".formatted("{created}"))
                                .encode()
                                .buildAndExpand(inputPostErrandDTO_1.getApplicationReceived(), Objects.requireNonNull(resultPostErrandDTO_1).getCreated())
                                .toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).getTotalElements());
        ErrandDTO result = resultList.getContent().get(0);

        assertNotNull(result.getProcessId());
        assertThat(inputPostErrandDTO_1)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(result);
    }

    /**
     * One of the fields is wrong, but an errand should be found anyway.
     */
    @Test
    void testGetWithMultipleQueryParams2() throws JsonProcessingException {

        ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
        String id = postErrand(inputPostErrandDTO_1);

        ErrandDTO resultPostErrandDTO_1 = webTestClient.get().uri("/errands/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(ErrandDTO.class).getResponseBody().blockFirst();

        // Create some test data
        createSomeErrands(5);

        Page<ErrandDTO> resultList = webTestClient.get().uri(
                        uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
                                .path("errands")
                                .queryParam("filter", // Random UUID = no match, but uses operator "or" and should find an errand anyway.
                                        "externalCaseId:'%s'".formatted(UUID.randomUUID()) +
                                                "or " +
                                                "caseType:'%s'".formatted(inputPostErrandDTO_1.getCaseType()) +
                                                "and " +
                                                "priority:'%s'".formatted(inputPostErrandDTO_1.getPriority()) +
                                                "and " +
                                                "description:'%s'".formatted(inputPostErrandDTO_1.getDescription()) +
                                                "and " +
                                                "caseTitleAddition:'%s'".formatted(inputPostErrandDTO_1.getCaseTitleAddition()) +
                                                "and " +
                                                "applicationReceived:'%s'".formatted("{applicationReceived}") +
                                                "and " +
                                                "created:'%s'".formatted("{created}"))
                                .encode()
                                .buildAndExpand(inputPostErrandDTO_1.getApplicationReceived(), Objects.requireNonNull(resultPostErrandDTO_1).getCreated())
                                .toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).getTotalElements());
        ErrandDTO result = resultList.getContent().get(0);

        assertNotNull(result.getProcessId());
        assertThat(inputPostErrandDTO_1)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(result);
    }

    @Test
    void testGetWithStakeholderQueryParams() throws JsonProcessingException {
        ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
        postErrand(inputPostErrandDTO_1);

        // Create some test data
        createSomeErrands(5);

        StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> stakeholderDTO.getType().equals(StakeholderType.PERSON)).findFirst().orElseThrow();

        // Get only the first one with query params
        Page<ErrandDTO> resultList = webTestClient.get().uri(
                        uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
                                .path("errands")
                                .queryParam("filter", "stakeholders.firstName:'%s'".formatted(person.getFirstName()) +
                                        "and " +
                                        "stakeholders.lastName:'%s'".formatted(person.getLastName()) +
                                        "and " +
                                        "stakeholders.personId:'%s'".formatted(person.getPersonId()))
                                .encode()
                                .build()
                                .toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).getTotalElements());
        ErrandDTO result = resultList.getContent().get(0);

        assertThat(inputPostErrandDTO_1)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(result);
    }

    @Test
    void testGetWithStakeholderAddressQueryParams() throws JsonProcessingException {
        ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
        postErrand(inputPostErrandDTO_1);

        // Create some test data
        createSomeErrands(5);

        StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> stakeholderDTO.getType().equals(StakeholderType.PERSON)).findFirst().orElseThrow();

        // Get only the first one with query params
        Page<ErrandDTO> resultList = webTestClient.get().uri(
                        uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
                                .path("errands")
                                .queryParam("filter", "stakeholders.addresses.street:'%s'".formatted(person.getAddresses().get(0).getStreet()) +
                                        "and " +
                                        "stakeholders.addresses.houseNumber:'%s'".formatted(person.getAddresses().get(0).getHouseNumber()))
                                .encode()
                                .build()
                                .toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).getTotalElements());
        ErrandDTO result = resultList.getContent().get(0);

        assertThat(inputPostErrandDTO_1)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(result);
    }

    @Test
    void testGetWithStakeholderQueryParams404() throws JsonProcessingException {
        ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
        postErrand(inputPostErrandDTO_1);

        // Create some test data
        createSomeErrands(5);

        StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> stakeholderDTO.getType().equals(StakeholderType.PERSON)).findFirst().orElseThrow();

        webTestClient.get().uri(
                        uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
                                .path("errands")
                                .queryParam("filter", "stakeholders.firstName:'%s'".formatted(person.getFirstName()) +
                                        "and " +
                                        "stakeholders.lastName:'%s'".formatted(person.getLastName()) +
                                        "and " +
                                        "stakeholders.personId:'%s'".formatted(UUID.randomUUID()))
                                .encode()
                                .build()
                                .toUri())
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    void testGetErrandsWithPersonId() throws JsonProcessingException {
        ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
        postErrand(inputPostErrandDTO_1);

        // Create some test data
        createSomeErrands(5);

        StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> stakeholderDTO.getType().equals(StakeholderType.PERSON)).findFirst().orElseThrow();
        // Get only the first one with query params
        Page<ErrandDTO> resultList = webTestClient.get().uri(
                        uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
                                .path("errands")
                                .queryParam("filter", "stakeholders.personId:'%s'".formatted(person.getPersonId()))
                                .encode()
                                .build()
                                .toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).getTotalElements());
        ErrandDTO result = resultList.getContent().get(0);

        assertThat(inputPostErrandDTO_1)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(result);
    }

    @Test
    void testGetErrandsWithOrganizationNumber() throws JsonProcessingException {
        ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
        postErrand(inputPostErrandDTO_1);

        // Create some test data
        createSomeErrands(5);

        StakeholderDTO organization = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> stakeholderDTO.getType().equals(StakeholderType.ORGANIZATION)).findFirst().orElseThrow();
        // Get only the first one with query params
        Page<ErrandDTO> resultList = webTestClient.get().uri(
                        uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
                                .path("errands")
                                .queryParam("filter", "stakeholders.organizationNumber:'%s'".formatted(organization.getOrganizationNumber()))
                                .encode()
                                .build()
                                .toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(new ParameterizedTypeReference<RestResponsePage<ErrandDTO>>() {
                }).getResponseBody().blockFirst();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).getTotalElements());
        ErrandDTO result = resultList.getContent().get(0);

        assertThat(inputPostErrandDTO_1)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        EXCLUDE_FIELDS)
                .isEqualTo(result);
    }

    @Test
    void testPatchErrandWithAttachment() throws JsonProcessingException {
        ErrandDTO errandDTO = createSomeErrands(1).getContent().get(0);

        AttachmentDTO attachmentDTO = createAttachmentDTO(AttachmentCategory.ANKVU);

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/attachments", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(attachmentDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .withExpectedResponseHeader(HttpHeaders.LOCATION, List.of(".+\\/attachments\\/\\d+"))
                .sendRequestAndVerifyResponse();
    }

    @Test
    void testPatchErrandWithNote() throws JsonProcessingException {
        ErrandDTO errandDTO = createSomeErrands(1).getContent().get(0);

        NoteDTO noteDTO = createNoteDTO();

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/notes", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(noteDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .withExpectedResponseHeader(HttpHeaders.LOCATION, List.of(".+\\/notes\\/\\d+"))
                .sendRequestAndVerifyResponse();
    }

    @Test
    void testPatchErrandWithStakeholders() throws JsonProcessingException {
        ErrandDTO errandDTO = createSomeErrands(1).getContent().get(0);

        StakeholderDTO stakeholderDTO = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER));

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/stakeholders", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(stakeholderDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .withExpectedResponseHeader(HttpHeaders.LOCATION, List.of(".+\\/stakeholders\\/\\d+"))
                .sendRequestAndVerifyResponse();
    }

    @Test
    void testPatchErrandWithDecision() throws JsonProcessingException {
        ErrandDTO errandDTO = createSomeErrands(1).getContent().get(0);

        DecisionDTO decisionDTO = createDecisionDTO();

        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/decisions", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(decisionDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .withExpectedResponseHeader(HttpHeaders.LOCATION, List.of(".+\\/decisions\\/\\d+"))
                .sendRequestAndVerifyResponse();
    }

    @Test
    void testPatchErrandWithStatus() throws JsonProcessingException {
        ErrandDTO errandDTO = createSomeErrands(1).getContent().get(0);

        var statusDTO = createStatusDTO();

        webTestClient.patch().uri("/errands/{id}/statuses", errandDTO.getId())
                .bodyValue(statusDTO)
                .exchange()
                .expectStatus().isNoContent();

        List<ErrandDTO> resultList = webTestClient.get().uri("errands/{id}", errandDTO.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(ErrandDTO.class).getResponseBody().collectList().block();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).size());
        var resultPatch = resultList.get(0);

        assertNotEquals(errandDTO, resultPatch);
        assertTrue(resultPatch.getStatuses().contains(statusDTO));
    }

    @Test
    void testPutErrandWithStatuses() throws JsonProcessingException {
        ErrandDTO errandDTO = createSomeErrands(1).getContent().get(0);

        var statusDTOList = List.of(createStatusDTO(), createStatusDTO(), createStatusDTO());

        webTestClient.put().uri("/errands/{id}/statuses", errandDTO.getId())
                .bodyValue(statusDTOList)
                .exchange()
                .expectStatus().isNoContent();

        List<ErrandDTO> resultList = webTestClient.get().uri("errands/{id}", errandDTO.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(ErrandDTO.class).getResponseBody().collectList().block();

        Assertions.assertEquals(1, Objects.requireNonNull(resultList).size());
        var resultPatch = resultList.get(0);

        assertNotEquals(errandDTO, resultPatch);
        assertEquals(statusDTOList, resultPatch.getStatuses());
    }

    private Page<ErrandDTO> createSomeErrands(int numberOfErrands) throws JsonProcessingException {
        for (int i = 0; i < numberOfErrands; i++) {
            setupCall()
                    .withHttpMethod(HttpMethod.POST)
                    .withServicePath("/errands")
                    .withRequest(OBJECT_MAPPER.writeValueAsString(createErrandDTO()))
                    .withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                    .withHeader(AD_USER_HEADER_KEY, AD_USER_HEADER_VALUE)
                    .withExpectedResponseStatus(HttpStatus.CREATED)
                    .sendRequestAndVerifyResponse();
        }

        Page<ErrandDTO> allResultList = setupCall().
                withHttpMethod(HttpMethod.GET)
                .withServicePath("/errands")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(new TypeReference<RestResponsePage<ErrandDTO>>() {
                });

        // There could be more errands created earlier
        assertTrue(Objects.requireNonNull(allResultList).getTotalElements() >= numberOfErrands);

        return allResultList;
    }

    /**
     * @param errandDTO - object that should be posted
     * @return errand ID
     */
    private String postErrand(ErrandDTO errandDTO) {
        var location = webTestClient.post().uri("/errands")
                .bodyValue(errandDTO)
                .header(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
                .header(AD_USER_HEADER_KEY, AD_USER_HEADER_VALUE)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(Object.class).getResponseHeaders().getLocation();

        assertNotNull(location);
        return location.toString().substring(location.toString().lastIndexOf("/") + 1);
    }
}

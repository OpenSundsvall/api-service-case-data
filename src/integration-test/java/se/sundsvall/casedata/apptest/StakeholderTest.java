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
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderRole;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderType;

@WireMockAppTestSuite(files = "classpath:/StakeholderTest/", classes = CaseDataApplication.class)
class StakeholderTest extends CustomAbstractAppTest {

    @Autowired
    private StakeholderRepository stakeholderRepository;

    @Test
    void testGetStakeholders() throws JsonProcessingException, ClassNotFoundException {

        StakeholderRole stakeholderRole = StakeholderRole.APPLICANT;

        for (int i = 0; i < 5; i++) {
            ErrandDTO errandDTO = postAndGetErrand(createErrandDTO());

            // Create stakeholder with random role
            patchStakeholder(errandDTO.getId(), getRandomStakeholderType(), List.of(getRandomStakeholderRole()));
            setupCall()
                    .withHttpMethod(HttpMethod.PATCH)
                    .withServicePath(MessageFormat.format("/errands/{0}/stakeholders", errandDTO.getId()))
                    .withRequest(OBJECT_MAPPER.writeValueAsString(createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole()))))
                    .withExpectedResponseStatus(HttpStatus.CREATED)
                    .sendRequestAndVerifyResponse();

            if (i == 4) {
                System.out.println("Role: " + stakeholderRole);
                // Create stakeholder with the correct role
                patchStakeholder(errandDTO.getId(), getRandomStakeholderType(), List.of(stakeholderRole));
            }
        }

        List<StakeholderDTO> allResultList = Arrays.asList(
                setupCall()
                        .withHttpMethod(HttpMethod.GET)
                        .withServicePath("/stakeholders")
                        .withExpectedResponseStatus(HttpStatus.OK)
                        .sendRequestAndVerifyResponse()
                        .andReturnBody(StakeholderDTO[].class));

        List<StakeholderDTO> roleResultList = Arrays.asList(
                setupCall()
                        .withHttpMethod(HttpMethod.GET)
                        .withServicePath(MessageFormat.format("/stakeholders?stakeholderRole={0}", stakeholderRole))
                        .withExpectedResponseStatus(HttpStatus.OK)
                        .sendRequestAndVerifyResponse()
                        .andReturnBody(StakeholderDTO[].class));

        List<StakeholderDTO> filteredAllResultList = requireNonNull(allResultList).stream().filter(stakeholderDTO -> stakeholderDTO.getRoles().contains(stakeholderRole)).toList();
        assertThat(roleResultList).hasSameElementsAs(filteredAllResultList);
        requireNonNull(roleResultList).forEach(stakeholderDTO -> assertTrue(stakeholderDTO.getRoles().contains(stakeholderRole)));
    }

    private void patchStakeholder(Long errandId, StakeholderType stakeholderType, List<StakeholderRole> stakeholderRoles) throws JsonProcessingException {
        setupCall()
                .withHttpMethod(HttpMethod.PATCH)
                .withServicePath(MessageFormat.format("/errands/{0}/stakeholders", errandId))
                .withRequest(OBJECT_MAPPER.writeValueAsString(createStakeholderDTO(stakeholderType, stakeholderRoles)))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();
    }

    @Test
    void testPutStakeholder() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = createErrandDTO();
        errandDTO.setStakeholders(new ArrayList<>());
        ErrandDTO persistedErrandDTO = postAndGetErrand(errandDTO);

        patchStakeholder(persistedErrandDTO.getId(), getRandomStakeholderType(), List.of(getRandomStakeholderRole()));

        StakeholderDTO preparationStakeholder = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", persistedErrandDTO.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class).getStakeholders().get(0);

        StakeholderDTO stakeholderBefore = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/stakeholders/" + preparationStakeholder.getId())
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(StakeholderDTO.class);

        assertEquals(preparationStakeholder, stakeholderBefore);

        StakeholderDTO inputStakeholderDTO = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        setupCall()
                .withHttpMethod(HttpMethod.PUT)
                .withServicePath(MessageFormat.format("/stakeholders/{0}", preparationStakeholder.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(inputStakeholderDTO))
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        StakeholderDTO stakeholderAfter = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/stakeholders/" + preparationStakeholder.getId())
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(StakeholderDTO.class);

        assertEquals(stakeholderBefore.getId(), stakeholderAfter.getId());
        assertEquals(stakeholderBefore.getCreated(), stakeholderAfter.getCreated());
        assertTrue(stakeholderBefore.getUpdated().isBefore(stakeholderAfter.getUpdated()));
        assertEquals(stakeholderBefore.getVersion() + 1, stakeholderAfter.getVersion());
        assertEquals(inputStakeholderDTO.getType(), stakeholderAfter.getType());
        assertEquals(inputStakeholderDTO.getFirstName(), stakeholderAfter.getFirstName());
        assertEquals(inputStakeholderDTO.getLastName(), stakeholderAfter.getLastName());
        assertEquals(inputStakeholderDTO.getAddresses(), stakeholderAfter.getAddresses());
        assertEquals(inputStakeholderDTO.getPersonId(), stakeholderAfter.getPersonId());
        assertEquals(inputStakeholderDTO.getRoles(), stakeholderAfter.getRoles());
        assertEquals(inputStakeholderDTO.getContactInformation(), stakeholderAfter.getContactInformation());
        assertEquals(inputStakeholderDTO.getOrganizationNumber(), stakeholderAfter.getOrganizationNumber());
        assertEquals(inputStakeholderDTO.getOrganizationName(), stakeholderAfter.getOrganizationName());
        assertEquals(inputStakeholderDTO.getAuthorizedSignatory(), stakeholderAfter.getAuthorizedSignatory());
        assertEquals(inputStakeholderDTO.getAdAccount(), stakeholderAfter.getAdAccount());
        assertEquals(inputStakeholderDTO.getExtraParameters(), stakeholderAfter.getExtraParameters());
    }

    @Test
    void testPutStakeholderNotFound() throws JsonProcessingException {
        var input = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));

        setupCall()
                .withHttpMethod(HttpMethod.PUT)
                .withServicePath(MessageFormat.format("/stakeholders/{0}", String.valueOf(new Random().nextLong())))
                .withRequest(OBJECT_MAPPER.writeValueAsString(input))
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();
    }

    @Test
    void testPutStakeholdersOnErrand() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO postErrandDTO = createErrandDTO();
        postErrandDTO.setStakeholders(List.of(createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole()))));
        ErrandDTO errandDTO = postAndGetErrand(postErrandDTO);

        List<StakeholderDTO> stakeholderDTOList = List.of(createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())), createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())), createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())));

        setupCall()
                .withHttpMethod(HttpMethod.PUT)
                .withServicePath(MessageFormat.format("/errands/{0}/stakeholders", errandDTO.getId()))
                .withRequest(OBJECT_MAPPER.writeValueAsString(stakeholderDTOList))
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        var result = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ErrandDTO.class);

        assertEquals(stakeholderDTOList.size(), result.getStakeholders().size());
    }

    @Test
    void testDeleteStakeholderOnErrand() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = createErrandDTO();

        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        List<ErrandDTO> errandListBeforeDelete = findAllErrands();

        Long errandId = errandListBeforeDelete.get(0).getId();
        Long stakeholderId = errandListBeforeDelete.get(0).getStakeholders().get(0).getId();

        Assertions.assertTrue(errandListBeforeDelete.get(0).getStakeholders().stream().anyMatch(stakeholderDTO -> stakeholderDTO.getId().equals(stakeholderId)));
        Assertions.assertTrue(stakeholderRepository.findById(stakeholderId).isPresent());

        setupCall()
                .withHttpMethod(HttpMethod.DELETE)
                .withServicePath(MessageFormat.format("/errands/{0}/stakeholders/{1}", errandId, stakeholderId))
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        List<ErrandDTO> errandListAfterDelete = findAllErrands();

        Assertions.assertFalse(errandListAfterDelete.get(0).getStakeholders().stream().anyMatch(stakeholderDTO -> stakeholderDTO.getId().equals(stakeholderId)));
        Assertions.assertFalse(stakeholderRepository.findById(stakeholderId).isPresent());
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

    @Test
    void testDeleteStakeholderOnErrandNotFound_1() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = createErrandDTO();

        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        List<ErrandDTO> errandListBeforeDelete = findAllErrands();

        Long stakeholderId = errandListBeforeDelete.get(0).getStakeholders().get(0).getId();

        setupCall()
                .withHttpMethod(HttpMethod.DELETE)
                .withServicePath(MessageFormat.format("/errands/{0}/stakeholders/{1}", String.valueOf(new Random().nextInt(100, 10000)), stakeholderId))
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();
    }

    @Test
    void testDeleteStakeholderOnErrandNotFound_2() throws JsonProcessingException, ClassNotFoundException {
        ErrandDTO errandDTO = createErrandDTO();

        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO))
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        List<ErrandDTO> errandListBeforeDelete = findAllErrands();

        Long errandId = errandListBeforeDelete.get(0).getId();

        setupCall()
                .withHttpMethod(HttpMethod.DELETE)
                .withServicePath(MessageFormat.format("/errands/{0}/stakeholders/{1}", errandId, String.valueOf(new Random().nextInt(100, 10000))))
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();
    }

    private ErrandDTO postAndGetErrand(ErrandDTO errandDTO) throws JsonProcessingException, ClassNotFoundException {
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
}

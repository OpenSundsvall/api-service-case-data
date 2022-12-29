package se.sundsvall.casedata;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import generated.client.processengine.ParkingPermitResponse;
import org.apache.commons.lang3.RandomStringUtils;
import se.sundsvall.casedata.api.model.AddressDTO;
import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.ContactInformationDTO;
import se.sundsvall.casedata.api.model.CoordinatesDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.api.model.GetParkingPermitDTO;
import se.sundsvall.casedata.api.model.LawDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.StatusDTO;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;
import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.model.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;
import se.sundsvall.casedata.integration.db.model.enums.FacilityType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.integration.processengine.ProcessEngineClient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static se.sundsvall.dept44.util.DateUtils.toOffsetDateTimeWithLocalOffset;

public class TestUtil {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .registerModule(new JavaTimeModule());

    public static OffsetDateTime getRandomOffsetDateTime() {
        return toOffsetDateTimeWithLocalOffset(OffsetDateTime.now().minusDays(new Random().nextInt(10000)).truncatedTo(ChronoUnit.MILLIS));
    }

    public static ErrandDTO createErrandDTO() {
        ErrandDTO errandDTO = new ErrandDTO();
        errandDTO.setExternalCaseId(UUID.randomUUID().toString());
        errandDTO.setCaseType((CaseType) getRandomOfEnum(CaseType.class));
        errandDTO.setPriority(Priority.HIGH);
        errandDTO.setDescription(RandomStringUtils.random(20, true, false));
        errandDTO.setCaseTitleAddition(RandomStringUtils.random(10, true, false));
        errandDTO.setDiaryNumber(RandomStringUtils.random(10, true, true));
        errandDTO.setPhase(RandomStringUtils.random(10, true, true));
        errandDTO.setStatuses(List.of(createStatusDTO()));
        errandDTO.setMunicipalityId(RandomStringUtils.random(10, false, true));
        errandDTO.setStartDate(LocalDate.now().minusDays(3));
        errandDTO.setEndDate(LocalDate.now().plusDays(10));
        errandDTO.setApplicationReceived(getRandomOffsetDateTime());
        errandDTO.setStakeholders(List.of(
                createStakeholderDTO(StakeholderType.PERSON, List.of(getRandomStakeholderRole(), getRandomStakeholderRole())),
                createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(getRandomStakeholderRole(), getRandomStakeholderRole()))));
        errandDTO.setFacilities(createFacilities(true, List.of(FacilityType.GARAGE)));
        errandDTO.setDecisions(List.of(createDecisionDTO()));
        errandDTO.setAttachments(List.of(createAttachmentDTO(AttachmentCategory.PASSPORT_PHOTO)));
        errandDTO.setNotes(List.of(createNoteDTO(), createNoteDTO(), createNoteDTO()));
        errandDTO.setMessageIds(List.of(RandomStringUtils.random(10, true, true), RandomStringUtils.random(10, true, true), RandomStringUtils.random(10, true, true)));
        errandDTO.setExtraParameters(createExtraParameters());

        return errandDTO;
    }

    public static StakeholderRole getRandomStakeholderRole() {
        return StakeholderRole.values()[new Random().nextInt(StakeholderRole.values().length)];
    }

    public static StakeholderType getRandomStakeholderType() {
        return StakeholderType.values()[new Random().nextInt(StakeholderType.values().length)];
    }

    public static DecisionType getRandomDecisionType() {
        return DecisionType.values()[new Random().nextInt(DecisionType.values().length)];
    }

    public static StatusDTO createStatusDTO() {
        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setStatusType(RandomStringUtils.random(10, true, false));
        statusDTO.setDescription(RandomStringUtils.random(20, true, false));
        statusDTO.setDateTime(getRandomOffsetDateTime());

        return statusDTO;
    }

    public static DecisionDTO createDecisionDTO() {
        DecisionDTO decisionDTO = new DecisionDTO();
        decisionDTO.setDecisionType(getRandomDecisionType());
        decisionDTO.setDecisionOutcome((DecisionOutcome) getRandomOfEnum(DecisionOutcome.class));
        decisionDTO.setDescription(RandomStringUtils.random(30, true, false));
        decisionDTO.setLaw(List.of(createLawDTO()));
        decisionDTO.setDecidedBy(createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())));
        decisionDTO.setDecidedAt(getRandomOffsetDateTime());
        decisionDTO.setValidFrom(getRandomOffsetDateTime());
        decisionDTO.setValidTo(getRandomOffsetDateTime());
        decisionDTO.setAppeal(createAppealDTO());
        decisionDTO.setAttachments(List.of(createAttachmentDTO((AttachmentCategory) getRandomOfEnum(AttachmentCategory.class))));
        decisionDTO.setExtraParameters(createExtraParameters());

        return decisionDTO;
    }

    private static long getRandomId() {
        return new Random().nextLong(1, 100000);
    }

    public static AttachmentDTO createAttachmentDTO(AttachmentCategory category) {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(category);
        attachmentDTO.setName(RandomStringUtils.random(10, true, false) + ".pdf");
        attachmentDTO.setNote(RandomStringUtils.random(20, true, false));
        attachmentDTO.setExtension(".pdf");
        attachmentDTO.setMimeType("application/pdf");
        attachmentDTO.setFile("dGVzdA==");
        attachmentDTO.setExtraParameters(createExtraParameters());

        return attachmentDTO;
    }

    public static AppealDTO createAppealDTO() {
        AppealDTO appealDTO = new AppealDTO();
        appealDTO.setAppealedBy(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT)));
        appealDTO.setJudicialAuthorisation(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.DOCTOR)));
        appealDTO.setAttachments(List.of(createAttachmentDTO(AttachmentCategory.POLICE_REPORT)));
        appealDTO.setExtraParameters(createExtraParameters());

        return appealDTO;
    }

    public static LawDTO createLawDTO() {
        LawDTO lawDTO = new LawDTO();
        lawDTO.setHeading(RandomStringUtils.random(10, true, false));
        lawDTO.setSfs(RandomStringUtils.random(10, true, false));
        lawDTO.setChapter(RandomStringUtils.random(10, true, false));
        lawDTO.setArticle(RandomStringUtils.random(10, true, false));

        return lawDTO;
    }

    public static List<FacilityDTO> createFacilities(boolean oneMainFacility, List<FacilityType> facilityTypes) {
        List<FacilityDTO> facilityList = new ArrayList<>();

        facilityTypes.forEach(facilityType -> {
            FacilityDTO facilityDTO = new FacilityDTO();
            facilityDTO.setFacilityType(facilityType);
            facilityDTO.setMainFacility(oneMainFacility && facilityList.isEmpty());
            facilityDTO.setDescription(RandomStringUtils.random(20, true, false));
            facilityDTO.setFacilityCollectionName(RandomStringUtils.random(10, true, false));

            AddressDTO address = new AddressDTO();
            address.setAddressCategory(AddressCategory.VISITING_ADDRESS);
            Random random = new Random();
            address.setPropertyDesignation(RandomStringUtils.random(20, true, false).toUpperCase() + " " + random.nextInt(99) + ":" + random.nextInt(999));
            facilityDTO.setAddress(address);

            facilityList.add(facilityDTO);
        });

        return facilityList;
    }

    public static Map<String, String> createExtraParameters() {
        Map<String, String> extraParams = new HashMap<>();
        extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));
        extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));
        extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));

        return extraParams;
    }

    public static StakeholderDTO createStakeholderDTO(StakeholderType stakeholderType, List<StakeholderRole> stakeholderRoles) {
        if (stakeholderType.equals(StakeholderType.PERSON)) {
            StakeholderDTO person = new StakeholderDTO();
            person.setType(StakeholderType.PERSON);
            person.setPersonId(UUID.randomUUID().toString());
            person.setAdAccount(RandomStringUtils.random(10, true, false));
            person.setFirstName(RandomStringUtils.random(10, true, false));
            person.setLastName(RandomStringUtils.random(10, true, false));
            person.setRoles(stakeholderRoles);
            person.setContactInformation(List.of(createContactInfo(ContactType.EMAIL), createContactInfo(ContactType.PHONE), createContactInfo(ContactType.CELLPHONE)));
            person.setAddresses(List.of(createAddress(AddressCategory.VISITING_ADDRESS)));
            person.setExtraParameters(createExtraParameters());

            return person;
        } else {
            StakeholderDTO organization = new StakeholderDTO();
            organization.setType(StakeholderType.ORGANIZATION);
            organization.setOrganizationNumber((new Random().nextInt(999999 - 111111) + 111111) + "-" + (new Random().nextInt(9999 - 1111) + 1111));
            organization.setOrganizationName(RandomStringUtils.random(20, true, false));
            organization.setRoles(stakeholderRoles);
            organization.setContactInformation(List.of(createContactInfo(ContactType.EMAIL), createContactInfo(ContactType.PHONE), createContactInfo(ContactType.CELLPHONE)));
            organization.setAddresses(List.of(createAddress(AddressCategory.VISITING_ADDRESS)));
            organization.setExtraParameters(createExtraParameters());
            organization.setAuthorizedSignatory(RandomStringUtils.random(10, true, false));
            organization.setAdAccount(RandomStringUtils.random(10, true, false));

            return organization;
        }
    }

    public static List<StakeholderDTO> createRandomStakeholderDTOList(int numberOfStakeholders) {
        List<StakeholderDTO> stakeholderDTOList = new ArrayList<>();
        for (int i = 0; i < numberOfStakeholders; i++) {
            stakeholderDTOList.add(createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())));
        }
        return stakeholderDTOList;
    }

    public static ContactInformationDTO createContactInfo(ContactType contactType) {
        ContactInformationDTO contactInformationDTO = new ContactInformationDTO();
        contactInformationDTO.setContactType(contactType);
        contactInformationDTO.setValue(RandomStringUtils.random(10, false, true));

        return contactInformationDTO;
    }

    public static AddressDTO createAddress(AddressCategory addressCategory) {
        AddressDTO address = new AddressDTO();
        address.setAddressCategory(addressCategory);
        address.setCity(RandomStringUtils.random(10, true, false));
        address.setCountry("Sverige");
        address.setPropertyDesignation(RandomStringUtils.random(10, true, false));
        address.setStreet(RandomStringUtils.random(10, true, false));
        address.setHouseNumber(RandomStringUtils.random(10, true, false));
        address.setCareOf(RandomStringUtils.random(10, true, false));
        address.setPostalCode(RandomStringUtils.random(10, true, false));
        address.setApartmentNumber(RandomStringUtils.random(10, true, false));
        address.setAttention(RandomStringUtils.random(10, true, false));
        address.setInvoiceMarking(RandomStringUtils.random(10, true, false));
        address.setIsZoningPlanArea(false);
        CoordinatesDTO coordinates = createCoordinatesDTO();
        address.setLocation(coordinates);

        return address;
    }

    public static CoordinatesDTO createCoordinatesDTO() {
        CoordinatesDTO coordinates = new CoordinatesDTO();
        coordinates.setLatitude(new Random().nextDouble());
        coordinates.setLongitude(new Random().nextDouble());
        return coordinates;
    }

    public static NoteDTO createNoteDTO() {
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setTitle(RandomStringUtils.random(10, true, false));
        noteDTO.setText(RandomStringUtils.random(10, true, false));
        noteDTO.setExtraParameters(createExtraParameters());

        return noteDTO;
    }

    public static GetParkingPermitDTO createGetParkingPermitDTO() {
        return GetParkingPermitDTO.builder()
                .errandId(getRandomId())
                .artefactPermitNumber(RandomStringUtils.random(10, true, true))
                .artefactPermitStatus(RandomStringUtils.random(10, true, false))
                .errandDecision(createDecisionDTO())
                .build();
    }

    public static <E extends Enum<E>> Enum<?> getRandomOfEnum(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).toList().get(new Random().nextInt(enumClass.getEnumConstants().length));
    }

    public static void mockStartProcess(ProcessEngineClient processEngineClientMock) {
        ParkingPermitResponse response = new ParkingPermitResponse();
        response.setProcessId(UUID.randomUUID().toString());
        doReturn(response).when(processEngineClientMock).startProcess(any());
    }
}

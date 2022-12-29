package se.sundsvall.casedata.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.integration.db.model.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class PatchErrandDTOTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
        registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(PatchErrandDTO.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        PatchErrandDTO dto = new PatchErrandDTO();
        dto.setExternalCaseId(UUID.randomUUID().toString());
        dto.setCaseType(CaseType.PARKING_PERMIT);
        dto.setPriority(Priority.HIGH);
        dto.setDescription("abc");
        dto.setCaseTitleAddition("abc");
        dto.setDiaryNumber("abc");
        dto.setPhase("Aktualisering");
        dto.setMunicipalityId("123");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());
        dto.setApplicationReceived(TestUtil.getRandomOffsetDateTime());
        dto.setExtraParameters(TestUtil.createExtraParameters());

        assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
    }
}

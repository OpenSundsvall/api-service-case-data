package se.sundsvall.casedata.api.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.TestUtil;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Random;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class NoteDTOTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
        registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(NoteDTO.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        NoteDTO dto = TestUtil.createErrandDTO().getNotes().get(0);
        dto.setId(new Random().nextLong());
        dto.setCreated(OffsetDateTime.now().plusDays(new Random().nextInt()));
        dto.setUpdated(OffsetDateTime.now().plusDays(new Random().nextInt()));
        dto.setUpdatedBy(RandomStringUtils.random(10, true, false));
        dto.setCreatedBy(RandomStringUtils.random(10, true, false));

        assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
    }
}

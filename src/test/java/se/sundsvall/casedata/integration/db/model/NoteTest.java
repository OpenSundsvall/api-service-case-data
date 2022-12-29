package se.sundsvall.casedata.integration.db.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.CoreMatchers.allOf;

class NoteTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(Note.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCodeExcluding("errand"),
                hasValidBeanEqualsExcluding("errand"),
                hasValidBeanToStringExcluding("errand")));
    }

    @Test
    void testFields() {
        List<Note> list = EntityDtoMapper.INSTANCE.dtoToErrand(TestUtil.createErrandDTO()).getNotes();
        list.forEach(note -> {
            note.setId(new Random().nextLong());
            note.setCreated(OffsetDateTime.now().plusDays(new Random().nextInt()));
            note.setUpdated(OffsetDateTime.now().plusDays(new Random().nextInt()));
            note.setUpdatedBy(RandomStringUtils.random(10, true, false));
            note.setCreatedBy(RandomStringUtils.random(10, true, false));
        });

        Assertions.assertThat(list).isNotEmpty();
        list.forEach(facility -> Assertions.assertThat(facility).isNotNull().hasNoNullFieldsOrProperties());
    }
}

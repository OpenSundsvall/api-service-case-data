package se.sundsvall.casedata.integration.db.model;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.time.OffsetDateTime;
import java.util.Random;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.CoreMatchers.allOf;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;

class DecisionTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(Decision.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCodeExcluding("errand"),
                hasValidBeanEqualsExcluding("errand"),
                hasValidBeanToStringExcluding("errand")));
    }

    @Test
    void testFields() {
        Decision object = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO()).getDecisions().get(0);
        object.setId(new Random().nextLong());
        object.setCreated(OffsetDateTime.now().plusDays(new Random().nextInt()));
        object.setUpdated(OffsetDateTime.now().plusDays(new Random().nextInt()));

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }
}

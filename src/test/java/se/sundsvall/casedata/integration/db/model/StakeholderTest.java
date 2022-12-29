package se.sundsvall.casedata.integration.db.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.TestUtil;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class StakeholderTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(Stakeholder.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCodeExcluding("errand"),
                hasValidBeanEqualsExcluding("errand"),
                hasValidBeanToStringExcluding("errand")));
    }

    @Test
    void testFields() {
        Stakeholder object = EntityDtoMapper.INSTANCE.dtoToErrand(TestUtil.createErrandDTO()).getStakeholders().get(0);
        object.setId(new Random().nextLong());
        object.setCreated(OffsetDateTime.now().plusDays(new Random().nextInt()));
        object.setUpdated(OffsetDateTime.now().plusDays(new Random().nextInt()));
        object.setOrganizationName("OrgName");
        object.setOrganizationNumber("12345");
        object.setAuthorizedSignatory("Auth name");

        assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }
}

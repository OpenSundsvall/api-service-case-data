package se.sundsvall.casedata.integration.db.model;

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
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.CoreMatchers.allOf;

class StatusTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(Status.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        List<Status> statusList = EntityDtoMapper.INSTANCE.dtoToErrand(TestUtil.createErrandDTO()).getStatuses();

        Assertions.assertThat(statusList).isNotEmpty();
        statusList.forEach(facility -> Assertions.assertThat(facility).isNotNull().hasNoNullFieldsOrProperties());
    }
}

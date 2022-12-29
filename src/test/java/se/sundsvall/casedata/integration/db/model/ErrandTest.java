package se.sundsvall.casedata.integration.db.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;

class ErrandTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
        registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
    }

    @Test
    void testFields() {
        Errand object = EntityDtoMapper.INSTANCE.dtoToErrand(TestUtil.createErrandDTO());
        object.setId(new Random().nextLong());
        object.setCreated(OffsetDateTime.now().plusDays(new Random().nextInt()));
        object.setUpdated(OffsetDateTime.now().plusDays(new Random().nextInt()));
        object.setProcessId(UUID.randomUUID().toString());
        object.setCreatedBy(RandomStringUtils.random(10, true, false));
        object.setUpdatedBy(RandomStringUtils.random(10, true, false));
        object.setCreatedByClient(RandomStringUtils.random(10, true, false));
        object.setUpdatedByClient(RandomStringUtils.random(10, true, false));
        object.setErrandNumber("PRH-2022-000001");

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }
}

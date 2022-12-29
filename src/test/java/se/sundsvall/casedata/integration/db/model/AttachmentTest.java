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
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.CoreMatchers.allOf;

class AttachmentTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(Attachment.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCodeExcluding("errand"),
                hasValidBeanEqualsExcluding("errand"),
                hasValidBeanToStringExcluding("errand")));
    }

    @Test
    void testFields() {
        List<Attachment> attachmentList = EntityDtoMapper.INSTANCE.dtoToErrand(TestUtil.createErrandDTO()).getAttachments();
        attachmentList.forEach(attachment -> {
            attachment.setId(new Random().nextLong());
            attachment.setCreated(OffsetDateTime.now().plusDays(new Random().nextInt()));
            attachment.setUpdated(OffsetDateTime.now().plusDays(new Random().nextInt()));
        });

        Assertions.assertThat(attachmentList).isNotEmpty();
        attachmentList.forEach(attachment -> Assertions.assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties());
    }
}

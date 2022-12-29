package se.sundsvall.casedata.integration.db.model;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.util.List;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

class AddressTest {

    @Test
    void testBean() {
        MatcherAssert.assertThat(Address.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        List<Address> addressList = EntityDtoMapper.INSTANCE.dtoToErrand(TestUtil.createErrandDTO()).getStakeholders().stream()
                .filter(stakeholder -> stakeholder.getType().equals(StakeholderType.PERSON))
                .map(Stakeholder::getAddresses)
                .findFirst().orElseThrow();

        Assertions.assertThat(addressList).isNotEmpty();
        addressList.forEach(address -> Assertions.assertThat(address).isNotNull().hasNoNullFieldsOrProperties());
    }
}

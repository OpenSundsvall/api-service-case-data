package se.sundsvall.casedata.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.TestUtil;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class CoordinatesDTOTest {

    @Test
    void testBean() {
        MatcherAssert.assertThat(CoordinatesDTO.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        CoordinatesDTO dto = TestUtil.createCoordinatesDTO();

        assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
    }
}

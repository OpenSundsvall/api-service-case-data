package se.sundsvall.casedata.apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.util.List;

import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;

@WireMockAppTestSuite(files = "classpath:/RollbackTest", classes = CaseDataApplication.class)
class RollbackTest extends CustomAbstractAppTest{

    @Autowired
    private ErrandRepository errandRepository;

    // Simulate HTTP 500 response from POST /start-process to ProcessEngine. No errand should be persisted.
    @Test
    void test1_500rollback() throws JsonProcessingException {
        List<Errand> listBefore = errandRepository.findAll();

        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/errands")
                .withRequest(OBJECT_MAPPER.writeValueAsString(createErrandDTO()))
                .withExpectedResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .sendRequestAndVerifyResponse();

        List<Errand> listAfter = errandRepository.findAll();

        // Verify that no errand was persisted
        Assertions.assertEquals(listBefore.size(), listAfter.size());
    }
}

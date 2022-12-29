package se.sundsvall.casedata.apptest;

import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Status;
import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.service.ProcessEngineService;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static se.sundsvall.casedata.apptest.util.TestConstants.PROCESS_ENGINE_URL;

@WireMockAppTestSuite(files = "classpath:/ProcessEngineServiceTest", classes = CaseDataApplication.class)
class ProcessEngineServiceTest {

    @Autowired
    ProcessEngineService processEngineService;

    @Test
    void testServerProblem() {
        Long errandId = 500000000L;
        var problem = Assertions.assertThrows(DefaultProblem.class, () -> processEngineService.startProcess(errandId));

        Assertions.assertEquals(Status.SERVICE_UNAVAILABLE, problem.getStatus());
        Assertions.assertEquals(Constants.PROCESS_ENGINE_PROBLEM_DETAIL, problem.getDetail());
        verify(5, postRequestedFor(urlEqualTo(PROCESS_ENGINE_URL)).withRequestBody(new ContainsPattern(errandId.toString())));
    }

    @Test
    void testClientProblem() {
        Long errandId = 400000000L;
        var problem = Assertions.assertThrows(DefaultProblem.class, () -> processEngineService.startProcess(errandId));

        Assertions.assertEquals(Status.SERVICE_UNAVAILABLE, problem.getStatus());
        Assertions.assertEquals(Constants.PROCESS_ENGINE_PROBLEM_DETAIL, problem.getDetail());
        // Client problems = no reason to retry
        verify(5, postRequestedFor(urlEqualTo(PROCESS_ENGINE_URL)).withRequestBody(new ContainsPattern(errandId.toString())));
    }
}

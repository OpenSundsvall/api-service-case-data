package se.sundsvall.casedata.service;


import generated.client.processengine.CaseObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Status;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.integration.processengine.ProcessEngineClient;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProcessEngineServiceTest {

    @Mock
    private ProcessEngineClient processEngineClientMock;

    @InjectMocks
    private ProcessEngineService processEngineService;

    @Captor
    private ArgumentCaptor<CaseObject> caseObjectArgumentCaptor;

    @Test
    void startProcess() {
        TestUtil.mockStartProcess(processEngineClientMock);

        Long caseId = new Random().nextLong();
        processEngineService.startProcess(caseId);

        verify(processEngineClientMock, times(1)).startProcess(caseObjectArgumentCaptor.capture());
        assertEquals(caseId, Long.valueOf(caseObjectArgumentCaptor.getValue().getCaseNumber()));
        Assertions.assertNull(caseObjectArgumentCaptor.getValue().getProcessInstanceId());
        Assertions.assertNull(caseObjectArgumentCaptor.getValue().getProcessName());
    }

    @Test
    void updateProcess() {
        Long caseId = new Random().nextLong();
        processEngineService.updateProcess(caseId);

        verify(processEngineClientMock, times(1)).updateProcess(caseObjectArgumentCaptor.capture());
        assertEquals(caseId, Long.valueOf(caseObjectArgumentCaptor.getValue().getCaseNumber()));
        Assertions.assertNull(caseObjectArgumentCaptor.getValue().getProcessInstanceId());
        Assertions.assertNull(caseObjectArgumentCaptor.getValue().getProcessName());
    }

    @Test
    void badRequest() {
        doThrow(ClientProblem.class).when(processEngineClientMock).startProcess(any());
        var problem = assertThrows(DefaultProblem.class, () -> processEngineService.startProcess(1L));
        assertEquals(Status.SERVICE_UNAVAILABLE, problem.getStatus());
        assertEquals(Constants.PROCESS_ENGINE_PROBLEM_DETAIL, problem.getDetail());
    }
    @Test
    void internalServerError() {
        doThrow(ServerProblem.class).when(processEngineClientMock).startProcess(any());
        var problem = assertThrows(DefaultProblem.class, () -> processEngineService.startProcess(1L));
        assertEquals(Status.SERVICE_UNAVAILABLE, problem.getStatus());
        assertEquals(Constants.PROCESS_ENGINE_PROBLEM_DETAIL, problem.getDetail());
    }
    @Test
    void exception() {
        doThrow(RuntimeException.class).when(processEngineClientMock).startProcess(any());
        assertThrows(RuntimeException.class, () -> processEngineService.startProcess(1L));
    }
}

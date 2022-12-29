package se.sundsvall.casedata.service;

import generated.client.processengine.CaseObject;
import generated.client.processengine.ParkingPermitResponse;
import org.springframework.stereotype.Service;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.integration.processengine.ProcessEngineClient;

import static se.sundsvall.casedata.service.util.Constants.PROCESS_ENGINE_PROBLEM_DETAIL;

@Service
public class ProcessEngineService {

    public static final ThrowableProblem PROBLEM = Problem.valueOf(Status.SERVICE_UNAVAILABLE, PROCESS_ENGINE_PROBLEM_DETAIL);
    private final ProcessEngineClient processEngineClient;

    public ProcessEngineService(ProcessEngineClient processEngineClient) {
        this.processEngineClient = processEngineClient;
    }

    public ParkingPermitResponse startProcess(Long errandId) {
        try {
            return processEngineClient.startProcess(createCaseObject(errandId));
        } catch (AbstractThrowableProblem e) {
            throw PROBLEM;
        }
    }

    public void updateProcess(Long errandId) {
        try {
            processEngineClient.updateProcess(createCaseObject(errandId));
        } catch (AbstractThrowableProblem e) {
            throw PROBLEM;
        }
    }

    private static CaseObject createCaseObject(Long errandId) {
        CaseObject caseObject = new CaseObject();
        caseObject.setCaseNumber(errandId.toString());
        caseObject.setProcessInstanceId(null);
        caseObject.setProcessName(null);
        return caseObject;
    }
}

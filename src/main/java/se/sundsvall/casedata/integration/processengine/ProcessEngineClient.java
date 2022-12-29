package se.sundsvall.casedata.integration.processengine;

import generated.client.processengine.CaseObject;
import generated.client.processengine.ParkingPermitResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casedata.integration.processengine.configuration.ProcessEngineConfiguration;

@FeignClient(name = ProcessEngineConfiguration.REGISTRATION_ID, url = "${integration.process-engine.url}", configuration = ProcessEngineConfiguration.class)
@CircuitBreaker(name = ProcessEngineConfiguration.REGISTRATION_ID)
public interface ProcessEngineClient {

    @Retry(name = ProcessEngineConfiguration.REGISTRATION_ID)
    @PostMapping(path = "process/start-process", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ParkingPermitResponse startProcess(@RequestBody CaseObject caseObject);

    @Retry(name = ProcessEngineConfiguration.REGISTRATION_ID)
    @PostMapping(path = "/process/update-process", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ParkingPermitResponse updateProcess(@RequestBody CaseObject caseObject);
}

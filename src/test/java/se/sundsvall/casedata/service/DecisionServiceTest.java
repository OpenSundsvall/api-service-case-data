package se.sundsvall.casedata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.javers.core.Javers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.getRandomOfEnum;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @InjectMocks
    DecisionService decisionService;

    @Mock
    private Javers javers;
    @Mock
    private DecisionRepository decisionRepository;

    @Captor
    private ArgumentCaptor<Decision> decisionCaptor;

    @Test
    void putDecisionOnErrand() {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Decision decision = EntityDtoMapper.INSTANCE.dtoToDecision(createDecisionDTO());
        decision.setId(new Random().nextLong());
        errand.addDecision(decision);
        doReturn(Optional.of(decision)).when(decisionRepository).findById(any());

        DecisionDTO putDTO = createDecisionDTO();

        decisionService.put(decision.getId(), putDTO);
        ArgumentCaptor<Decision> decisionArgumentCaptor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepository).save(decisionArgumentCaptor.capture());
        Decision persistedDecision = decisionArgumentCaptor.getValue();

        // Map putDTO to entity-model
        Decision put = EntityDtoMapper.INSTANCE.dtoToDecision(putDTO);

        // Should not change
        assertEquals(decision.getId(), persistedDecision.getId());
        assertEquals(decision.getVersion(), persistedDecision.getVersion());
        assertEquals(decision.getCreated(), persistedDecision.getCreated());
        assertEquals(decision.getUpdated(), persistedDecision.getUpdated());

        // Should change
        assertEquals(put.getDecisionType(), persistedDecision.getDecisionType());
        assertEquals(put.getDecisionOutcome(), persistedDecision.getDecisionOutcome());
        assertEquals(put.getDescription(), persistedDecision.getDescription());
        assertEquals(put.getLaw(), persistedDecision.getLaw());
        assertEquals(put.getDecidedBy(), persistedDecision.getDecidedBy());
        assertEquals(put.getDecidedAt(), persistedDecision.getDecidedAt());
        assertEquals(put.getValidFrom(), persistedDecision.getValidFrom());
        assertEquals(put.getValidTo(), persistedDecision.getValidTo());
        assertEquals(put.getAppeal(), persistedDecision.getAppeal());
        assertEquals(put.getAttachments(), persistedDecision.getAttachments());
        assertEquals(put.getExtraParameters(), persistedDecision.getExtraParameters());
    }

    @Test
    void patchDecisionOnErrand() throws JsonProcessingException {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Decision decision = EntityDtoMapper.INSTANCE.dtoToDecision(createDecisionDTO());
        decision.setId(new Random().nextLong());
        errand.setDecisions(List.of(decision));

        var mockDecision = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(decision), Decision.class);
        mockDecision.setErrand(errand);
        doReturn(Optional.of(mockDecision)).when(decisionRepository).findById(decision.getId());

        PatchDecisionDTO patch = new PatchDecisionDTO();
        patch.setDecisionOutcome((DecisionOutcome) getRandomOfEnum(DecisionOutcome.class));
        patch.setDescription(RandomStringUtils.random(10, true, false));
        patch.setExtraParameters(createExtraParameters());

        decisionService.patch(decision.getId(), patch);
        Mockito.verify(decisionRepository).save(decisionCaptor.capture());
        Decision persistedDecision = decisionCaptor.getValue();

        assertEquals(patch.getDecisionOutcome(), persistedDecision.getDecisionOutcome());
        assertEquals(patch.getDescription(), persistedDecision.getDescription());

        // ExtraParameters should contain all objects
        Map<String, Object> extraParams = new HashMap<>();
        extraParams.putAll(patch.getExtraParameters());
        extraParams.putAll(decision.getExtraParameters());
        assertEquals(extraParams, persistedDecision.getExtraParameters());
    }
}

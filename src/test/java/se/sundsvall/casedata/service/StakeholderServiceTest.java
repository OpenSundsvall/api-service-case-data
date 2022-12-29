package se.sundsvall.casedata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.TestUtil.getRandomOfEnum;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderRole;

@ExtendWith(MockitoExtension.class)
class StakeholderServiceTest {

    @Mock
    private StakeholderRepository stakeholderRepository;

    @InjectMocks
    private StakeholderService stakeholderService;

    @Captor
    private ArgumentCaptor<Stakeholder> stakeholderArgumentCaptor;

    @Test
    void findAllStakeholders() {
        List<Stakeholder> stakeholderList = TestUtil.createRandomStakeholderDTOList(5).stream().map(EntityDtoMapper.INSTANCE::dtoToStakeholder).toList();
        doReturn(stakeholderList).when(stakeholderRepository).findAll();

        List<StakeholderDTO> resultList = stakeholderService.findAllStakeholders();
        Assertions.assertEquals(5, resultList.size());
        verify(stakeholderRepository, times(1)).findAll();
    }

    @Test
    void findAllStakeholders404() {
        List<Stakeholder> stakeholderList = new ArrayList<>();
        doReturn(stakeholderList).when(stakeholderRepository).findAll();

        ThrowableProblem problem = Assertions.assertThrows(ThrowableProblem.class, () -> stakeholderService.findAllStakeholders());
        Assertions.assertEquals(Status.NOT_FOUND, problem.getStatus());
        verify(stakeholderRepository, times(1)).findAll();
    }

    @Test
    void findStakeholdersByRole() {
        List<Stakeholder> stakeholderList = Stream.of(
                        TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER)),
                        TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.DRIVER, StakeholderRole.OPERATOR)))
                .map(EntityDtoMapper.INSTANCE::dtoToStakeholder)
                .toList();

        doReturn(stakeholderList).when(stakeholderRepository).findByRoles(StakeholderRole.DRIVER);

        List<StakeholderDTO> resultList = stakeholderService.findStakeholdersByRole(StakeholderRole.DRIVER);
        Assertions.assertEquals(2, resultList.size());
        verify(stakeholderRepository, times(1)).findByRoles(StakeholderRole.DRIVER);
    }

    @Test
    void findStakeholdersByRole404() {
        List<Stakeholder> stakeholderList = new ArrayList<>();

        doReturn(stakeholderList).when(stakeholderRepository).findByRoles(StakeholderRole.DRIVER);

        ThrowableProblem problem = Assertions.assertThrows(ThrowableProblem.class, () -> stakeholderService.findStakeholdersByRole(StakeholderRole.DRIVER));
        Assertions.assertEquals(Status.NOT_FOUND, problem.getStatus());
        verify(stakeholderRepository, times(1)).findByRoles(StakeholderRole.DRIVER);
    }

    @Test
    void testFindById() {
        Long id = new Random().nextLong();
        var stakeholder = EntityDtoMapper.INSTANCE.dtoToStakeholder(TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT)));
        Mockito.doReturn(Optional.of(stakeholder)).when(stakeholderRepository).findById(id);

        var result = stakeholderService.findById(id);
        assertEquals(EntityDtoMapper.INSTANCE.stakeholderToDto(stakeholder), result);

        verify(stakeholderRepository, times(1)).findById(id);
    }

    @Test
    void testFindByIdNotFound() {
        Long id = new Random().nextLong();
        Mockito.doReturn(Optional.empty()).when(stakeholderRepository).findById(id);

        var problem = assertThrows(ThrowableProblem.class, () -> stakeholderService.findById(id));

        assertEquals(Status.NOT_FOUND, problem.getStatus());
        verify(stakeholderRepository, times(1)).findById(id);
    }

    @Test
    void putStakeholder() throws JsonProcessingException {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Stakeholder stakeholder = EntityDtoMapper.INSTANCE.dtoToStakeholder(createStakeholderDTO((StakeholderType) getRandomOfEnum(StakeholderType.class), List.of(getRandomStakeholderRole())));
        stakeholder.setId(new Random().nextLong());
        errand.addStakeholder(stakeholder);

        var mockStakeholder = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(stakeholder), Stakeholder.class);
        mockStakeholder.setErrand(errand);
        doReturn(Optional.of(mockStakeholder)).when(stakeholderRepository).findById(any());

        StakeholderDTO putDTO = createStakeholderDTO((StakeholderType) getRandomOfEnum(StakeholderType.class), List.of(getRandomStakeholderRole()));

        stakeholderService.put(stakeholder.getId(), putDTO);
        verify(stakeholderRepository).save(stakeholderArgumentCaptor.capture());
        Stakeholder persistedStakeholder = stakeholderArgumentCaptor.getValue();

        assertThat(putDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        "id", "version", "created", "updated")
                .isEqualTo(EntityDtoMapper.INSTANCE.stakeholderToDto(persistedStakeholder));
    }
}

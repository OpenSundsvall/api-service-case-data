package se.sundsvall.casedata.apptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.ErrandService;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("it")
class OptimisticLockingTest {

    @MockBean
    private ErrandRepository errandRepositoryMock;

    @Autowired
    ErrandService errandService;

    @Test
    void patchErrandWithAttachmentOptimisticLockingFailureException() {
        ErrandDTO errandDTO = TestUtil.createErrandDTO();
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(errandDTO);
        errand.setId(new Random().nextLong(1, 1000));
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

        doThrow(OptimisticLockingFailureException.class).when(errandRepositoryMock).save(any());
        Long id = errand.getId();
        AttachmentDTO attachmentDTO = TestUtil.createAttachmentDTO(AttachmentCategory.SIGNATURE);
        Assertions.assertThrows(OptimisticLockingFailureException.class, () -> errandService.patchErrand(id, attachmentDTO));

        verify(errandRepositoryMock, times(5)).save(any(Errand.class));
    }

    @Test
    void patchErrandWithAttachmentOtherException() {
        ErrandDTO errandDTO = TestUtil.createErrandDTO();
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(errandDTO);
        errand.setId(new Random().nextLong(1, 1000));
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

        doThrow(RuntimeException.class).when(errandRepositoryMock).save(any());
        Long id = errand.getId();
        AttachmentDTO attachmentDTO = TestUtil.createAttachmentDTO(AttachmentCategory.SIGNATURE);
        Assertions.assertThrows(RuntimeException.class, () -> errandService.patchErrand(id, attachmentDTO));

        verify(errandRepositoryMock, times(1)).save(any(Errand.class));
    }

    @Test
    void patchErrandWithStakeholderOptimisticLockingFailureException() {
        ErrandDTO errandDTO = TestUtil.createErrandDTO();
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(errandDTO);
        errand.setId(new Random().nextLong(1, 1000));
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

        doThrow(OptimisticLockingFailureException.class).when(errandRepositoryMock).save(any());
        Long id = errand.getId();
        StakeholderDTO stakeholderDTO = TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(TestUtil.getRandomStakeholderRole()));
        Assertions.assertThrows(OptimisticLockingFailureException.class, () -> errandService.patchErrand(id, stakeholderDTO));

        verify(errandRepositoryMock, times(5)).save(any(Errand.class));
    }

    @Test
    void patchErrandWithStakeholderOtherException() throws JsonProcessingException {
        ErrandDTO errandDTO = TestUtil.createErrandDTO();
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(errandDTO);
        errand.setId(new Random().nextLong(1, 1000));
        doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

        doThrow(RuntimeException.class).when(errandRepositoryMock).save(any());

        Long id = errand.getId();
        StakeholderDTO stakeholderDTO = TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(TestUtil.getRandomStakeholderRole()));
        Assertions.assertThrows(RuntimeException.class, () -> errandService.patchErrand(id, stakeholderDTO));

        verify(errandRepositoryMock, times(1)).save(any(Errand.class));
    }
}

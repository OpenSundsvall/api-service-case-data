package se.sundsvall.casedata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
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
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private ProcessEngineService processEngineService;

    @InjectMocks
    private NoteService noteService;

    @Captor
    private ArgumentCaptor<Note> noteCaptor;

    @Test
    void testPatchNoteOnErrand() throws JsonProcessingException {
        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Note note = EntityDtoMapper.INSTANCE.dtoToNote(createNoteDTO());
        note.setId(new Random().nextLong());
        errand.setNotes(List.of(note));

        var mockNote = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(note), Note.class);
        mockNote.setErrand(errand);
        doReturn(Optional.of(mockNote)).when(noteRepository).findById(note.getId());

        NoteDTO patch = new NoteDTO();
        patch.setTitle(RandomStringUtils.random(10, true, false));
        patch.setText(RandomStringUtils.random(10, true, false));
        patch.setExtraParameters(createExtraParameters());

        noteService.patchNote(note.getId(), patch);
        Mockito.verify(noteRepository).save(noteCaptor.capture());
        Note persistedNote = noteCaptor.getValue();

        assertEquals(patch.getTitle(), persistedNote.getTitle());
        assertEquals(patch.getText(), persistedNote.getText());
        assertEquals(patch.getCreatedBy(), persistedNote.getCreatedBy());
        assertEquals(patch.getUpdatedBy(), persistedNote.getUpdatedBy());

        // ExtraParameters should contain all objects
        Map<String, Object> extraParams = new HashMap<>();
        extraParams.putAll(patch.getExtraParameters());
        extraParams.putAll(note.getExtraParameters());
        assertEquals(extraParams, persistedNote.getExtraParameters());
    }

    @Test
    void testPatchNoteNotFound() {
        NoteDTO noteDTO = new NoteDTO();
        var problem = assertThrows(ThrowableProblem.class, () -> noteService.patchNote(1L, noteDTO));

        assertEquals(Status.NOT_FOUND, problem.getStatus());
    }

    @Test
    void testPut() throws JsonProcessingException {

        Errand errand = EntityDtoMapper.INSTANCE.dtoToErrand(createErrandDTO());
        errand.setId(new Random().nextLong(1, 1000));
        Note note = EntityDtoMapper.INSTANCE.dtoToNote(createNoteDTO());
        note.setId(new Random().nextLong());
        errand.setNotes(List.of(note));

        var mockNote = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(note), Note.class);
        mockNote.setErrand(errand);
        doReturn(Optional.of(mockNote)).when(noteRepository).findById(note.getId());

        NoteDTO putDTO = new NoteDTO();
        putDTO.setTitle(RandomStringUtils.random(10, true, false));
        putDTO.setText(RandomStringUtils.random(10, true, false));

        noteService.put(note.getId(), putDTO);
        Mockito.verify(noteRepository).save(noteCaptor.capture());
        Note persistedNote = noteCaptor.getValue();

        assertThat(putDTO)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(
                        "id", "version", "created", "updated")
                .isEqualTo(EntityDtoMapper.INSTANCE.noteToDto(persistedNote));
    }
}

package se.sundsvall.casedata.service;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.service.util.mappers.EntityDtoMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;
import se.sundsvall.casedata.service.util.mappers.PutMapper;

@Service
public class NoteService {
    private static final ThrowableProblem NOTE_NOT_FOUND_PROBLEM = Problem.valueOf(Status.NOT_FOUND, "Note not found");

    private final Javers javers;

    private final NoteRepository noteRepository;
    private final ProcessEngineService processEngineService;

    public NoteService(Javers javers, NoteRepository noteRepository, ProcessEngineService processEngineService) {
        this.javers = javers;
        this.noteRepository = noteRepository;
        this.processEngineService = processEngineService;
    }

    public String findHistory(Long id) {
        QueryBuilder query = QueryBuilder.byInstanceId(id, Note.class).withChildValueObjects();
        Changes changes = javers.findChanges(query.build());
        if (changes.isEmpty()) {
            throw NOTE_NOT_FOUND_PROBLEM;
        }
        return javers.getJsonConverter().toJson(changes);
    }

    public void patchNote(Long id, NoteDTO noteDTO) {
        Note note = getNote(id);
        PatchMapper.INSTANCE.updateNote(note, noteDTO);
        noteRepository.save(note);
        processEngineService.updateProcess(note.getErrand().getId());
    }

    public void put(Long id, NoteDTO dto) {
        var note = getNote(id);
        PutMapper.INSTANCE.putNote(note, dto);
        noteRepository.save(note);
        processEngineService.updateProcess(note.getErrand().getId());
    }

    public NoteDTO findById(Long id) {
        return EntityDtoMapper.INSTANCE.noteToDto(getNote(id));
    }

    private Note getNote(Long id) {
        return noteRepository.findById(id).orElseThrow(() -> NOTE_NOT_FOUND_PROBLEM);
    }
}

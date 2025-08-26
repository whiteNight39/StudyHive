package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Note;
import com.studyhive.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findByNoteRoom(Room noteRoom);
}

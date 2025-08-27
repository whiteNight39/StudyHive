package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Note;
import com.studyhive.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findByNoteRoomAndNoteStatus(Room noteRoom, String noteStatus);

    Optional<Note> findByNoteIdAndNoteStatus(UUID noteId, String noteStatus);

    @Modifying
    @Query("UPDATE Note SET noteStatus = 'DELETED' WHERE noteId = :noteId")
    void deleteByNoteId(@Param("noteId") UUID noteId);
}

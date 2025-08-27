package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Note;
import com.studyhive.model.entity.NoteUpvote;
import com.studyhive.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteUpvoteRepository extends JpaRepository<NoteUpvote, UUID> {

    Optional<NoteUpvote> findByNoteAndNoteUserAndNoteUpvoteStatus(Note note, User noteUser, String noteUpvoteStatus);

    @Modifying
    @Query("UPDATE NoteUpvote SET noteUpvoteStatus = 'DELETED' WHERE noteUpvoteId = :noteUpvoteId")
    void deleteByNoteUpvoteId(@Param("noteUpvoteId") UUID noteUpvoteId);
}

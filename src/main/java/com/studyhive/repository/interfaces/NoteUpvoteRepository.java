package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Note;
import com.studyhive.model.entity.NoteUpvote;
import com.studyhive.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteUpvoteRepository extends JpaRepository<NoteUpvote, UUID> {

    Optional<NoteUpvote> findByNoteAndNoteUser(Note note, User noteUser);
}

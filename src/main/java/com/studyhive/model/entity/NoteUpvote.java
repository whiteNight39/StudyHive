package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "NoteUpvote")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteUpvote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "noteUpvoteId", nullable = false, updatable = false)
    private UUID noteUpvoteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "noteId", nullable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User noteUser;

    @Column(name = "noteUpvoteCreatedAt", nullable = false)
    private Instant noteUpvoteCreatedAt;

    @Column(name = "noteUpvoteUpdatedAt", nullable = false)
    private Instant noteUpvoteUpdatedAt;

    @PrePersist
    void onCreate() {
        noteUpvoteCreatedAt = Instant.now();
        noteUpvoteUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        noteUpvoteUpdatedAt = Instant.now();
    }
}

package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Note")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "noteId", updatable = false, nullable = false)
    private UUID noteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "noteRoomId", nullable = false)
    private Room noteRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "noteCreatedBy", nullable = false)
    private User noteCreatedBy;

    @Column(name = "noteContent", columnDefinition = "text", nullable = false)
    private String noteContent;

    @Column(name = "noteUpvotes", nullable = false)
    private Integer noteUpvotes;

    @Column(name = "noteStatus", nullable = false)
    private String noteStatus = "ACTIVE";

    @Column(name = "noteCreatedAt", nullable = false, updatable = false)
    private Instant noteCreatedAt;

    @Column(name = "noteUpdatedAt", nullable = false)
    private Instant noteUpdatedAt;

    @PrePersist
    void onCreate() {
        noteCreatedAt = Instant.now();
        noteUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        noteUpdatedAt = Instant.now();
    }
}

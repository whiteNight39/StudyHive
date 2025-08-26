package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "NoteTags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "noteTagId", nullable = false, updatable = false)
    private UUID noteTagId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "noteTagNoteId", nullable = false)
    private Note noteTagNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "noteTagTagId", nullable = false)
    private Tag noteTagTag;
}

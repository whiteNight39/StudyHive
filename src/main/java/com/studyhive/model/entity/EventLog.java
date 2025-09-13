package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "EventLogs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "eventLogId", nullable = false, updatable = false)
    private UUID eventLogId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eventLogUserId", referencedColumnName = "userId", nullable = false)
    private User eventLogUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventLogRoomId", referencedColumnName = "roomId")
    private Room eventLogRoom; // optional, may be null

    @Column(name = "eventLogEventType", nullable = false)
    private String eventLogEventType; // LOGIN, NOTE_CREATED, MESSAGE_SENT, etc.

    @Builder.Default
    @Column(name = "eventLogStatus", nullable = false)
    private String eventLogStatus = "ACTIVE"; // default

    @Column(name = "eventLogTimestamp", nullable = false, updatable = false)
    private Instant eventLogTimestamp;

    @PrePersist
    void onCreate() {
        eventLogTimestamp = Instant.now();
    }
}

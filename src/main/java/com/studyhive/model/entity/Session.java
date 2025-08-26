package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sessionId", nullable = false, updatable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessionRoomId", nullable = false)
    private Room sessionRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessionScheduleddBy", nullable = false)
    private User sessionScheduleddBy;

    @Column(name = "sessionTitle", nullable = false)
    private String sessionTitle;

    @Column(name = "sessionDescription")
    private String sessionDescription;

    @Column(name = "sessionStatus")
    private String sessionStatus = "ACTIVE";

    @Column(name = "sessionStartTime", nullable = false)
    private Instant sessionStartTime;

    @Column(name = "sessionEndTime", nullable = false)
    private Instant sessionEndTime;

    @Column(name = "sessionCreatedAt", nullable = false, updatable = false)
    private Instant sessionCreatedAt;

    @Column(name = "sessionUpdatedAt")
    private Instant sessionUpdatedAt;

    @PrePersist
    void onCreate() {
        sessionCreatedAt = Instant.now();
        sessionUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        sessionUpdatedAt = Instant.now();
    }
}

package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notificationId", nullable = false, updatable = false)
    private UUID notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notificationUserId", referencedColumnName = "userId", nullable = false)
    private User notificationUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notificationRoomId", referencedColumnName = "roomId", nullable = false)
    private Room notificationRoom; // optional

    @Column(name = "notificationMessage", nullable = false)
    private String notificationMessage;

    @Column(name = "notificationIsRead", nullable = false)
    private Boolean notificationIsRead = false; // default false

    @Builder.Default
    @Column(name = "notificationStatus", nullable = false)
    private String notificationStatus = "ACTIVE"; // default value

    @Column(name = "notificationCreatedAt", nullable = false, updatable = false)
    private Instant notificationCreatedAt;

    @Column(name = "notificationUpdatedAt")
    private Instant notificationUpdatedAt;

    @PrePersist
    void onCreate() {
        notificationCreatedAt = Instant.now();
        notificationUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        notificationUpdatedAt = Instant.now();
    }
}

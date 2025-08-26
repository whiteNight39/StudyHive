package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "RoomJoinRequest")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "roomJoinRequestId", updatable = false, nullable = false)
    private UUID roomJoinRequestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roomJoinRequestUserId", nullable = false)
    private User roomJoinRequestUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roomJoinRequestRoomId", nullable = false)
    private Room roomJoinRequestRoom;

    @Column(name = "roomJoinRequestStatus", nullable = false)
    private String roomJoinRequestStatus = "PENDING";

    @Column(name = "roomJoinRequestCreatedAt", nullable = false, updatable = false)
    private Instant roomJoinRequestCreatedAt;

    @Column(name = "roomJoinRequestUpdatedAt", nullable = false)
    private Instant roomJoinRequestUpdatedAt;

    @Column(name = "roomJoinRequestReviewedAt")
    private Instant roomJoinRequestReviewedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "roomJoinReqestReviewedBy", nullable = true)
    private User roomJoinRequestReviewedBy;

    @PrePersist
    void onCreate() {
        roomJoinRequestCreatedAt = Instant.now();
        roomJoinRequestUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        roomJoinRequestUpdatedAt = Instant.now();
    }
}

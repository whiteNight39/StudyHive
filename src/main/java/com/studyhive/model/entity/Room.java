package com.studyhive.model.entity;

import com.studyhive.model.enums.RoomPrivacy;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Room")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "roomId", updatable = false, nullable = false)
    private UUID roomId;

    @Column(name = "roomName", nullable = false)
    private String roomName;

    @Column(name = "roomDescription")
    private String roomDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roomCreatedBy", nullable = false)
    private User roomCreatedBy;

    @Column(name = "roomStatus", nullable = false)
    private String roomStatus = "ACTIVE";

    @Column(name = "roomCreatedAt", updatable = false, nullable = false)
    private Instant roomCreatedAt;

    @Column(name = "roomUpdatedAt", nullable = false)
    private Instant roomUpdatedAt;

    @Column(name = "roomSize", nullable = false)
    private Integer roomSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "roomPrivacy", nullable = false)
    private RoomPrivacy roomPrivacy;

    @PrePersist
    void onCreate() {
            roomCreatedAt = Instant.now();
            roomUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        roomUpdatedAt = Instant.now();
    }
}

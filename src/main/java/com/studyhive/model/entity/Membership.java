package com.studyhive.model.entity;

import com.studyhive.model.enums.RoleInRoom;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Membership")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "membershipId", updatable = false, nullable = false)
    private UUID membershipId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membershipUserId", nullable = false)
    private User membershipUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membershipRoomId", nullable = false)
    private Room membershipRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "membershipRoleInRoom", nullable = false)
    private RoleInRoom membershipRoleInRoom; // OWNER, MEMBER, TUTOR

    @Column(name = "membershipStatus", nullable = false)
    private String membershipStatus = "ACTIVE";

    @Column(name = "membershipCreatedAt", nullable = false, updatable = false)
    private Instant membershipCreatedAt;

    @Column(name = "membershipUpdatedAt", nullable = false)
    private Instant membershipUpdatedAt;

    @PrePersist
    void onCreate() {
        membershipCreatedAt = Instant.now();
        membershipUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        membershipUpdatedAt = Instant.now();
    }
}

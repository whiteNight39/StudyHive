package com.studyhive.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "userId", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "userEmail", unique = true, nullable = false)
    private String userEmail;

    @Column(name = "userPassword", nullable = false)
    private String userPassword;

    @Column(name = "userName", unique = true)
    private String userName;

    @Column(name = "userFirstName")
    private String userFirstName;

    @Column(name = "userLastName")
    private String userLastName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userRoleId", referencedColumnName = "roleId", nullable = false)
    @JsonIgnore
    private Role userRole;

    @Builder.Default
    @Column(name = "userCreditScore", nullable = false)
    private int userCreditScore = 0;

    @Builder.Default
    @Column(name = "userMaxRooms", nullable = false)
    private int userMaxRooms = 2;   // default: can only create 1 room

    @Builder.Default
    @Column(name = "userStatus", nullable = false)
    private String userStatus = "ACTIVE";

    @Column(name = "userCreatedAt", nullable = false, updatable = false)
    private Instant userCreatedAt;

    @Column(name = "userUpdatedAt", nullable = false)
    private Instant userUpdatedAt;

    @PrePersist
    void onCreate() {
            userCreatedAt = Instant.now();
            userUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        userUpdatedAt = Instant.now();
    }
}

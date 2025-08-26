package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Role") // matches your ERD; see note below about quoted identifiers
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "roleId", updatable = false, nullable = false)
    private UUID roleId;

    @Column(name = "roleName", nullable = false, unique = true)
    private String roleName;

    @Column(name = "roleDescription")
    private String roleDescription;

    @Column(name = "roleStatus", nullable = false)
    private String roleStatus = "ACTIVE";

    @Column(name = "roleCreatedAt", nullable = false, updatable = false)
    private Instant roleCreatedAt;

    @PrePersist
    void onCreate() {
        if (roleCreatedAt == null) roleCreatedAt = Instant.now();
    }
}

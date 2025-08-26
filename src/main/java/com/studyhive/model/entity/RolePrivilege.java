package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "RolePrivilege")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePrivilege {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rolePrivilegeId", updatable = false, nullable = false)
    private UUID rolePrivilegeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rolePrivilegeRoleId", nullable = false)
    private Role rolePrivilegeRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rolePrivilegePrivilegeId", nullable = false)
    private Privilege rolePrivilegePrivilege;

    @Column(name = "rolePrivilegeCreatedAt", nullable = false, updatable = false)
    private Instant rolePrivilegeCreatedAt;

    @PrePersist
    void onCreate() {
        if (rolePrivilegeCreatedAt == null) {
            rolePrivilegeCreatedAt = Instant.now();
        }
    }
}

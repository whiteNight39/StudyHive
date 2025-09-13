package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Privilege")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "privilegeId", updatable = false, nullable = false)
    private UUID privilegeId;

    @Column(name = "privilegeName", unique = true, nullable = false)
    private String privilegeName;

    @Column(name = "privilegeDescription")
    private String privilegeDescription;

    @Builder.Default
    @Column(name = "privilegeStatus", nullable = false)
    private String privilegeStatus = "ACTIVE";

    @Column(name = "privilegeCreatedAt", nullable = false, updatable = false)
    private Instant privilegeCreatedAt;

    @PrePersist
    void onCreate() {
        if (privilegeCreatedAt == null) privilegeCreatedAt = Instant.now();
    }
}

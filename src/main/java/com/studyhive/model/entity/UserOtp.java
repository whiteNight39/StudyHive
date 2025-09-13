package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "UserOtp")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "userOtpId", updatable = false, nullable = false)
    private UUID userOtpId;

    @Column(name = "userOtpOtp", updatable = false, nullable = false)
    private String userOtpOtp;

    @Builder.Default
    @Column(name = "userOtpStatus", nullable = false)
    private String userOtpStatus = "ACTIVE";

    @Column(name = "userOtpReason")
    private String userOtpReason;

    @Column(name = "userOtpUserEmail")
    private String userOtpUserEmail;

    @Column(name = "userOtpExpiresAt", updatable = false, nullable = false)
    private Instant userOtpExpiresAt;

    @Column(name = "userOtpCreatedAt", updatable = false, nullable = false)
    private Instant userOtpCreatedAt;

    @PrePersist
    void onCreate() {
        if (userOtpCreatedAt == null) {
            userOtpCreatedAt = Instant.now();
        }
        if (userOtpStatus == null) {
            userOtpStatus = "ACTIVE";
        }
    }
}
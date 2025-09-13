package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "UserLoginJwt")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginJwt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "userLoginJwtId", updatable = false, nullable = false)
    private UUID userLoginJwtId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userLoginJwtUserId", referencedColumnName = "userId", nullable = false)
    private User user;

    @Column(name = "userLoginJwtToken", nullable = false)
    private String userLoginJwtToken;

    @Column(name = "userLoginJwtIssuedAt", nullable = false)
    private Instant userLoginJwtIssuedAt;

    @Column(name = "userLoginJwtExpiresAt", nullable = false)
    private Instant userLoginJwtExpiresAt;

    @Column(name = "userLoginJwtDeviceIp")
    private String userLoginJwtDeviceIp;

    @Column(name = "userLoginJwtUserAgent")
    private String userLoginJwtUserAgent;
}

package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "messageId", nullable = false, updatable = false)
    private UUID messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "messageRoomId")
    private Room messageRoom; // FK → Room, optional

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "messageSenderId", nullable = false)
    private User messageSender; // FK → Sender

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "messageRecipientId")
    private User messageRecipient; // FK → Receiver, optional

    @Column(name = "messageType", nullable = false)
    private String messageType; // TEXT | FILE

    @Column(name = "messageText", columnDefinition = "text")
    private String messageText;

    @Column(name = "messageFileUrl")
    private String messageFileUrl;

    @Column(name = "messageStatus", nullable = false)
    private String messageStatus = "SENT"; // SENT | DELIVERED | READ

    @Column(name = "messageCreatedAt", nullable = false, updatable = false)
    private Instant messageCreatedAt;

    @Column(name = "messageUpdatedAt")
    private Instant messageUpdatedAt;

    @PrePersist
    void onCreate() {
        messageCreatedAt = Instant.now();
        messageUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        messageUpdatedAt = Instant.now();
    }
}

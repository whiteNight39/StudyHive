package com.studyhive.model.entity;

import com.studyhive.model.enums.FileType;
import com.studyhive.model.enums.MessageType;
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
    @JoinColumn(name = "messageRoomId", referencedColumnName = "roomId")
    private Room messageRoom; // FK → Room, optional

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "messageSenderId", referencedColumnName = "userId", nullable = false)
    private User messageSender; // FK → Sender

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "messageRecipientId", referencedColumnName = "userId")
    private User messageRecipient; // FK → Receiver, optional

    @Enumerated(EnumType.STRING)
    @Column(name = "messageType", nullable = false)
    private MessageType messageType; // TEXT | FILE

    @Enumerated(EnumType.STRING)
    @Column(name = "messageFileType", nullable = false)
    private FileType messageFileType; // TEXT | FILE

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "messageReplyingToId", referencedColumnName = "messageId")
    private Message messageReplyingTo;

    @Column(name = "messageText", columnDefinition = "text")
    private String messageText;

    @Column(name = "messageFileUrl")
    private String messageFileUrl;

    @Column(name = "messageFileName")
    private String messageFileName;

    @Builder.Default
    @Column(name = "messageEdited", nullable = false)
    private Boolean messageEdited = false;

    @Builder.Default
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

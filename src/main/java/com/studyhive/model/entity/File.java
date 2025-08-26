package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "fileId", nullable = false, updatable = false)
    private UUID fileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fileRoomId", nullable = false)
    private Room fileRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fileUploadedBy", nullable = false)
    private User fileUploadedBy;

    @Column(name = "fileName", nullable = false)
    private String fileName;

    @Column(name = "filePath", nullable = false)
    private String filePath;

    @Column(name = "fileType")
    private String fileType;

    @Column(name = "fileStatus")
    private String fileStatus = "ACTIVE";

    @Column(name = "fileCreatedAt", nullable = false, updatable = false)
    private Instant fileCreatedAt;

    @Column(name = "fileUpdatedAt")
    private Instant fileUpdatedAt;

    @PrePersist
    void onCreate() {
        fileCreatedAt = Instant.now();
        fileUpdatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        fileUpdatedAt = Instant.now();
    }
}

package com.studyhive.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "Tags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tagId", nullable = false, updatable = false)
    private UUID tagId;

    @Column(name = "tagName", nullable = false, unique = true)
    private String tagName;
}

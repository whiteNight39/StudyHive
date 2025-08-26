package com.studyhive.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {

    private UUID noteId;
    private String noteContent;
    private String noteCreatedByUserName;
    private Instant noteTimestamp;
}

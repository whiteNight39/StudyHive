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
public class RoomResponse {

    private UUID roomId;
    private String roomName;
    private String roomDescription;
    private Integer roomCapacity;
    private String roomCreatedByUserName;
    private Instant roomCreatedAt;
}

package com.studyhive.model.request;

import com.studyhive.model.enums.RoomPrivacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateRequest {

    private UUID roomId;
    private String roomName;
    private String roomDescription;
    private RoomPrivacy roomPrivacy;
}

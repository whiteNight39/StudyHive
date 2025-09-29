package com.studyhive.model.response;

import com.studyhive.model.enums.RoleInRoom;
import com.studyhive.model.enums.RoomPrivacy;
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
public class UserRoomResponse {

    private UUID userRoomsRoomId;
    private String userRoomsRoomName;
    private String userRoomsRoomDescription;
    private UUID userRoomsRoomCreatedBy;
    private Instant userRoomsRoomCreatedAt;
    private Integer userRoomsRoomSize;
    private RoomPrivacy userRoomsRoomPrivacy;
    private RoleInRoom userRoomsRoomRole;
}

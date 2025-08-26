package com.studyhive.model.response;

import com.studyhive.model.enums.RoleInRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomMembershipListResponse {

    private UUID userId;
    private String userName;
    private RoleInRoom userRoleInRoom;
    private String userStatusInRoom;
}

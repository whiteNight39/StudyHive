package com.studyhive.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGlobalProfileResponse {

    private UUID userId;
    private String userName;
    private List<UserRoomInfo> userRoomInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoomInfo {
        private String roomName;
        private String roomRole;
    }
}

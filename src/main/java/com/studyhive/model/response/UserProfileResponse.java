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
public class UserProfileResponse {

    private UUID userId;
    private String userName;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private Integer userMaxRooms;
    private Integer userCreditScore;
    private List<UserRoomInfo> userRoomInfo;
//    private List<String> userRoomsRoomRole;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoomInfo {
        private String roomName;
        private String roomRole;
    }
}

package com.studyhive.model.interfaces;

import java.util.UUID;

public interface UserProfileResponses {

        UUID getUserId();
        String getUserName();
        String getUserFirstName();
        String getUserLastName();
        String getUserEmail();
        Integer getUserMaxRooms();
        Integer getUserCreditScore();
        String getUserRoomsRoomName();
        String getUserRoomsRoomRole();
}

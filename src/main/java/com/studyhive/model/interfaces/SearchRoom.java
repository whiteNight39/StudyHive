package com.studyhive.model.interfaces;

import com.studyhive.model.enums.RoomPrivacy;


import java.time.Instant;
import java.util.UUID;

public interface SearchRoom {
    UUID getRoomId();
    String getRoomName();
    String getRoomDescription();
    Integer getRoomSize();
    RoomPrivacy getRoomPrivacy();
    String getRoomOwner();
    Instant getRoomCreatedAt();
    Boolean getUserIsMember();
}

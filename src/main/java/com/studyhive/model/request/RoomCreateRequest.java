package com.studyhive.model.request;

import com.studyhive.model.enums.RoomPrivacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateRequest {

    private String roomName;
    private String roomDescription;
    private RoomPrivacy roomPrivacy;
}

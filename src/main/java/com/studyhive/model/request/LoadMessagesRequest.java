package com.studyhive.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadMessagesRequest {

    private UUID messageRoomId;
    private UUID messageRecipientId;
    private Integer messageSize;
    private UUID oldestMessageId;
    private Integer messagePage;
}

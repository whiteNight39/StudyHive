package com.studyhive.model.request;

import com.studyhive.model.entity.Message;
import com.studyhive.model.entity.Room;
import com.studyhive.model.entity.User;
import com.studyhive.model.enums.FileType;
import com.studyhive.model.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest {

    private UUID messageRoomId;
    private UUID messageRecipientId;
    private MessageType messageType;
    private UUID messageReplyingToId;
    private String messageText;
    private String messageFileUrl;
    private String messageFileName;
    private FileType messageFileType;
}

package com.studyhive.controller;

import com.studyhive.model.entity.Message;
import com.studyhive.model.request.LoadMessagesRequest;
import com.studyhive.model.request.MessageUpdateRequest;
import com.studyhive.model.response.BaseResponse;

import java.util.UUID;

import com.studyhive.model.request.MessageCreateRequest;
import com.studyhive.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessagingTemplate template;
    private final MessageService messageService;

    @MessageMapping("/message.send") // clients send to /app/message.send
    public void sendMessage(@Payload MessageCreateRequest request, Principal principal) {
        // principal.getName() is userId (because JwtChannelInterceptor set it)
        UUID senderId = UUID.fromString(principal.getName());

        // Persist message (service should return a MessageResponse DTO)
        BaseResponse<UUID> sentMessage = messageService.sendMessage(request, senderId);

        // Broadcast according to message type:
        // If room message -> broadcast to room topic
        if (request.getMessageRoomId() != null) {
            template.convertAndSend("/topic/room." + request.getMessageRoomId(), sentMessage);
        } else if (request.getMessageRecipientId() != null) {
            // private message: send to specific user queue (user destination)
            template.convertAndSendToUser(
                    request.getMessageRecipientId().toString(),
                    "/queue/messages",
                    sentMessage
            );
        }
    }

    @MessageMapping("/message.update") // clients send to /app/message.update
    public void updateMessage(@Payload MessageUpdateRequest request, Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());

        // Persist the update and get the updated DTO
        BaseResponse<Message> updatedMessage = messageService.updateMessage(request, senderId);

        // Broadcast to the same targets as sendMessage
        if (updatedMessage.getResponseData().getMessageRoom().getRoomId() != null) {
            template.convertAndSend("/topic/room." + updatedMessage.getResponseData().getMessageRoom().getRoomId(), updatedMessage);
        } else if (updatedMessage.getResponseData().getMessageRecipient().getUserId() != null) {
            template.convertAndSendToUser(
                    updatedMessage.getResponseData().getMessageRecipient().getUserId().toString(),
                    "/queue/messages",
                    updatedMessage
            );
        }
    }

    @MessageMapping("/message.delete")
    public void deleteMessage(@Payload UUID messageId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());

        BaseResponse<?> deletedMessageId = messageService.deleteMessage(messageId, userId);

//        Do I need to broadcast a deleted message as well..?
    }

    @MessageMapping("/message.view")
    public void viewMessage(@Payload UUID messageId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());

        BaseResponse<Message> viewedMessage = messageService.viewMessage(messageId, userId);

        // Broadcast to the same targets as sendMessage
        if (viewedMessage.getResponseData().getMessageRoom().getRoomId() != null) {
            template.convertAndSend("/topic/room." + viewedMessage.getResponseData().getMessageRoom().getRoomId(), viewedMessage);
        } else if (viewedMessage.getResponseData().getMessageRecipient().getUserId() != null) {
            template.convertAndSendToUser(
                    viewedMessage.getResponseData().getMessageRecipient().getUserId().toString(),
                    "/queue/messages",
                    viewedMessage
            );
        }
    }

    @MessageMapping("/message.load")
    public void loadMessage(@Payload LoadMessagesRequest request, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());

        
    }

}


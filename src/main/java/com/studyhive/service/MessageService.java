package com.studyhive.service;

import com.studyhive.model.entity.*;
import com.studyhive.model.enums.MessageType;
import com.studyhive.model.enums.RoleInRoom;
import com.studyhive.model.request.LoadMessagesRequest;
import com.studyhive.model.request.MessageCreateRequest;
import com.studyhive.model.request.MessageUpdateRequest;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.repository.interfaces.MembershipRepository;
import com.studyhive.repository.interfaces.MessageRepository;
import com.studyhive.repository.interfaces.RoomRepository;
import com.studyhive.repository.interfaces.UserRepository;
import com.studyhive.util.exception.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public MessageService(MessageRepository messageRepository, RoomRepository roomRepository, UserRepository userRepository, MembershipRepository membershipRepository) {
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public BaseResponse<UUID> sendMessage(MessageCreateRequest request, UUID userId) {
        if (request == null)
            throw new ApiException("11", "Request is null", null);

        boolean roomMessage;
        Room room = null;
        User recipient = null;

        // Ensure not both recipient and room are set
        if (request.getMessageRecipientId() != null && request.getMessageRoomId() != null) {
            throw new ApiException("11", "Message cannot have both recipientId and roomId", null);
        }

        // Determine context
        if (request.getMessageRecipientId() != null) {
            recipient = userRepository.getByUserIdAndUserStatus(request.getMessageRecipientId(), "ACTIVE")
                    .orElseThrow(() -> new ApiException("44", "Recipient not found", null));
            roomMessage = false;
        } else if (request.getMessageRoomId() != null) {
            room = roomRepository.findRoomByRoomIdAndRoomStatus(request.getMessageRoomId(), "ACTIVE")
                    .orElseThrow(() -> new ApiException("44", "Room not found", null));
            roomMessage = true;
        } else {
            throw new ApiException("11", "Either recipientId or roomId is required", null);
        }

        // Validate sender
        User sender = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        // Membership check only for room messages
        if (roomMessage) {
            membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                    room, sender, "ACTIVE")
                    .orElseThrow(() -> new ApiException("33", "Sender is not a member of this room", null));
        }

        // Handle replying to a previous message
        Message replyingTo = null;
        if (request.getMessageReplyingToId() != null) {
            replyingTo = messageRepository.findByMessageIdAndMessageStatus(
                    request.getMessageReplyingToId(), "ACTIVE")
                    .orElseThrow(() -> new ApiException("44", "Replying-to message not found", null));
        }

        // Validate file requirements
        if (request.getMessageType() == MessageType.FILE) {
            if (request.getMessageFileUrl() == null || request.getMessageFileType() == null) {
                throw new ApiException("11", "File messages must include fileUrl and fileType", null);
            }
        }

        // Build message
        Message message = Message.builder()
                .messageType(request.getMessageType())
                .messageSender(sender)
                .messageReplyingTo(replyingTo)
                .messageText(request.getMessageText())
                .messageFileUrl(request.getMessageFileUrl())
                .messageFileName(request.getMessageFileName())
                .messageFileType(request.getMessageFileType())
                .build();

        if (roomMessage) {
            message.setMessageRoom(room);
        } else {
            message.setMessageRecipient(recipient);
        }

        messageRepository.save(message);

        return new BaseResponse<>("00", "Message sent", message.getMessageId());
    }

    @Transactional
    public BaseResponse<UUID> updateMessage(MessageUpdateRequest request, UUID userId) {
        if  (request == null) throw new ApiException("11", "Request is null", null);

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        Message message = messageRepository.findByMessageIdAndMessageStatus(
                request.getMessageId(), "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Message not found", null));

        if (!message.getMessageSender().getUserId().equals(userId)) {
            throw new ApiException("33", "User is not permitted to update this message", null);
        }

        if (request.getMessageText() != null && !request.getMessageText().isBlank()) {
            message.setMessageText(request.getMessageText());
            message.setMessageEdited(true);
        }
        messageRepository.save(message);

        return new BaseResponse<>("00", "Message updated", message.getMessageId());
    }

    @Transactional
    public BaseResponse<?> deleteMessage(UUID messageId, UUID userId) {
        if (messageId == null) throw new ApiException("11", "Message id is null", null);

        boolean roomMessage = false;
        Room room = null;

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));
        Message message = messageRepository.findByMessageIdAndMessageStatus(
                messageId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Message not found", null));
        User messageOwner = message.getMessageSender();

//      Determine context
        if (message.getMessageRoom() != null) {
            room = roomRepository.findRoomByRoomIdAndRoomStatus(
                    message.getMessageRoom().getRoomId(), "ACTIVE")
                    .orElseThrow(() -> new ApiException("44", "Room not found", null));
            roomMessage = true;
        }

        if (roomMessage) {
            Membership membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                    room, user, "ACTIVE")
                    .orElseThrow(() -> new ApiException("33", "User is not a member of this room", null));

            if (!messageOwner.getUserId().equals(userId)) {
                if (isNotOwnerOrAdmin(membership)) throw new ApiException("22", "User is not permitted to delete this message", null);
                messageOwner.setUserCreditScore(Math.max(0, messageOwner.getUserCreditScore() - 3));
            }
        }
        if (!roomMessage && !messageOwner.getUserId().equals(userId)) {
            throw new ApiException("22", "User is not permitted to delete this message", null);
        }


        messageRepository.deleteByMessageId(message.getMessageId());

        return new BaseResponse<>("00", "Message deleted", message.getMessageId());
    }

    public BaseResponse<?> viewMessage(UUID messageId, UUID userId) {
        if (messageId == null) throw new ApiException("11", "Message id is null", null);

        boolean roomMessage = false;
        Room room = null;

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));
        Message message = messageRepository.findByMessageIdAndMessageStatus(
                        messageId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Message not found", null));

        if (message.getMessageRoom() != null) {
            room = roomRepository.findRoomByRoomIdAndRoomStatus(
                            message.getMessageRoom().getRoomId(), "ACTIVE")
                    .orElseThrow(() -> new ApiException("44", "Room not found", null));
            roomMessage = true;
        }

        if (roomMessage) {
            Membership membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                            room, user, "ACTIVE")
                    .orElseThrow(() -> new ApiException("33", "User is not a member of this room", null));
        }

        return new BaseResponse<Message>("00", "Message", message);
    }

    public BaseResponse<?> loadMessages(LoadMessagesRequest request, UUID userId) {
        if (request == null) throw new ApiException("11", "Request is null", null);

        boolean roomMessage = false;
        Room room = null;
        Page<Message> messagePage = null;

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        if (request.getMessageRoomId() != null) {
            room = roomRepository.findRoomByRoomIdAndRoomStatus(
                            request.getMessageRoomId(), "ACTIVE")
                    .orElseThrow(() -> new ApiException("44", "Room not found", null));
            roomMessage = true;
        }
        if (roomMessage) {
            Membership membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                            room, user, "ACTIVE")
                    .orElseThrow(() -> new ApiException("33", "User is not a member of this room", null));
        }

        Pageable pageable = PageRequest.of(request.getMessagePage(), request.getMessageSize());
        if (roomMessage) {
            if (request.getMessageRecipientId() == null) throw new ApiException("11", "Message recipient id is null", null);
            User messageRecipient = userRepository.getByUserIdAndUserStatus(request.getMessageRecipientId(), "ACTIVE")
                    .orElseThrow(() -> new ApiException("44", "User not found", null));
            messagePage = messageRepository.loadPersonalMessages(user, messageRecipient, pageable);
        } else {
            messagePage = messageRepository.loadRoomMessages(user, room, pageable);
        }

        return new BaseResponse<>(
                "00",
                "Messages",
                Map.of(
                        "content", messagePage.getContent(),
                        "currentPage", messagePage.getNumber(),
                        "totalPages", messagePage.getTotalPages(),
                        "totalElements", messagePage.getTotalElements(),
                        "pageSize", messagePage.getSize()
                )
        );
    }

    private boolean isNotOwnerOrAdmin(Membership membership) {
        RoleInRoom role = membership.getMembershipRoleInRoom();
        return role != RoleInRoom.OWNER && role != RoleInRoom.ADMIN;
    }

}

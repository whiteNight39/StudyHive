package com.studyhive.service;

import com.studyhive.model.entity.Membership;
import com.studyhive.model.entity.Room;
import com.studyhive.model.entity.User;
import com.studyhive.model.enums.RoleInRoom;
import com.studyhive.model.enums.RoomPrivacy;
import com.studyhive.model.request.RoomCreateRequest;
import com.studyhive.model.request.RoomUpdateRequest;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.model.response.RoomMembershipListResponse;
import com.studyhive.model.response.UserRoomProfileResponse;
import com.studyhive.repository.interfaces.MembershipRepository;
import com.studyhive.repository.interfaces.RoomRepository;
import com.studyhive.repository.interfaces.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository, MembershipRepository membershipRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public BaseResponse createRoom(RoomCreateRequest request, UUID userId) {
        if (request == null) throw new IllegalArgumentException("request cannot be null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        List<Room> userRooms = roomRepository.getByRoomCreatedBy(user);
        if (userRooms.size() >= user.getUserMaxRooms()) {
            throw new IllegalArgumentException("user rooms exceeds maximum");
        }

        Room room = Room.builder()
                .roomName(request.getRoomName())
                .roomDescription(request.getRoomDescription())
                .roomPrivacy(request.getRoomPrivacy())
                .roomCreatedBy(user)
                .roomSize(1)
                .build();
        roomRepository.save(room);

        Membership membership = Membership.builder()
                .membershipUser(user)
                .membershipRoom(room)
                .membershipRoleInRoom(RoleInRoom.OWNER)
                .build();
        membershipRepository.save(membership);

        return new BaseResponse("00", "Room created successfully", null);
    }

    public BaseResponse updateRoom(RoomUpdateRequest request, UUID userId) {
        if (request == null) throw new IllegalArgumentException("request cannot be null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("room not found"));

        Optional.ofNullable(request.getRoomName()).ifPresent(room::setRoomName);
        Optional.ofNullable(request.getRoomDescription()).ifPresent(room::setRoomDescription);
        Optional.ofNullable(request.getRoomPrivacy()).ifPresent(room::setRoomPrivacy);
        roomRepository.save(room);

        return new BaseResponse("00", "Room updated successfully", null);
    }

    public BaseResponse deleteRoom(UUID userId, UUID roomId) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));

        roomRepository.delete(room);

        List<Membership> membershipList = membershipRepository.getByMembershipRoom(room);
        membershipRepository.deleteAll(membershipList);

        return new BaseResponse("00", "Room deleted successfully", null);
    }

    @Transactional
    public BaseResponse addUserToRoom(UUID addedUserId, UUID roomId, UUID roomOwnerId) {
        if (addedUserId == null) throw new IllegalArgumentException("addedUserId cannot be null");
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        User addedUser = userRepository.findById(addedUserId)
                .orElseThrow(() -> new IllegalArgumentException("user to be added not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
        User roomOwner = userRepository.findById(roomOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("room owner not found"));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, roomOwner)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this room"));
        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new IllegalArgumentException("You do not have permission to add users from this room");
        }
        Optional<Membership> existingMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, addedUser);
        if (existingMembership.isPresent()) {
            if (existingMembership.get().getMembershipStatus().equalsIgnoreCase("BANNED")) {
                throw new IllegalArgumentException("This user has been previously banned. Unban this user before readding...");
            }
            throw new IllegalArgumentException("Added user is already in room");
        }

        Membership membership = Membership.builder()
                .membershipUser(addedUser)
                .membershipRoom(room)
                .membershipRoleInRoom(RoleInRoom.MEMBER)
                .build();
        membershipRepository.save(membership);

        room.setRoomSize(room.getRoomSize() + 1);
        roomRepository.save(room);

        return new BaseResponse("00", "User added to room successfully", null);
    }

    @Transactional
    public BaseResponse joinRoom(UUID userId, UUID roomId) {
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));

        if (room.getRoomPrivacy() != RoomPrivacy.OPEN) {
            throw new IllegalArgumentException("You shouldn't have been able to see this 🙂... You sneaky little...");
        }
        Optional<Membership> existingMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, user);
        if (existingMembership.isPresent()) {
            if (existingMembership.get().getMembershipStatus().equalsIgnoreCase("BANNED")) {
                throw new IllegalArgumentException("You have been banned and can not rejoin this room...");
            }
            throw new IllegalArgumentException("User is already in room");
        }

        Membership membership = Membership.builder()
                .membershipUser(user)
                .membershipRoom(room)
                .membershipRoleInRoom(RoleInRoom.MEMBER)
                .build();
        membershipRepository.save(membership);

        room.setRoomSize(room.getRoomSize() + 1);
        roomRepository.save(room);

        return new BaseResponse("00", "User joined room successfully", null);
    }

    @Transactional
    public BaseResponse removeUserFromRoom(UUID removedUserId, UUID roomId, UUID roomOwnerId) {
        if (removedUserId == null) throw new IllegalArgumentException("addedUserId cannot be null");
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        User removedUser = userRepository.findById(removedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User to be removed not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
        User roomOwner = userRepository.findById(roomOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("Room owner not found"));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, roomOwner)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this room"));
        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new IllegalArgumentException("You do not have permission to remove users from this room");
        }

        Membership removedUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, removedUser)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this room"));
        if (removedUserMembership.getMembershipRoleInRoom() == RoleInRoom.OWNER) {
            throw new IllegalArgumentException("Cannot remove the room owner");
        }
        membershipRepository.delete(removedUserMembership);

        room.setRoomSize(room.getRoomSize() - 1);
        roomRepository.save(room);

        return new BaseResponse("00", "User removed from room successfully", null);
    }

    @Transactional
    public BaseResponse kickUserFromRoom(UUID kickedUserId, UUID roomId, UUID roomOwnerId) {
        if (kickedUserId == null) throw new IllegalArgumentException("addedUserId cannot be null");
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        User kickedUser = userRepository.findById(kickedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User to be kicked not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
        User roomOwner = userRepository.findById(roomOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("Room owner not found"));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, roomOwner)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this room"));
        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new IllegalArgumentException("You do not have permission to kick users from this room");
        }

        Membership kickedUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, kickedUser)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this room"));
        if (kickedUserMembership.getMembershipRoleInRoom() == RoleInRoom.OWNER) {
            throw new IllegalArgumentException("Cannot kick the room owner");
        }
        membershipRepository.delete(kickedUserMembership);

        room.setRoomSize(room.getRoomSize() - 1);
        roomRepository.save(room);

        kickedUser.setUserCreditScore(Math.max(kickedUser.getUserCreditScore() - 10, 0));
        userRepository.save(kickedUser);

        return new BaseResponse("00", "User kicked from room successfully", null);
    }

    @Transactional
    public BaseResponse banUserFromRoom(UUID bannedUserId, UUID roomId, UUID roomOwnerId) {
        if (bannedUserId == null) throw new IllegalArgumentException("addedUserId cannot be null");
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        User bannedUser = userRepository.findById(bannedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User to be banned not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
        User roomOwner = userRepository.findById(roomOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("Room owner not found"));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, roomOwner)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this room"));
        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new IllegalArgumentException("You do not have permission to ban users from this room");
        }

        Membership bannedUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, bannedUser)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this room"));
        if (bannedUserMembership.getMembershipRoleInRoom() == RoleInRoom.OWNER) {
            throw new IllegalArgumentException("Cannot ban the room owner");
        }
        bannedUserMembership.setMembershipStatus("BANNED");
        membershipRepository.save(bannedUserMembership);

        room.setRoomSize(room.getRoomSize() - 1);
        roomRepository.save(room);

        bannedUser.setUserCreditScore(Math.max(bannedUser.getUserCreditScore() - 20, 0));
        userRepository.save(bannedUser);

        return new BaseResponse("00", "User banned from room successfully", null);
    }

    public BaseResponse unbanUsersInRoom(UUID roomId, UUID userId, UUID bannedUserId) {
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");
        if (bannedUserId == null) throw new IllegalArgumentException("Banned userId cannot be null");

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
        User bannedUser = userRepository.getByUserId(bannedUserId)
                .orElseThrow(() -> new IllegalArgumentException("user to be unbanned not found"));
        User roomOwner = userRepository.getByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Room owner not found"));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, roomOwner)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this room"));
        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new IllegalArgumentException("You do not have permission to unban users from this room");
        }

        Membership bannedUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, bannedUser)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this room"));
        if (!bannedUserMembership.getMembershipStatus().equalsIgnoreCase("BANNED")) {
            throw new IllegalArgumentException("This user is not banned");
        }

        bannedUserMembership.setMembershipStatus("ACTIVE");
        membershipRepository.save(bannedUserMembership);

        room.setRoomSize(room.getRoomSize() + 1);
        roomRepository.save(room);

        return new BaseResponse("00", "User unbanned successfully", null);
    }

    public BaseResponse leaveRoom(UUID userId, UUID roomId) {
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));

        Membership userMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, user)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this room"));
        if (userMembership.getMembershipRoleInRoom() == RoleInRoom.OWNER) {
            throw new IllegalArgumentException("Cannot remove the room owner");
        }
        membershipRepository.delete(userMembership);

        room.setRoomSize(room.getRoomSize() - 1);
        roomRepository.save(room);

        return new BaseResponse("00", "User left room successfully", null);
    }

    @Transactional
    public BaseResponse assignUserRoleInRoom(UUID userId, UUID roomId, UUID roomOwnerId, RoleInRoom newRole) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        User actingUser = userRepository.findById(roomOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("Acting user not found"));

        // Validate acting user is OWNER of the room
        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, actingUser)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this room"));
        if (!ownerMembership.getMembershipRoleInRoom().equals(RoleInRoom.OWNER)) {
            throw new IllegalArgumentException("Only the room owner may assign/change roles");
        }

        // Owner cannot change their own role (prevents resigning without assigning)
        if (actingUser.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Owner cannot change their own role directly. Assign ownership to someone else to retire.");
        }

        // Get membership of the target user
        Membership targetMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, targetUser)
                .orElseThrow(() -> new IllegalArgumentException("Target user is not a member of this room"));

        // If assigning OWNER role, demote current owner to MEMBER
        if (newRole == RoleInRoom.OWNER) {
            // Demote the current owner
            ownerMembership.setMembershipRoleInRoom(RoleInRoom.MEMBER);
            membershipRepository.save(ownerMembership);

            // Promote target user
            targetMembership.setMembershipRoleInRoom(RoleInRoom.OWNER);
            membershipRepository.save(targetMembership);

            return new BaseResponse("00", "Ownership transferred successfully", null);
        }

        // Otherwise, just update the target user's role
        targetMembership.setMembershipRoleInRoom(newRole);
        membershipRepository.save(targetMembership);

        return new BaseResponse("00", "User role updated successfully", null);
    }

    public BaseResponse getAllUsersInRoom(UUID roomId) {
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));

        List<Membership> membershipList = membershipRepository.getByMembershipRoom(room);

        List<RoomMembershipListResponse> roomMembershipListResponseList = new ArrayList<>();
        for (Membership membership : membershipList) {
            RoomMembershipListResponse roomMembershipListResponse = RoomMembershipListResponse.builder()
                    .userId(membership.getMembershipUser().getUserId())
                    .userName(membership.getMembershipUser().getUserName())
                    .userRoleInRoom(membership.getMembershipRoleInRoom())
                    .build();
            roomMembershipListResponseList.add(roomMembershipListResponse);
        }

        return new BaseResponse("00", "Room Membership List", roomMembershipListResponseList);
    }

    public BaseResponse getBannedUsersInRoom(UUID roomId, UUID userId) {
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Membership> bannedMembersList = membershipRepository.getByMembershipRoomAndMembershipStatus(room, "BANNED");
        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, user)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this room"));
        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new IllegalArgumentException("You do not have permission to view banned users");
        }

        List<RoomMembershipListResponse> roomMembershipListResponseList = new ArrayList<>();
        for (Membership membership : bannedMembersList) {
            RoomMembershipListResponse roomMembershipListResponse = RoomMembershipListResponse.builder()
                    .userId(membership.getMembershipUser().getUserId())
                    .userName(membership.getMembershipUser().getUserName())
                    .userRoleInRoom(membership.getMembershipRoleInRoom())
                    .userStatusInRoom(membership.getMembershipStatus())
                    .build();
            roomMembershipListResponseList.add(roomMembershipListResponse);
        }

        return new BaseResponse("00", "Banned Members List", roomMembershipListResponseList);
    }

    public BaseResponse viewUserRoomProfile(UUID roomId, UUID userId) {
        if (roomId == null) throw new IllegalArgumentException("roomId cannot be null");
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Membership userMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, user)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this room"));

        UserRoomProfileResponse  userRoomProfileResponse = UserRoomProfileResponse.builder()
                .userId(userId)
                .userName(user.getUserName())
                .userRoleInRoom(userMembership.getMembershipRoleInRoom())
                .build();

        return new BaseResponse("00", "User room profile", userRoomProfileResponse);
    }

    public BaseResponse getAllRooms(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        List<RoomPrivacy> nonOpenPrivacies = Arrays.asList(RoomPrivacy.CLOSED, RoomPrivacy.INVITE_ONLY);

        List<Room> openRooms = roomRepository.getByRoomPrivacy(RoomPrivacy.OPEN);
        List<Room> nonOpenRooms = roomRepository.findByRoomPrivacyIn(nonOpenPrivacies);

        List<Room> roomUserAllowedToSee = new ArrayList<>(openRooms);

        for (Room room : nonOpenRooms) {
            Optional<Membership> membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, user);
            if (membership.isPresent()) {
                roomUserAllowedToSee.add(room);
            }
        }

        return new BaseResponse("00", "All Rooms", roomUserAllowedToSee);
    }

    public BaseResponse viewRoom(UUID userId, UUID roomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Room room = roomRepository.findRoomByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));

//        List<RoomPrivacy> nonOpenPrivacies = Arrays.asList(RoomPrivacy.CLOSED, RoomPrivacy.INVITE_ONLY);

        if (room.getRoomPrivacy() != RoomPrivacy.OPEN) {
            Membership membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, user)
                    .orElseThrow(() -> new IllegalArgumentException("User is not allowed to see the room"));
        }

        return new BaseResponse("00", "Room", room);
    }

    private boolean isNotOwnerOrAdmin(Membership membership) {
        RoleInRoom role = membership.getMembershipRoleInRoom();
        return role != RoleInRoom.OWNER && role != RoleInRoom.ADMIN;
    }
}

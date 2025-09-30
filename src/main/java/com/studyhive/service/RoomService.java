package com.studyhive.service;

import com.studyhive.model.entity.Membership;
import com.studyhive.model.entity.Room;
import com.studyhive.model.entity.User;
import com.studyhive.model.enums.RoleInRoom;
import com.studyhive.model.enums.RoomPrivacy;
import com.studyhive.model.interfaces.SearchRoom;
import com.studyhive.model.request.RoomCreateRequest;
import com.studyhive.model.request.RoomUpdateRequest;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.model.response.RoomMembershipListResponse;
import com.studyhive.model.response.UserRoomProfileResponse;
import com.studyhive.repository.interfaces.MembershipRepository;
import com.studyhive.repository.interfaces.RoomRepository;
import com.studyhive.repository.interfaces.UserRepository;
import com.studyhive.util.exception.ApiException;
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
    public BaseResponse<?> createRoom(RoomCreateRequest request, UUID userId) {
        if (request == null) {
            throw new ApiException("11", "Request cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        List<Room> userRooms = roomRepository.getByRoomCreatedByAndRoomStatus(user, "ACTIVE");
        if (userRooms.size() >= user.getUserMaxRooms()) {
            throw new ApiException("55", "User rooms exceed maximum allowed", null);
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

        return new BaseResponse<>("00", "Room created successfully", null);
    }

    public BaseResponse<?> updateRoom(RoomUpdateRequest request, UUID userId) {
        if (request == null) {
            throw new ApiException("11", "Request cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(request.getRoomId(), "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        Optional.ofNullable(request.getRoomName()).ifPresent(room::setRoomName);
        Optional.ofNullable(request.getRoomDescription()).ifPresent(room::setRoomDescription);
        Optional.ofNullable(request.getRoomPrivacy()).ifPresent(room::setRoomPrivacy);

        roomRepository.save(room);

        return new BaseResponse<>("00", "Room updated successfully", null);
    }

    public BaseResponse<?> deleteRoom(UUID userId, UUID roomId) {
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        roomRepository.deleteByRoomId(room.getRoomId());

        List<UUID> membershipListIds = membershipRepository.getByMembershipRoomAndMembershipStatus(
                        room, "ACTIVE")
                .stream()
                .map(Membership::getMembershipId)
                .toList();
        membershipRepository.deleteAllByMembershipId(membershipListIds);

        return new BaseResponse<>("00", "Room deleted successfully", null);
    }

    @Transactional
    public BaseResponse<?> addUserToRoom(UUID addedUserId, UUID roomId, UUID roomOwnerId) {
        if (addedUserId == null) {
            throw new ApiException("11", "Added user ID cannot be null", null);
        }
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }

        User addedUser = userRepository.getByUserIdAndUserStatus(addedUserId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User to be added not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        User roomOwner = userRepository.getByUserIdAndUserStatus(roomOwnerId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room owner not found", null));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        roomOwner,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("33", "You are not a member of this room", null));

        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new ApiException("33", "You do not have permission to add users to this room", null);
        }

        Optional<Membership> existingMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                room,
                addedUser,
                "ACTIVE");

        if (existingMembership.isPresent()) {
            if ("BANNED".equalsIgnoreCase(existingMembership.get().getMembershipStatus())) {
                throw new ApiException("55", "This user has been previously banned. Unban before re-adding", null);
            }
            throw new ApiException("55", "Added user is already in the room", null);
        }

        Membership membership = Membership.builder()
                .membershipUser(addedUser)
                .membershipRoom(room)
                .membershipRoleInRoom(RoleInRoom.MEMBER)
                .build();
        membershipRepository.save(membership);

        room.setRoomSize(room.getRoomSize() + 1);
        roomRepository.save(room);

        return new BaseResponse<>("00", "User added to room successfully", null);
    }

    @Transactional
    public BaseResponse<?> addMultipleUsersToRoom(List<UUID> addedUserIds, UUID roomId, UUID roomOwnerId) {
        if (addedUserIds == null || addedUserIds.isEmpty()) {
            throw new ApiException("11", "List of users to add cannot be null or empty", null);
        }
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (roomOwnerId == null) {
            throw new ApiException("11", "Room owner ID cannot be null", null);
        }

        for (UUID addedUserId : addedUserIds) {
            addUserToRoom(addedUserId, roomId, roomOwnerId);
        }

        return new BaseResponse<>("00", "All users added to room successfully", null);
    }

    @Transactional
    public BaseResponse<?> joinRoom(UUID userId, UUID roomId) {
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        if (room.getRoomPrivacy() != RoomPrivacy.OPEN) {
            throw new ApiException("33", "You do not have permission to join this room", null);
        }

        Optional<Membership> existingMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                room,
                user,
                "ACTIVE");

        if (existingMembership.isPresent()) {
            if ("BANNED".equalsIgnoreCase(existingMembership.get().getMembershipStatus())) {
                throw new ApiException("55", "You have been banned and cannot rejoin this room", null);
            }
            throw new ApiException("55", "User is already in the room", null);
        }

        Membership membership = Membership.builder()
                .membershipUser(user)
                .membershipRoom(room)
                .membershipRoleInRoom(RoleInRoom.MEMBER)
                .build();
        membershipRepository.save(membership);

        room.setRoomSize(room.getRoomSize() + 1);
        roomRepository.save(room);

        return new BaseResponse<>("00", "User joined room successfully", null);
    }

    @Transactional
    public BaseResponse<?> removeUserFromRoom(UUID removedUserId, UUID roomId, UUID roomOwnerId) {
        if (removedUserId == null) {
            throw new ApiException("11", "User ID to be removed cannot be null", null);
        }
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (roomOwnerId == null) {
            throw new ApiException("11", "Room owner ID cannot be null", null);
        }

        User removedUser = userRepository.getByUserIdAndUserStatus(removedUserId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User to be removed not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        User roomOwner = userRepository.getByUserIdAndUserStatus(roomOwnerId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room owner not found", null));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        roomOwner,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("33", "You are not a member of this room", null));

        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new ApiException("33", "You do not have permission to remove users from this room", null);
        }

        Membership removedUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        removedUser,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User is not a member of this room", null));

        if (removedUserMembership.getMembershipRoleInRoom() == RoleInRoom.OWNER) {
            throw new ApiException("55", "Cannot remove the room owner", null);
        }

        membershipRepository.delete(removedUserMembership);

        room.setRoomSize(room.getRoomSize() - 1);
        roomRepository.save(room);

        return new BaseResponse<>("00", "User removed from room successfully", null);
    }

    @Transactional
    public BaseResponse<?> kickUserFromRoom(UUID kickedUserId, UUID roomId, UUID roomOwnerId) {
        if (kickedUserId == null) {
            throw new ApiException("11", "User ID to be kicked cannot be null", null);
        }
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (roomOwnerId == null) {
            throw new ApiException("11", "Room owner ID cannot be null", null);
        }

        User kickedUser = userRepository.getByUserIdAndUserStatus(kickedUserId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User to be kicked not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        User roomOwner = userRepository.getByUserIdAndUserStatus(roomOwnerId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room owner not found", null));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        roomOwner,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("33", "You are not a member of this room", null));

        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new ApiException("33", "You do not have permission to kick users from this room", null);
        }

        Membership kickedUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        kickedUser,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User is not a member of this room", null));

        if (kickedUserMembership.getMembershipRoleInRoom() == RoleInRoom.OWNER) {
            throw new ApiException("55", "Cannot kick the room owner", null);
        }

        membershipRepository.delete(kickedUserMembership);

        room.setRoomSize(room.getRoomSize() - 1);
        roomRepository.save(room);

        kickedUser.setUserCreditScore(Math.max(kickedUser.getUserCreditScore() - 10, 0));
        userRepository.save(kickedUser);

        return new BaseResponse<>("00", "User kicked from room successfully", null);
    }

    @Transactional
    public BaseResponse<?> banUserFromRoom(UUID bannedUserId, UUID roomId, UUID roomOwnerId) {
        if (bannedUserId == null) {
            throw new ApiException("11", "User ID to be banned cannot be null", null);
        }
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (roomOwnerId == null) {
            throw new ApiException("11", "Room owner ID cannot be null", null);
        }

        User bannedUser = userRepository.getByUserIdAndUserStatus(bannedUserId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User to be banned not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        User roomOwner = userRepository.getByUserIdAndUserStatus(roomOwnerId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room owner not found", null));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        roomOwner,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("33", "You are not a member of this room", null));

        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new ApiException("33", "You do not have permission to ban users from this room", null);
        }

        Membership bannedUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        bannedUser,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User is not a member of this room", null));

        if (bannedUserMembership.getMembershipRoleInRoom() == RoleInRoom.OWNER) {
            throw new ApiException("55", "Cannot ban the room owner", null);
        }

        bannedUserMembership.setMembershipStatus("BANNED");
        membershipRepository.save(bannedUserMembership);

        room.setRoomSize(room.getRoomSize() - 1);
        roomRepository.save(room);

        bannedUser.setUserCreditScore(Math.max(bannedUser.getUserCreditScore() - 20, 0));
        userRepository.save(bannedUser);

        return new BaseResponse<>("00", "User banned from room successfully", null);
    }

    public BaseResponse<?> unbanUsersInRoom(UUID roomId, UUID userId, UUID bannedUserId) {
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (bannedUserId == null) {
            throw new ApiException("11", "Banned user ID cannot be null", null);
        }
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        User bannedUser = userRepository.getByUserIdAndUserStatus(bannedUserId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User to be unbanned not found", null));

        User roomOwner = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room owner not found", null));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        roomOwner,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("33", "You are not a member of this room", null));

        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new ApiException("33", "You do not have permission to unban users from this room", null);
        }

        Membership bannedUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        bannedUser,
                        "BANNED")
                .orElseThrow(() -> new ApiException("44", "User is not a member of this room", null));

        if (!"BANNED".equalsIgnoreCase(bannedUserMembership.getMembershipStatus())) {
            throw new ApiException("55", "This user is not banned", null);
        }

        bannedUserMembership.setMembershipStatus("ACTIVE");
        membershipRepository.save(bannedUserMembership);

        room.setRoomSize(room.getRoomSize() + 1);
        roomRepository.save(room);

        return new BaseResponse<>("00", "User unbanned successfully", null);
    }

    public BaseResponse<?> leaveRoom(UUID userId, UUID roomId) {
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        Membership userMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        user,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User is not a member of this room", null));

        if (userMembership.getMembershipRoleInRoom() == RoleInRoom.OWNER) {
            throw new ApiException("55", "Cannot remove the room owner", null);
        }

        membershipRepository.delete(userMembership);

        room.setRoomSize(room.getRoomSize() - 1);
        roomRepository.save(room);

        return new BaseResponse<>("00", "User left room successfully", null);
    }

    @Transactional
    public BaseResponse<?> assignUserRoleInRoom(UUID userId, UUID roomId, UUID roomOwnerId, RoleInRoom newRole) {
        if (userId == null) {
            throw new ApiException("11", "Target user ID cannot be null", null);
        }
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (roomOwnerId == null) {
            throw new ApiException("11", "Acting user ID cannot be null", null);
        }

        User targetUser = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Target user not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        User actingUser = userRepository.getByUserIdAndUserStatus(roomOwnerId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Acting user not found", null));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        actingUser,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("33", "You are not a member of this room", null));

        if (!ownerMembership.getMembershipRoleInRoom().equals(RoleInRoom.OWNER)) {
            throw new ApiException("33", "Only the room owner may assign/change roles", null);
        }

        if (actingUser.getUserId().equals(userId)) {
            throw new ApiException("55", "Owner cannot change their own role directly. Assign ownership to someone else to retire.", null);
        }

        Membership targetMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room,
                        targetUser,
                        "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Target user is not a member of this room", null));

        if (newRole == RoleInRoom.OWNER) {
            // Demote current owner
            ownerMembership.setMembershipRoleInRoom(RoleInRoom.MEMBER);
            membershipRepository.save(ownerMembership);

            // Promote target user
            targetMembership.setMembershipRoleInRoom(RoleInRoom.OWNER);
            membershipRepository.save(targetMembership);

            return new BaseResponse<>("00", "Ownership transferred successfully", null);
        }

        // Update target user's role
        targetMembership.setMembershipRoleInRoom(newRole);
        membershipRepository.save(targetMembership);

        return new BaseResponse<>("00", "User role updated successfully", null);
    }

    public BaseResponse<?> getAllUsersInRoom(UUID roomId, UUID userId) {
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        Membership userMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room, user, "ACTIVE")
                .orElseThrow(() -> new ApiException("33", "You are not a member of this room", null));

        List<Membership> membershipList = membershipRepository.getByMembershipRoomAndMembershipStatus(
                room, "ACTIVE");

        List<RoomMembershipListResponse> roomMembershipListResponseList = new ArrayList<>();
        for (Membership membership : membershipList) {
            RoomMembershipListResponse roomMembershipListResponse = RoomMembershipListResponse.builder()
                    .userId(membership.getMembershipUser().getUserId())
                    .userName(membership.getMembershipUser().getUserName())
                    .userRoleInRoom(membership.getMembershipRoleInRoom())
                    .build();
            roomMembershipListResponseList.add(roomMembershipListResponse);
        }

        return new BaseResponse<>("00", "Room Membership List", roomMembershipListResponseList);
    }

    public BaseResponse<?> getBannedUsersInRoom(UUID roomId, UUID userId) {
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        Membership ownerMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room, user, "ACTIVE")
                .orElseThrow(() -> new ApiException("33", "You are not a member of this room", null));

        if (isNotOwnerOrAdmin(ownerMembership)) {
            throw new ApiException("33", "You do not have permission to view banned users", null);
        }

        List<Membership> bannedMembersList = membershipRepository.getByMembershipRoomAndMembershipStatus(room, "BANNED");

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

        return new BaseResponse<>("00", "Banned Members List", roomMembershipListResponseList);
    }

    public BaseResponse<?> viewUserRoomProfile(UUID roomId, UUID viewedUserId, UUID viewerUserId) {
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }
        if (viewedUserId == null) {
            throw new ApiException("11", "Viewed user ID cannot be null", null);
        }
        if (viewerUserId == null) {
            throw new ApiException("11", "Viewer user ID cannot be null", null);
        }

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        User viewedUser = userRepository.getByUserIdAndUserStatus(viewedUserId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Viewed user not found", null));

        User viewerUser = userRepository.getByUserIdAndUserStatus(viewerUserId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Viewer user not found", null));

        Membership viewedUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room, viewedUser, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Viewed user is not a member of this room", null));

        Membership viewerUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                        room, viewerUser, "ACTIVE")
                .orElseThrow(() -> new ApiException("33", "You are not a member of this room", null));

        UserRoomProfileResponse userRoomProfileResponse = UserRoomProfileResponse.builder()
                .userId(viewedUserId)
                .userName(viewedUser.getUserName())
                .userRoleInRoom(viewedUserMembership.getMembershipRoleInRoom())
                .build();

        return new BaseResponse<>("00", "User room profile", userRoomProfileResponse);
    }

    public BaseResponse<?> getAllRooms(UUID userId) {
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        List<RoomPrivacy> nonOpenPrivacies = Arrays.asList(RoomPrivacy.CLOSED, RoomPrivacy.INVITE_ONLY);

        List<Room> openRooms = roomRepository.getByRoomPrivacyAndRoomStatus(RoomPrivacy.OPEN, "ACTIVE");
        List<Room> nonOpenRooms = roomRepository.findByRoomPrivacyInAndRoomStatus(nonOpenPrivacies, "ACTIVE");

        List<Room> roomUserAllowedToSee = new ArrayList<>(openRooms);

        System.out.println(openRooms);
        System.out.println(nonOpenRooms);
        for (Room room : nonOpenRooms) {
            Optional<Membership> membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                    room,
                    user,
                    "ACTIVE");
            membership.ifPresent(m -> roomUserAllowedToSee.add(room));
        }

        return new BaseResponse<>("00", "All Rooms", roomUserAllowedToSee);
    }

    public BaseResponse<?> viewRoom(UUID userId, UUID roomId) {
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }
        if (roomId == null) {
            throw new ApiException("11", "Room ID cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        Room room = roomRepository.findRoomByRoomIdAndRoomStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Room not found", null));

        if (room.getRoomPrivacy() != RoomPrivacy.OPEN) {
            membershipRepository.findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
                            room,
                            user,
                            "ACTIVE")
                    .orElseThrow(() -> new ApiException("33", "User is not allowed to see the room", null));
        }

        return new BaseResponse<>("00", "Room", room);
    }

    public BaseResponse<?> searchRoomByRoomName(String searchQuery, UUID userId) {
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            throw new ApiException("11", "Search Query cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        List<SearchRoom> rooms = roomRepository.searchRoomByRoomName(searchQuery, userId);

        return new BaseResponse<>("00", "Search Room", rooms);
    }

    private boolean isNotOwnerOrAdmin(Membership membership) {
        RoleInRoom role = membership.getMembershipRoleInRoom();
        return role != RoleInRoom.OWNER && role != RoleInRoom.ADMIN;
    }

    public record RoomResponse(UUID roomId, String roomName, String privacy) {}
}

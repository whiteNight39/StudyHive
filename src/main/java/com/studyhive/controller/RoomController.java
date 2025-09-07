package com.studyhive.controller;

import com.studyhive.model.enums.RoleInRoom;
import com.studyhive.model.request.RoomCreateRequest;
import com.studyhive.model.request.RoomUpdateRequest;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.service.RoomService;
import com.studyhive.util.jwt.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create-room")
    public BaseResponse<?> createRoom(
            @Valid @RequestBody RoomCreateRequest request) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.createRoom(request, principal.getUserId());
    }

    @PatchMapping("/update-room")
    public BaseResponse<?> updateRoom(
            @Valid @RequestBody RoomUpdateRequest request) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.updateRoom(request, principal.getUserId());
    }

    @DeleteMapping("/delete-room")
    public BaseResponse<?> deleteRoom(
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.deleteRoom(roomId, principal.getUserId());
    }

    @PatchMapping("/add-user-to-room")
    public BaseResponse<?> addUserToRoom(
            @Valid @RequestParam UUID addedUserId,
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.addUserToRoom(addedUserId, roomId, principal.getUserId());
    }

    @PatchMapping("/add-multiple-users-to-room")
    public BaseResponse<?> addMultipleUsersToRoom(
            @Valid @RequestParam List<UUID> addedUserIds,
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.addMultipleUsersToRoom(addedUserIds, roomId, principal.getUserId());
    }

    @PatchMapping("/join-room")
    public BaseResponse<?> joinRoom(
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.joinRoom(principal.getUserId(), roomId);
    }

    @PatchMapping("/remove-user-from-room")
    public BaseResponse<?> removeUserFromRoom(
            @Valid @RequestParam UUID removedUserId,
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.removeUserFromRoom(removedUserId, roomId, principal.getUserId());
    }

    @PatchMapping("/kick-user-from-room")
    public BaseResponse<?> kickUserFromRoom(
            @Valid @RequestParam UUID kickedUserId,
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.kickUserFromRoom(kickedUserId, roomId, principal.getUserId());
    }

    @PatchMapping("/ban-user-from-room")
    public BaseResponse<?> banUserFromRoom(
            @Valid @RequestParam UUID bannedUserId,
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.banUserFromRoom(bannedUserId, roomId, principal.getUserId());
    }

    @PatchMapping("/unban-users-in-room")
    public BaseResponse<?> unbanUsersInRoom(
            @Valid @RequestParam UUID bannedUserId,
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.unbanUsersInRoom(roomId, principal.getUserId(), bannedUserId);
    }

    @PatchMapping("/leave-room")
    public BaseResponse<?> leaveRoom(
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.leaveRoom(principal.getUserId(), roomId);
    }

    @PatchMapping("/assign-user-role-in-room")
    public BaseResponse<?> assignUserRoleInRoom(
            @Valid @RequestParam UUID userId,
            @Valid @RequestParam UUID roomId,
            @Valid @RequestParam RoleInRoom newRole) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.assignUserRoleInRoom(userId, roomId, principal.getUserId(), newRole);
    }

    @GetMapping("/get-all-uers-in-room")
    public BaseResponse<?> getAllUsersInRoom(
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.getAllUsersInRoom(roomId, principal.getUserId());
    }

    @GetMapping("/get-banned-users-in-room")
    public BaseResponse<?> getBannedUsersInRoom(
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.getBannedUsersInRoom(roomId, principal.getUserId());
    }

    @GetMapping("/view-user-room-profile")
    public BaseResponse<?> viewUserRoomProfile(
            @Valid @RequestParam UUID roomId,
            @Valid @RequestParam UUID viewedUserId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.viewUserRoomProfile(roomId, viewedUserId, principal.getUserId());
    }

    @GetMapping("/get-all-rooms")
    public BaseResponse<?> getAllRooms() {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.getAllRooms(principal.getUserId());
    }

    @GetMapping("/view-room")
    public BaseResponse<?> viewRoom(
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return roomService.viewRoom(principal.getUserId(), roomId);
    }
}

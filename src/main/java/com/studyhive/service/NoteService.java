package com.studyhive.service;

import com.studyhive.model.entity.*;
import com.studyhive.model.enums.RoleInRoom;
import com.studyhive.model.request.NoteCreateRequest;
import com.studyhive.model.request.NoteUpdateRequest;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.model.response.NoteResponse;
import com.studyhive.repository.interfaces.*;
import com.studyhive.util.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final MembershipRepository membershipRepository;
    private final NoteUpvoteRepository noteUpvoteRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository, RoomRepository roomRepository, MembershipRepository membershipRepository, NoteUpvoteRepository noteUpvoteRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.membershipRepository = membershipRepository;
        this.noteUpvoteRepository = noteUpvoteRepository;
    }

    @Transactional
    public BaseResponse<?> createNoteInRoom(NoteCreateRequest request, UUID userId) {
        if (request == null) throw new ApiException("11", "Note create request is null", null);

        Room noteRoom = roomRepository.findRoomByRoomId(request.getNoteRoomId())
                .orElseThrow(() -> new ApiException("44", "Note room not found", null));
        User noteUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("44", "User not found", null));
        Membership noteUserMembership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(noteRoom, noteUser)
                .orElseThrow(() -> new ApiException("44", "User is not a member of the room", null));

        Note note = Note.builder()
                .noteContent(request.getNoteContent())
                .noteRoom(noteRoom)
                .noteCreatedBy(noteUser)
                .noteUpvotes(1)
                .build();
        noteRepository.save(note);

        noteUser.setUserCreditScore(noteUser.getUserCreditScore() + 2);
        userRepository.save(noteUser);

        return new BaseResponse<>("00", "Note created successfully", null);
    }

    @Transactional
    public BaseResponse<?> updateNoteInRoom(NoteUpdateRequest request, UUID userId) {
        if (request == null) throw new ApiException("11", "Note update request is null", null);

        Note note = noteRepository.findById(request.getNoteId())
                .orElseThrow(() -> new ApiException("44", "Note not found", null));

        if (!note.getNoteCreatedBy().getUserId().equals(userId)) {
            throw new ApiException("33", "This is not your note", null);
        }
        Optional.ofNullable(request.getNoteContent()).ifPresent(note::setNoteContent);
        noteRepository.save(note);

        return new BaseResponse<>("00", "Note updated successfully", null);
    }

    @Transactional
    public BaseResponse<?> deleteNoteInRoom(UUID userId, UUID noteId) {
        if (noteId == null) throw new ApiException("11", "Note id is null", null);

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ApiException("44", "Note not found", null));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("44", "User not found", null));
        User noteOwner = userRepository.findById(note.getNoteCreatedBy().getUserId())
                .orElseThrow(() -> new ApiException("44", "User not found", null));
        Membership membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(note.getNoteRoom(), user)
                .orElseThrow(() -> new ApiException("33", "User is not a member of the room", null));

        if (!note.getNoteCreatedBy().getUserId().equals(userId)) {
            // not the creator → must be owner or admin
            if (isNotOwnerOrAdmin(membership)) {
                throw new ApiException("33", "You do not have permission to delete this note", null);
            }
            // admin/owner deleting → reduce noteOwner’s credit
            noteOwner.setUserCreditScore(noteOwner.getUserCreditScore() - 3);
            userRepository.save(noteOwner);
        }

        noteRepository.delete(note);
        return new BaseResponse<>("00", "Note deleted successfully", null);
    }

    public BaseResponse<List<NoteResponse>> getAllNoteInRoom(UUID userId, UUID roomId) {
        if (roomId == null) throw new ApiException("11", "Room id is null", null);

        Room room = roomRepository.findRoomByRoomId(roomId)
                .orElseThrow(() -> new ApiException("44", "Room not found", null));
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new ApiException("44", "User not found", null));
        Membership membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, user)
                .orElseThrow(() -> new ApiException("33", "User is not a member of the room", null));

        List<NoteResponse> noteResponses = new ArrayList<>();
        List<Note> notes = noteRepository.findByNoteRoom(room);

        for (Note note : notes) {
            NoteResponse noteResponse = NoteResponse.builder()
                    .noteId(note.getNoteId())
                    .noteContent(note.getNoteContent())
                    .noteTimestamp(note.getNoteUpdatedAt())
                    .noteCreatedByUserName(note.getNoteCreatedBy().getUserName())
                    .build();
        }

        return new BaseResponse<>("00", "Notes found successfully", noteResponses);
    }

    public BaseResponse<NoteResponse> getNoteInRoom(UUID userId, UUID noteId, UUID roomId) {
        if (roomId == null) throw new ApiException("11", "Room id is null", null);
        if (noteId == null) throw new ApiException("11", "Note id is null", null);

        Room room = roomRepository.findRoomByRoomId(roomId)
                .orElseThrow(() -> new ApiException("44", "Room not found", null));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("44", "User not found", null));
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ApiException("44", "Note not found", null));
        if (!note.getNoteRoom().equals(room))
            throw new ApiException("11", "This note doesn't belong to this room", null);
        Membership membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, user)
                .orElseThrow(() -> new ApiException("33", "User is not a member of the room", null));

        NoteResponse noteResponse = NoteResponse.builder()
                .noteId(note.getNoteId())
                .noteContent(note.getNoteContent())
                .noteTimestamp(note.getNoteUpdatedAt())
                .noteCreatedByUserName(note.getNoteCreatedBy().getUserName())
                .build();

        return new BaseResponse<>("00", "Note found successfully", noteResponse);
    }

    @Transactional
    public BaseResponse<?> upvoteNoteInRoom(UUID userId, UUID noteId, UUID roomId) {
        if (roomId == null) throw new ApiException("11", "Room id is null", null);
        if (noteId == null) throw new ApiException("11", "Note id is null", null);

        Room room = roomRepository.findRoomByRoomId(roomId)
                .orElseThrow(() -> new ApiException("44", "Room not found", null));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("44", "User not found", null));
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ApiException("44", "Note not found", null));
        if (!note.getNoteRoom().equals(room))
            throw new ApiException("11", "This note doesn't belong to this room", null);
        Membership membership = membershipRepository.findMembershipByMembershipRoomAndMembershipUser(room, user)
                .orElseThrow(() -> new ApiException("33", "User is not a member of the room", null));

        NoteUpvote noteUpvotedByUser = noteUpvoteRepository.findByNoteAndNoteUser(note, user)
                .orElse(null);

        if (!(noteUpvotedByUser == null)) {
            note.setNoteUpvotes(note.getNoteUpvotes() - 1);
            noteRepository.save(note);

            user.setUserCreditScore(user.getUserCreditScore() - 2); // keep balance
            userRepository.save(user);

            noteUpvoteRepository.delete(noteUpvotedByUser);
            return new BaseResponse<>("00", "Upvote removed",
                    Map.of("upvotes", note.getNoteUpvotes(), "userHasUpvoted", false));
        }

        note.setNoteUpvotes(note.getNoteUpvotes() + 1);
        noteRepository.save(note);
        user.setUserCreditScore(user.getUserCreditScore() + 2);
        userRepository.save(user);
        NoteUpvote noteUpvote = NoteUpvote.builder()
                .note(note)
                .noteUser(user)
                .build();
        noteUpvoteRepository.save(noteUpvote);

        return new BaseResponse<>("00", "Note upvoted",
                Map.of("upvotes", note.getNoteUpvotes(), "userHasUpvoted", true));
    }

    private boolean isNotOwnerOrAdmin(Membership membership) {
        RoleInRoom role = membership.getMembershipRoleInRoom();
        return role != RoleInRoom.OWNER && role != RoleInRoom.ADMIN;
    }
}

package com.studyhive.controller;

import com.studyhive.model.request.NoteCreateRequest;
import com.studyhive.model.request.NoteUpdateRequest;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.service.NoteService;
import com.studyhive.util.jwt.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/note")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/create-note-in-room")
    public BaseResponse<?> createNoteInRoom(
            @Valid @RequestBody NoteCreateRequest request) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return noteService.createNoteInRoom(request, principal.getUserId());
    }

    @PatchMapping("/update-note-in-room")
    public BaseResponse<?> updateNoteInRoom(
            @Valid @RequestBody NoteUpdateRequest request) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return noteService.updateNoteInRoom(request, principal.getUserId());
    }

    @DeleteMapping("/delete-note-in-room")
    public BaseResponse<?> deleteNoteInRoom(
            @Valid @RequestParam UUID noteId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return noteService.deleteNoteInRoom(principal.getUserId(), noteId);
    }

    @GetMapping("/get-all-notes-in-room")
    public BaseResponse<?> getAllNoteInRoom(
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return noteService.getAllNoteInRoom(principal.getUserId(), roomId);
    }

    @GetMapping("/get-note-in-room")
    public BaseResponse<?> getNoteInRoom(
            @Valid @RequestParam UUID noteId,
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return noteService.getNoteInRoom(principal.getUserId(), noteId, roomId);
    }

    @PatchMapping("/upvote-note-in-room")
    public BaseResponse<?> upvoteNoteInRoom(
            @Valid @RequestParam UUID noteId,
            @Valid @RequestParam UUID roomId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return noteService.upvoteNoteInRoom(principal.getUserId(), noteId, roomId);
    }
}

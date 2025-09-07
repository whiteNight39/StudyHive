package com.studyhive.controller;

import com.studyhive.model.request.LoadMessagesRequest;
import com.studyhive.model.request.MessageCreateRequest;
import com.studyhive.model.request.MessageUpdateRequest;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.service.MessageService;
import com.studyhive.util.jwt.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/message")
@SecurityRequirement(name = "BearerAuth")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send-message")
    public BaseResponse<?> sendMessage(
            @Valid @RequestBody MessageCreateRequest request) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return messageService.sendMessage(request, principal.getUserId());
    }

    @PatchMapping("/update-message")
    public BaseResponse<?> updateMessage(
            @Valid @RequestBody MessageUpdateRequest request) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return messageService.updateMessage(request, principal.getUserId());
    }

    @DeleteMapping("/delete-message")
    public BaseResponse<?> deleteMessage(
            @Valid @RequestParam UUID messageId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return messageService.deleteMessage(messageId, principal.getUserId());
    }

    @GetMapping("/view-message")
    public BaseResponse<?> viewMessage(
            @Valid @RequestParam UUID messageId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return messageService.viewMessage(messageId, principal.getUserId());
    }

    @GetMapping("/load-messages")
    public BaseResponse<?> loadMessages(
            @Valid @RequestBody LoadMessagesRequest request) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return messageService.loadMessages(request, principal.getUserId());
    }
}

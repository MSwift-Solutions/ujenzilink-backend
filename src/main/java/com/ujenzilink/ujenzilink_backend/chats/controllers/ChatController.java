package com.ujenzilink.ujenzilink_backend.chats.controllers;

import com.ujenzilink.ujenzilink_backend.chats.dtos.*;
import com.ujenzilink.ujenzilink_backend.chats.services.ChatService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<ApiCustomResponse<List<ConversationSummaryDTO>>> getConversations() {
        ApiCustomResponse<List<ConversationSummaryDTO>> response = chatService.getConversationSummaries();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiCustomResponse<List<MessageDTO>>> getConversationMessages(
            @PathVariable UUID id) {

        ApiCustomResponse<List<MessageDTO>> response = chatService.getConversationMessages(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiCustomResponse<String>> sendMessage(
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request) {

        ApiCustomResponse<String> response = chatService.sendMessage(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/conversations/direct")
    public ResponseEntity<ApiCustomResponse<DirectConversationDTO>> createDirectConversation(
            @Valid @RequestBody CreateDirectConversationRequest request) {

        ApiCustomResponse<DirectConversationDTO> response = chatService.createDirectConversation(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/conversations/group")
    public ResponseEntity<ApiCustomResponse<GroupConversationDTO>> createGroupConversation(
            @Valid @RequestBody CreateGroupConversationRequest request) {

        ApiCustomResponse<GroupConversationDTO> response = chatService.createGroupConversation(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiCustomResponse<ConversationDTO>> getConversationDetails(
            @PathVariable UUID id) {

        ApiCustomResponse<ConversationDTO> response = chatService.getConversationDetails(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/messages/{id}/read")
    public ResponseEntity<ApiCustomResponse<String>> markMessageAsRead(
            @PathVariable UUID id) {

        ApiCustomResponse<String> response = chatService.markMessageAsRead(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/conversations/{id}/participants")
    public ResponseEntity<ApiCustomResponse<String>> addParticipants(
            @PathVariable UUID id,
            @Valid @RequestBody AddParticipantsRequest request) {

        ApiCustomResponse<String> response = chatService.addParticipants(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/conversations/{id}/participants/{userId}")
    public ResponseEntity<ApiCustomResponse<String>> removeParticipant(
            @PathVariable UUID id,
            @PathVariable UUID userId) {

        ApiCustomResponse<String> response = chatService.removeParticipant(id, userId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/conversations/{id}/leave")
    public ResponseEntity<ApiCustomResponse<String>> leaveConversation(
            @PathVariable UUID id) {

        ApiCustomResponse<String> response = chatService.leaveConversation(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PutMapping("/conversations/{id}")
    public ResponseEntity<ApiCustomResponse<String>> updateGroupName(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGroupNameRequest request) {

        ApiCustomResponse<String> response = chatService.updateGroupName(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<ApiCustomResponse<String>> deleteConversation(
            @PathVariable UUID id) {

        ApiCustomResponse<String> response = chatService.deleteConversation(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<ApiCustomResponse<MessageDTO>> editMessage(
            @PathVariable UUID id,
            @Valid @RequestBody EditMessageRequest request) {

        ApiCustomResponse<MessageDTO> response = chatService.editMessage(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<ApiCustomResponse<String>> deleteMessage(
            @PathVariable UUID id) {

        ApiCustomResponse<String> response = chatService.deleteMessage(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}

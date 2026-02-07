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
@RequestMapping("/api/v1")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // ========== CORE APIS ==========

    /**
     * 1. GET CONVERSATION SUMMARIES
     * Usage: Chat home screen showing all conversations
     * GET /api/v1/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiCustomResponse<List<ConversationSummaryDTO>>> getConversations() {
        ApiCustomResponse<List<ConversationSummaryDTO>> response = chatService.getConversationSummaries();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 2. GET MESSAGES FOR CONVERSATION
     * Usage: When user clicks on a conversation to view messages
     * GET /api/v1/conversations/{id}/messages?page=0&size=20
     */
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiCustomResponse<MessagePageDTO>> getConversationMessages(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        ApiCustomResponse<MessagePageDTO> response = chatService.getConversationMessages(id, page, size);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 3. SEND MESSAGE
     * Usage: Send a message to a conversation
     * POST /api/v1/conversations/{id}/messages
     */
    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiCustomResponse<MessageDTO>> sendMessage(
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request) {

        ApiCustomResponse<MessageDTO> response = chatService.sendMessage(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    // ========== ADDITIONAL APIS ==========

    /**
     * 4. CREATE CONVERSATION
     * Usage: Start a new direct or group chat
     * POST /api/v1/conversations
     */
    @PostMapping("/conversations")
    public ResponseEntity<ApiCustomResponse<ConversationDTO>> createConversation(
            @Valid @RequestBody CreateConversationRequest request) {

        ApiCustomResponse<ConversationDTO> response = chatService.createConversation(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 5. GET CONVERSATION DETAILS
     * Usage: View full conversation details with all participants
     * GET /api/v1/conversations/{id}
     */
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiCustomResponse<ConversationDTO>> getConversationDetails(
            @PathVariable UUID id) {

        ApiCustomResponse<ConversationDTO> response = chatService.getConversationDetails(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 6. MARK MESSAGE AS READ
     * Usage: Update read status when user views a message
     * POST /api/v1/messages/{id}/read
     */
    @PostMapping("/messages/{id}/read")
    public ResponseEntity<ApiCustomResponse<String>> markMessageAsRead(
            @PathVariable UUID id) {

        ApiCustomResponse<String> response = chatService.markMessageAsRead(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 7. ADD PARTICIPANTS TO GROUP
     * Usage: Add new members to a group conversation
     * POST /api/v1/conversations/{id}/participants
     */
    @PostMapping("/conversations/{id}/participants")
    public ResponseEntity<ApiCustomResponse<String>> addParticipants(
            @PathVariable UUID id,
            @Valid @RequestBody AddParticipantsRequest request) {

        ApiCustomResponse<String> response = chatService.addParticipants(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 8. REMOVE PARTICIPANT FROM GROUP
     * Usage: Remove a member from group conversation (admin only)
     * DELETE /api/v1/conversations/{id}/participants/{userId}
     */
    @DeleteMapping("/conversations/{id}/participants/{userId}")
    public ResponseEntity<ApiCustomResponse<String>> removeParticipant(
            @PathVariable UUID id,
            @PathVariable UUID userId) {

        ApiCustomResponse<String> response = chatService.removeParticipant(id, userId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 9. LEAVE GROUP CONVERSATION
     * Usage: User leaves a group chat
     * POST /api/v1/conversations/{id}/leave
     */
    @PostMapping("/conversations/{id}/leave")
    public ResponseEntity<ApiCustomResponse<String>> leaveConversation(
            @PathVariable UUID id) {

        ApiCustomResponse<String> response = chatService.leaveConversation(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 10. UPDATE GROUP NAME
     * Usage: Rename a group conversation (admin only)
     * PUT /api/v1/conversations/{id}
     */
    @PutMapping("/conversations/{id}")
    public ResponseEntity<ApiCustomResponse<String>> updateGroupName(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGroupNameRequest request) {

        ApiCustomResponse<String> response = chatService.updateGroupName(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 11. DELETE CONVERSATION
     * Usage: Delete a conversation (admin only for groups)
     * DELETE /api/v1/conversations/{id}
     */
    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<ApiCustomResponse<String>> deleteConversation(
            @PathVariable UUID id) {

        ApiCustomResponse<String> response = chatService.deleteConversation(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 12. EDIT MESSAGE
     * Usage: Edit a previously sent message
     * PUT /api/v1/messages/{id}
     */
    @PutMapping("/messages/{id}")
    public ResponseEntity<ApiCustomResponse<MessageDTO>> editMessage(
            @PathVariable UUID id,
            @Valid @RequestBody EditMessageRequest request) {

        ApiCustomResponse<MessageDTO> response = chatService.editMessage(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    /**
     * 13. DELETE MESSAGE
     * Usage: Delete a message
     * DELETE /api/v1/messages/{id}
     */
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<ApiCustomResponse<String>> deleteMessage(
            @PathVariable UUID id) {

        ApiCustomResponse<String> response = chatService.deleteMessage(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}

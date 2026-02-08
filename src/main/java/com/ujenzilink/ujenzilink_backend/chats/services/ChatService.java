package com.ujenzilink.ujenzilink_backend.chats.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.chats.dtos.*;
import com.ujenzilink.ujenzilink_backend.chats.enums.MessageStatus;
import com.ujenzilink.ujenzilink_backend.chats.enums.ParticipantRole;
import com.ujenzilink.ujenzilink_backend.chats.models.Conversation;
import com.ujenzilink.ujenzilink_backend.chats.models.ConversationParticipant;
import com.ujenzilink.ujenzilink_backend.chats.models.Message;
import com.ujenzilink.ujenzilink_backend.chats.models.MessageReadReceipt;
import com.ujenzilink.ujenzilink_backend.chats.repositories.ConversationParticipantRepository;
import com.ujenzilink.ujenzilink_backend.chats.repositories.ConversationRepository;
import com.ujenzilink.ujenzilink_backend.chats.repositories.MessageReadReceiptRepository;
import com.ujenzilink.ujenzilink_backend.chats.repositories.MessageRepository;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.dtos.CreatorInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageReadReceiptRepository readReceiptRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public ApiCustomResponse<List<ConversationSummaryDTO>> getConversationSummaries() {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Get all active conversations for user
        List<Conversation> conversations = conversationRepository.findActiveConversationsForUserId(currentUser.getId());

        // Map to summary DTOs
        List<ConversationSummaryDTO> summaries = conversations.stream()
                .map(conversation -> mapToConversationSummary(conversation, currentUser))
                .sorted((a, b) -> b.updatedAt().compareTo(a.updatedAt()))
                .collect(Collectors.toList());

        return new ApiCustomResponse<>(summaries, "Conversations retrieved successfully", HttpStatus.OK.value());
    }

    @Transactional(readOnly = true)
    public ApiCustomResponse<List<MessageDTO>> getConversationMessages(UUID conversationId) {

        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify conversation exists
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Conversation not found", HttpStatus.NOT_FOUND.value());
        }

        // Verify user is a participant
        boolean isParticipant = participantRepository
                .findByConversation_IdAndUser_IdAndLeftAtIsNull(conversationId, currentUser.getId())
                .isPresent();

        if (!isParticipant) {
            return new ApiCustomResponse<>(null, "You are not a participant of this conversation",
                    HttpStatus.FORBIDDEN.value());
        }

        // Get all messages (ordered by createdAt DESC - newest first)
        List<Message> messages = messageRepository.findByConversation_IdOrderByCreatedAtDesc(conversationId);

        // Map to DTOs
        List<MessageDTO> messageDTOs = messages.stream()
                .map(this::mapToMessageDTO)
                .collect(Collectors.toList());

        return new ApiCustomResponse<>(messageDTOs, "Messages retrieved successfully", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> sendMessage(UUID conversationId, SendMessageRequest request) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify conversation exists
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Conversation not found", HttpStatus.NOT_FOUND.value());
        }

        Conversation conversation = convOpt.get();

        // Verify user is a participant
        Optional<ConversationParticipant> participantOpt = participantRepository
                .findByConversation_IdAndUser_IdAndLeftAtIsNull(conversationId, currentUser.getId());

        if (participantOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "You are not a participant of this conversation",
                    HttpStatus.FORBIDDEN.value());
        }

        // Create and save message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setContent(request.content());
        message.setMessageType(request.messageType());
        message.setStatus(MessageStatus.SENT);

        messageRepository.save(message);

        // Update conversation's updatedAt timestamp
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return new ApiCustomResponse<>("Message sent successfully", "Message sent successfully",
                HttpStatus.CREATED.value());
    }

    private ConversationSummaryDTO mapToConversationSummary(Conversation conversation, User currentUser) {
        // Get participants
        List<ConversationParticipant> participants = participantRepository
                .findByConversationAndLeftAtIsNull(conversation);

        ConversationSummaryDTO.ChatUserDTO chatUser;

        if (conversation.isGroup()) {
            String name = conversation.getName() != null ? conversation.getName() : "Group Chat";
            // Group avatar: use a distinctive color and 'group' related initials
            String avatar = "https://ui-avatars.com/api/?name=" + name.replace(" ", "+")
                    + "&background=6366f1&color=fff&size=128";
            chatUser = new ConversationSummaryDTO.ChatUserDTO(
                    name,
                    "group",
                    avatar,
                    false);
        } else {
            // For direct chats, find the other participant
            User other = participants.stream()
                    .map(ConversationParticipant::getUser)
                    .filter(user -> !user.getId().equals(currentUser.getId()))
                    .findFirst()
                    .orElse(null);

            if (other != null) {
                String name = other.getFullName();
                String username = (other.getUserHandle() != null && !other.getUserHandle().isEmpty())
                        ? other.getUserHandle()
                        : other.getEmail();
                String profilePictureUrl = (other.getProfilePicture() != null)
                        ? other.getProfilePicture().getUrl()
                        : "https://i.pravatar.cc/150?u=" + username;

                chatUser = new ConversationSummaryDTO.ChatUserDTO(
                        name,
                        username,
                        profilePictureUrl,
                        false // Default to false
                );
            } else {
                chatUser = new ConversationSummaryDTO.ChatUserDTO(
                        "Deleted User",
                        "deleted",
                        "https://i.pravatar.cc/150?u=deleted",
                        false);
            }
        }

        // Get last message
        Optional<Message> lastMessageOpt = messageRepository.findLastMessageInConversationById(conversation.getId());
        String lastMessageText = "";
        Instant updatedAt = conversation.getUpdatedAt();

        if (lastMessageOpt.isPresent()) {
            Message lastMsg = lastMessageOpt.get();
            lastMessageText = lastMsg.getContent();
            updatedAt = lastMsg.getCreatedAt();
        }

        // Get unread count for current user
        int unreadCount = (int) messageRepository.countUnreadMessagesForUser(conversation.getId(),
                currentUser.getId());

        return new ConversationSummaryDTO(
                conversation.getId(),
                chatUser,
                lastMessageText,
                unreadCount,
                updatedAt,
                conversation.isGroup());
    }

    private MessageDTO mapToMessageDTO(Message message) {
        // Get current user to determine isMe
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        UUID currentUserId = userOpt.map(User::getId).orElse(null);

        // Get read receipts for group chats
        List<MessageDTO.ReadByDTO> readBy = new ArrayList<>();

        if (message.getConversation().isGroup()) {
            List<MessageReadReceipt> receipts = readReceiptRepository.findByMessage(message);
            readBy = receipts.stream()
                    .map(receipt -> new MessageDTO.ReadByDTO(
                            receipt.getUser().getId(),
                            receipt.getReadAt()))
                    .collect(Collectors.toList());
        }

        boolean isMe = currentUserId != null && message.getSender().getId().equals(currentUserId);

        return new MessageDTO(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getMessageType(),
                message.getStatus(),
                isMe,
                readBy,
                message.getCreatedAt());
    }

    private CreatorInfoDTO mapToCreatorInfoDTO(User user) {
        String name = user.getFullName();
        String username = (user.getUserHandle() != null && !user.getUserHandle().isEmpty())
                ? user.getUserHandle()
                : user.getEmail();
        String profilePictureUrl = (user.getProfilePicture() != null)
                ? user.getProfilePicture().getUrl()
                : "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random";

        return new CreatorInfoDTO(user.getId(), name, username, profilePictureUrl);
    }

    @Transactional
    public ApiCustomResponse<DirectConversationDTO> createDirectConversation(CreateDirectConversationRequest request) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();
        UUID otherUserId = request.participantId();

        // Check if conversation already exists
        Optional<Conversation> existingConv = conversationRepository
                .findDirectConversationBetweenUsers(currentUser.getId(), otherUserId);

        if (existingConv.isPresent()) {
            // Return existing conversation
            return new ApiCustomResponse<>(mapToDirectConversationDTO(existingConv.get(), currentUser),
                    "Conversation already exists", HttpStatus.OK.value());
        }

        // Create conversation
        Conversation conversation = new Conversation();
        conversation.setGroup(false);
        conversation.setCreatedBy(currentUser);

        Conversation savedConversation = conversationRepository.save(conversation);

        // Add creator as participant
        ConversationParticipant creatorParticipant = new ConversationParticipant();
        creatorParticipant.setConversation(savedConversation);
        creatorParticipant.setUser(currentUser);
        creatorParticipant.setRole(ParticipantRole.MEMBER);
        participantRepository.save(creatorParticipant);

        // Add other participant
        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversation(savedConversation);
        participant.setUser(new User());
        participant.getUser().setId(otherUserId);
        participant.setRole(ParticipantRole.MEMBER);
        participantRepository.save(participant);

        return new ApiCustomResponse<>(mapToDirectConversationDTO(savedConversation, currentUser),
                "Direct conversation created successfully", HttpStatus.CREATED.value());
    }

    @Transactional
    public ApiCustomResponse<GroupConversationDTO> createGroupConversation(CreateGroupConversationRequest request) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Create conversation
        Conversation conversation = new Conversation();
        conversation.setName(request.name());
        conversation.setGroup(true);
        conversation.setCreatedBy(currentUser);

        Conversation savedConversation = conversationRepository.save(conversation);

        // Add creator as participant (ADMIN for groups)
        ConversationParticipant creatorParticipant = new ConversationParticipant();
        creatorParticipant.setConversation(savedConversation);
        creatorParticipant.setUser(currentUser);
        creatorParticipant.setRole(ParticipantRole.ADMIN);
        participantRepository.save(creatorParticipant);

        // Add other participants
        for (UUID userId : request.participantIds()) {
            // Skip if trying to add self
            if (userId.equals(currentUser.getId())) {
                continue;
            }

            ConversationParticipant participant = new ConversationParticipant();
            participant.setConversation(savedConversation);
            participant.setUser(new User());
            participant.getUser().setId(userId);
            participant.setRole(ParticipantRole.MEMBER);
            participantRepository.save(participant);
        }

        return new ApiCustomResponse<>(mapToGroupConversationDTO(savedConversation),
                "Group conversation created successfully", HttpStatus.CREATED.value());
    }

    @Transactional(readOnly = true)
    public ApiCustomResponse<ConversationDTO> getConversationDetails(UUID conversationId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify conversation exists
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Conversation not found", HttpStatus.NOT_FOUND.value());
        }

        // Verify user is a participant
        boolean isParticipant = participantRepository
                .findByConversation_IdAndUser_IdAndLeftAtIsNull(conversationId, currentUser.getId())
                .isPresent();

        if (!isParticipant) {
            return new ApiCustomResponse<>(null, "You are not a participant of this conversation",
                    HttpStatus.FORBIDDEN.value());
        }

        ConversationDTO dto = mapToConversationDTO(convOpt.get());
        return new ApiCustomResponse<>(dto, "Conversation details retrieved successfully", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> markMessageAsRead(UUID messageId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify message exists
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Message not found", HttpStatus.NOT_FOUND.value());
        }

        Message message = messageOpt.get();

        // Verify user is a participant
        boolean isParticipant = participantRepository
                .findByConversation_IdAndUser_IdAndLeftAtIsNull(
                        message.getConversation().getId(), currentUser.getId())
                .isPresent();

        if (!isParticipant) {
            return new ApiCustomResponse<>(null, "You are not a participant of this conversation",
                    HttpStatus.FORBIDDEN.value());
        }

        // Check if already read
        boolean alreadyRead = readReceiptRepository.existsByMessage_IdAndUser_Id(messageId, currentUser.getId());
        if (alreadyRead) {
            return new ApiCustomResponse<>("Message already marked as read", "Message already read",
                    HttpStatus.OK.value());
        }

        // Create read receipt
        MessageReadReceipt receipt = new MessageReadReceipt();
        receipt.setMessage(message);
        receipt.setUser(currentUser);
        readReceiptRepository.save(receipt);

        return new ApiCustomResponse<>("Message marked as read", "Message marked as read successfully",
                HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> addParticipants(UUID conversationId, AddParticipantsRequest request) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify conversation exists and is a group
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Conversation not found", HttpStatus.NOT_FOUND.value());
        }

        Conversation conversation = convOpt.get();

        if (!conversation.isGroup()) {
            return new ApiCustomResponse<>(null, "Cannot add participants to direct conversations",
                    HttpStatus.BAD_REQUEST.value());
        }

        // Verify user is an admin
        boolean isAdmin = participantRepository.existsByConversation_IdAndUser_IdAndRoleAndLeftAtIsNull(
                conversationId, currentUser.getId(), ParticipantRole.ADMIN);

        if (!isAdmin) {
            return new ApiCustomResponse<>(null, "Only admins can add participants", HttpStatus.FORBIDDEN.value());
        }

        // Add participants
        int addedCount = 0;
        for (UUID userId : request.userIds()) {
            // Check if already a participant
            boolean exists = participantRepository
                    .findByConversation_IdAndUser_IdAndLeftAtIsNull(conversationId, userId)
                    .isPresent();

            if (!exists) {
                ConversationParticipant participant = new ConversationParticipant();
                participant.setConversation(conversation);
                participant.setUser(new User());
                participant.getUser().setId(userId);
                participant.setRole(ParticipantRole.MEMBER);
                participantRepository.save(participant);
                addedCount++;
            }
        }

        return new ApiCustomResponse<>(addedCount + " participants added",
                "Participants added successfully", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> removeParticipant(UUID conversationId, UUID userId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify conversation exists and is a group
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Conversation not found", HttpStatus.NOT_FOUND.value());
        }

        Conversation conversation = convOpt.get();

        if (!conversation.isGroup()) {
            return new ApiCustomResponse<>(null, "Cannot remove participants from direct conversations",
                    HttpStatus.BAD_REQUEST.value());
        }

        // Verify user is an admin
        boolean isAdmin = participantRepository.existsByConversation_IdAndUser_IdAndRoleAndLeftAtIsNull(
                conversationId, currentUser.getId(), ParticipantRole.ADMIN);

        if (!isAdmin) {
            return new ApiCustomResponse<>(null, "Only admins can remove participants", HttpStatus.FORBIDDEN.value());
        }

        // Find participant
        Optional<ConversationParticipant> participantOpt = participantRepository
                .findByConversation_IdAndUser_IdAndLeftAtIsNull(conversationId, userId);

        if (participantOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User is not a participant", HttpStatus.NOT_FOUND.value());
        }

        // Remove participant by setting leftAt
        ConversationParticipant participant = participantOpt.get();
        participant.setLeftAt(Instant.now());
        participantRepository.save(participant);

        return new ApiCustomResponse<>("Participant removed", "Participant removed successfully",
                HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> leaveConversation(UUID conversationId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify conversation exists
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Conversation not found", HttpStatus.NOT_FOUND.value());
        }

        Conversation conversation = convOpt.get();

        if (!conversation.isGroup()) {
            return new ApiCustomResponse<>(null, "Cannot leave direct conversations",
                    HttpStatus.BAD_REQUEST.value());
        }

        // Find participant
        Optional<ConversationParticipant> participantOpt = participantRepository
                .findByConversation_IdAndUser_IdAndLeftAtIsNull(conversationId, currentUser.getId());

        if (participantOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "You are not a participant", HttpStatus.NOT_FOUND.value());
        }

        // Leave by setting leftAt
        ConversationParticipant participant = participantOpt.get();
        participant.setLeftAt(Instant.now());
        participantRepository.save(participant);

        return new ApiCustomResponse<>("Left conversation", "Left conversation successfully", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> updateGroupName(UUID conversationId, UpdateGroupNameRequest request) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify conversation exists and is a group
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Conversation not found", HttpStatus.NOT_FOUND.value());
        }

        Conversation conversation = convOpt.get();

        if (!conversation.isGroup()) {
            return new ApiCustomResponse<>(null, "Cannot update name of direct conversations",
                    HttpStatus.BAD_REQUEST.value());
        }

        // Verify user is an admin
        boolean isAdmin = participantRepository.existsByConversation_IdAndUser_IdAndRoleAndLeftAtIsNull(
                conversationId, currentUser.getId(), ParticipantRole.ADMIN);

        if (!isAdmin) {
            return new ApiCustomResponse<>(null, "Only admins can update group name", HttpStatus.FORBIDDEN.value());
        }

        // Update name
        conversation.setName(request.name());
        conversationRepository.save(conversation);

        return new ApiCustomResponse<>("Group name updated", "Group name updated successfully", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> deleteConversation(UUID conversationId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify conversation exists
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Conversation not found", HttpStatus.NOT_FOUND.value());
        }

        Conversation conversation = convOpt.get();

        // For groups, verify user is admin
        if (conversation.isGroup()) {
            boolean isAdmin = participantRepository.existsByConversation_IdAndUser_IdAndRoleAndLeftAtIsNull(
                    conversationId, currentUser.getId(), ParticipantRole.ADMIN);

            if (!isAdmin) {
                return new ApiCustomResponse<>(null, "Only admins can delete group conversations",
                        HttpStatus.FORBIDDEN.value());
            }
        } else {
            // For direct chats, verify user is a participant
            boolean isParticipant = participantRepository
                    .findByConversation_IdAndUser_IdAndLeftAtIsNull(conversationId, currentUser.getId())
                    .isPresent();

            if (!isParticipant) {
                return new ApiCustomResponse<>(null, "You are not a participant", HttpStatus.FORBIDDEN.value());
            }
        }

        // Delete conversation (cascade will handle participants, messages, etc.)
        conversationRepository.delete(conversation);

        return new ApiCustomResponse<>("Conversation deleted", "Conversation deleted successfully",
                HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<MessageDTO> editMessage(UUID messageId, EditMessageRequest request) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify message exists
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Message not found", HttpStatus.NOT_FOUND.value());
        }

        Message message = messageOpt.get();

        // Verify user is the sender
        if (!message.getSender().getId().equals(currentUser.getId())) {
            return new ApiCustomResponse<>(null, "You can only edit your own messages", HttpStatus.FORBIDDEN.value());
        }

        // Update message content
        message.setContent(request.content());
        Message updatedMessage = messageRepository.save(message);

        MessageDTO dto = mapToMessageDTO(updatedMessage);
        return new ApiCustomResponse<>(dto, "Message edited successfully", HttpStatus.OK.value());
    }

    @Transactional
    public ApiCustomResponse<String> deleteMessage(UUID messageId) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "User not authenticated", HttpStatus.UNAUTHORIZED.value());
        }

        User currentUser = userOpt.get();

        // Verify message exists
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return new ApiCustomResponse<>(null, "Message not found", HttpStatus.NOT_FOUND.value());
        }

        Message message = messageOpt.get();

        // Verify user is the sender
        if (!message.getSender().getId().equals(currentUser.getId())) {
            return new ApiCustomResponse<>(null, "You can only delete your own messages", HttpStatus.FORBIDDEN.value());
        }

        // Delete message
        messageRepository.delete(message);

        return new ApiCustomResponse<>("Message deleted", "Message deleted successfully", HttpStatus.OK.value());
    }

    private DirectConversationDTO mapToDirectConversationDTO(Conversation conversation, User currentUser) {
        List<ConversationParticipant> participants = participantRepository
                .findByConversationAndLeftAtIsNull(conversation);

        User other = participants.stream()
                .map(ConversationParticipant::getUser)
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        ConversationSummaryDTO.ChatUserDTO chatUser = mapToChatUserDTO(other);

        return new DirectConversationDTO(conversation.getId(), chatUser, conversation.getCreatedAt());
    }

    private GroupConversationDTO mapToGroupConversationDTO(Conversation conversation) {
        List<ConversationParticipant> participants = participantRepository
                .findByConversationAndLeftAtIsNull(conversation);

        List<ConversationSummaryDTO.ChatUserDTO> participantDTOs = participants.stream()
                .map(p -> mapToChatUserDTO(p.getUser()))
                .collect(Collectors.toList());

        return new GroupConversationDTO(
                conversation.getId(),
                conversation.getName(),
                participantDTOs,
                conversation.getCreatedAt());
    }

    private ConversationSummaryDTO.ChatUserDTO mapToChatUserDTO(User user) {
        if (user == null) {
            return new ConversationSummaryDTO.ChatUserDTO(
                    "Deleted User",
                    "deleted",
                    "https://i.pravatar.cc/150?u=deleted",
                    false);
        }

        String name = user.getFullName() != null ? user.getFullName() : "";
        String username = (user.getUserHandle() != null && !user.getUserHandle().isEmpty())
                ? user.getUserHandle()
                : user.getEmail();
        String profilePictureUrl = (user.getProfilePicture() != null)
                ? user.getProfilePicture().getUrl()
                : "https://i.pravatar.cc/150?u=" + (username != null ? username : "unknown");

        return new ConversationSummaryDTO.ChatUserDTO(
                name,
                username,
                profilePictureUrl,
                false);
    }

    private ConversationDTO mapToConversationDTO(Conversation conversation) {
        // Get all participants (including those who left)
        List<ConversationParticipant> participants = participantRepository
                .findByConversation_Id(conversation.getId());

        List<ConversationDTO.ParticipantDTO> participantDTOs = participants.stream()
                .map(p -> new ConversationDTO.ParticipantDTO(
                        mapToCreatorInfoDTO(p.getUser()),
                        p.getRole(),
                        p.getJoinedAt(),
                        p.getLeftAt()))
                .collect(Collectors.toList());

        return new ConversationDTO(
                conversation.getId(),
                conversation.getName(),
                conversation.isGroup(),
                conversation.getProject() != null ? conversation.getProject().getId() : null,
                mapToCreatorInfoDTO(conversation.getCreatedBy()),
                participantDTOs,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }
}

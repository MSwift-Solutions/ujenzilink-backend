package com.ujenzilink.ujenzilink_backend.chats.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.chats.enums.ParticipantRole;
import com.ujenzilink.ujenzilink_backend.chats.models.Conversation;
import com.ujenzilink.ujenzilink_backend.chats.models.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, UUID> {

    // Find all participants in a conversation (active only)
    List<ConversationParticipant> findByConversationAndLeftAtIsNull(Conversation conversation);

    List<ConversationParticipant> findByConversation_IdAndLeftAtIsNull(UUID conversationId);

    // Find all participants including those who left
    List<ConversationParticipant> findByConversation(Conversation conversation);

    List<ConversationParticipant> findByConversation_Id(UUID conversationId);

    // Check if user is participant
    Optional<ConversationParticipant> findByConversationAndUserAndLeftAtIsNull(
            Conversation conversation, User user);

    Optional<ConversationParticipant> findByConversation_IdAndUser_IdAndLeftAtIsNull(
            UUID conversationId, UUID userId);

    // Find by user
    List<ConversationParticipant> findByUserAndLeftAtIsNull(User user);

    List<ConversationParticipant> findByUser_IdAndLeftAtIsNull(UUID userId);

    // Find admins in a conversation
    List<ConversationParticipant> findByConversationAndRoleAndLeftAtIsNull(
            Conversation conversation, ParticipantRole role);

    List<ConversationParticipant> findByConversation_IdAndRoleAndLeftAtIsNull(
            UUID conversationId, ParticipantRole role);

    // Count active participants
    @Query("SELECT COUNT(cp) FROM ConversationParticipant cp " +
            "WHERE cp.conversation = :conversation AND cp.leftAt IS NULL")
    long countActiveParticipants(@Param("conversation") Conversation conversation);

    @Query("SELECT COUNT(cp) FROM ConversationParticipant cp " +
            "WHERE cp.conversation.id = :conversationId AND cp.leftAt IS NULL")
    long countActiveParticipantsByConversationId(@Param("conversationId") UUID conversationId);

    // Check if user has specific role
    boolean existsByConversation_IdAndUser_IdAndRoleAndLeftAtIsNull(
            UUID conversationId, UUID userId, ParticipantRole role);
}

package com.ujenzilink.ujenzilink_backend.chats.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.chats.models.Conversation;
import com.ujenzilink.ujenzilink_backend.chats.models.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Find messages in a conversation (paginated)
    List<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);

    List<Message> findByConversation_IdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    // Cursor-based pagination for messages
    List<Message> findByConversationAndCreatedAtBeforeOrderByCreatedAtDesc(
            Conversation conversation, Instant cursor, Pageable pageable);

    List<Message> findByConversation_IdAndCreatedAtBeforeOrderByCreatedAtDesc(
            UUID conversationId, Instant cursor, Pageable pageable);

    // Find the last message in a conversation
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation = :conversation " +
            "ORDER BY m.createdAt DESC " +
            "LIMIT 1")
    Optional<Message> findLastMessageInConversation(@Param("conversation") Conversation conversation);

    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.createdAt DESC " +
            "LIMIT 1")
    Optional<Message> findLastMessageInConversationById(@Param("conversationId") UUID conversationId);

    // Count messages in a conversation
    long countByConversation(Conversation conversation);

    long countByConversation_Id(UUID conversationId);

    // Find messages by sender
    List<Message> findBySender(User sender, Pageable pageable);

    List<Message> findBySender_Id(UUID senderId, Pageable pageable);

    // Count unread messages for a user in a conversation
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.sender.id != :userId " +
            "AND NOT EXISTS (SELECT mrr FROM MessageReadReceipt mrr " +
            "                WHERE mrr.message = m AND mrr.user.id = :userId)")
    long countUnreadMessagesForUser(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId);
}

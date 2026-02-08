package com.ujenzilink.ujenzilink_backend.chats.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.chats.models.Conversation;
import com.ujenzilink.ujenzilink_backend.projects.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

        // Find conversations by project
        Optional<Conversation> findByProject(Project project);

        Optional<Conversation> findByProject_Id(UUID projectId);

        // Find all conversations for a user (via participants)
        @Query("SELECT DISTINCT c FROM Conversation c " +
                        "JOIN ConversationParticipant cp ON cp.conversation = c " +
                        "WHERE cp.user = :user AND cp.leftAt IS NULL " +
                        "ORDER BY c.updatedAt DESC")
        List<Conversation> findActiveConversationsForUser(@Param("user") User user);

        @Query("SELECT DISTINCT c FROM Conversation c " +
                        "JOIN ConversationParticipant cp ON cp.conversation = c " +
                        "WHERE cp.user.id = :userId AND cp.leftAt IS NULL " +
                        "ORDER BY c.updatedAt DESC")
        List<Conversation> findActiveConversationsForUserId(@Param("userId") UUID userId);

        // Find direct conversation between two users
        @Query("SELECT c FROM Conversation c " +
                        "WHERE c.isGroup = false AND c.deletedAt IS NULL " +
                        "AND EXISTS (SELECT cp1 FROM ConversationParticipant cp1 " +
                        "            WHERE cp1.conversation = c AND cp1.user.id = :user1Id AND cp1.leftAt IS NULL) " +
                        "AND EXISTS (SELECT cp2 FROM ConversationParticipant cp2 " +
                        "            WHERE cp2.conversation = c AND cp2.user.id = :user2Id AND cp2.leftAt IS NULL) " +
                        "AND (SELECT COUNT(cp3) FROM ConversationParticipant cp3 " +
                        "     WHERE cp3.conversation = c AND cp3.leftAt IS NULL) = (CASE WHEN :user1Id = :user2Id THEN 1 ELSE 2 END)")
        Optional<Conversation> findDirectConversationBetweenUsers(
                        @Param("user1Id") UUID user1Id,
                        @Param("user2Id") UUID user2Id);

        // Find group conversations created by user
        List<Conversation> findByIsGroupTrueAndCreatedBy(User createdBy);

        List<Conversation> findByIsGroupTrueAndCreatedBy_Id(UUID createdById);
}

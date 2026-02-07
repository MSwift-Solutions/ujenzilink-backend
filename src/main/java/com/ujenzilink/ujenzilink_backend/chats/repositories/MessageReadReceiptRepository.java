package com.ujenzilink.ujenzilink_backend.chats.repositories;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.chats.models.Message;
import com.ujenzilink.ujenzilink_backend.chats.models.MessageReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageReadReceiptRepository extends JpaRepository<MessageReadReceipt, UUID> {

    // Find all read receipts for a message
    List<MessageReadReceipt> findByMessage(Message message);

    List<MessageReadReceipt> findByMessage_Id(UUID messageId);

    // Check if user has read a message
    Optional<MessageReadReceipt> findByMessageAndUser(Message message, User user);

    Optional<MessageReadReceipt> findByMessage_IdAndUser_Id(UUID messageId, UUID userId);

    boolean existsByMessageAndUser(Message message, User user);

    boolean existsByMessage_IdAndUser_Id(UUID messageId, UUID userId);

    // Find all messages read by a user
    List<MessageReadReceipt> findByUser(User user);

    List<MessageReadReceipt> findByUser_Id(UUID userId);

    // Count how many users have read a message
    long countByMessage(Message message);

    long countByMessage_Id(UUID messageId);

    // Find users who have read a specific message
    @Query("SELECT mrr.user FROM MessageReadReceipt mrr WHERE mrr.message = :message")
    List<User> findUsersWhoReadMessage(@Param("message") Message message);

    @Query("SELECT mrr.user FROM MessageReadReceipt mrr WHERE mrr.message.id = :messageId")
    List<User> findUsersWhoReadMessageById(@Param("messageId") UUID messageId);
}

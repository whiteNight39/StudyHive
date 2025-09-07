package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Message;
import com.studyhive.model.entity.Room;
import com.studyhive.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Modifying
    @Query("UPDATE Message m SET m.messageStatus = 'DELETED' WHERE m.messageId IN :messageId")
    void deleteByMessageId(@Param("messageId") UUID messageId);

    Optional<Message> findByMessageIdAndMessageStatus(UUID messageId, String messageStatus);

    @Query("SELECT m FROM Message m WHERE m.messageRecipient = :recipient AND m.messageSender = :sender AND m.messageStatus != 'DELETED'")
    Page<Message> loadPersonalMessages(@Param("sender")User Sender,
                                       @Param("recipient") User recipient,
                                       Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.messageRoom = :rooom AND m.messageSender = :sender AND m.messageStatus != 'DELETED'")
    Page<Message> loadRoomMessages(@Param("sender")User Sender,
                                   @Param("room") Room room,
                                   Pageable pageable);
}

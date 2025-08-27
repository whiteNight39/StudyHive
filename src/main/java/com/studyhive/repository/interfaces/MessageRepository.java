package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Message;
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
}

package com.ga.lanceify.communication.repository;

import com.ga.lanceify.communication.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    long countByConversationIdAndRecipientIdAndReadFalse(String conversationId, String recipientId);

    long countByRecipientIdAndReadFalse(String recipientId);

    Optional<Message> findBySenderIdAndClientMessageId(String senderId, String clientMessageId);
}

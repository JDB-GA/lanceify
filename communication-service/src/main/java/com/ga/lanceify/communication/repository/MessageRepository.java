package com.ga.lanceify.communication.repository;

import com.ga.lanceify.communication.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    List<Message> findByConversationIdAndRecipientIdAndReadFalse(String conversationId, String recipientId);

    long countByConversationIdAndRecipientIdAndReadFalse(String conversationId, String recipientId);
}

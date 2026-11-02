package com.ga.lanceify.communication.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "messages")
@CompoundIndexes({
        @CompoundIndex(name = "conversation_timeline", def = "{'conversationId': 1, 'createdAt': 1}"),
        @CompoundIndex(name = "unread_by_recipient", def = "{'conversationId': 1, 'recipientId': 1, 'read': 1}"),
        @CompoundIndex(name = "idempotent_send", def = "{'senderId': 1, 'clientMessageId': 1}", unique = true, sparse = true)
})
public class Message {

    @Id
    private String id;

    private String conversationId;

    private String senderId;

    private String recipientId;

    private String content;

    private String clientMessageId;

    private boolean read = false;

    private Instant createdAt = Instant.now();

    public Message() {}

    public Message(String id, String conversationId, String senderId, String recipientId, String content) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.read = false;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClientMessageId() {
        return clientMessageId;
    }

    public void setClientMessageId(String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

package com.ga.lanceify.communication.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;

    @Indexed
    private List<String> participantIds = new ArrayList<>();

    @Indexed(unique = true, sparse = true)
    private String participantKey;

    private String projectId;

    private String lastMessage;

    private String lastSenderId;

    private Instant lastMessageAt;

    private Instant createdAt = Instant.now();

    public Conversation() {}

    public Conversation(String id, List<String> participantIds, String projectId) {
        this.id = id;
        this.participantIds = (participantIds != null) ? participantIds : new ArrayList<>();
        this.projectId = projectId;
        this.participantKey = keyFor(this.participantIds, projectId);
        this.createdAt = Instant.now();
        this.lastMessageAt = this.createdAt;
    }

    public static String keyFor(List<String> participantIds, String projectId) {
        List<String> sorted = participantIds.stream().sorted().toList();
        return String.join("|", sorted) + "#" + (projectId == null ? "" : projectId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public String getParticipantKey() {
        return participantKey;
    }

    public void setParticipantKey(String participantKey) {
        this.participantKey = participantKey;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastSenderId() {
        return lastSenderId;
    }

    public void setLastSenderId(String lastSenderId) {
        this.lastSenderId = lastSenderId;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

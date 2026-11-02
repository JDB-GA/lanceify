package com.ga.lanceify.communication.dto;

import java.time.Instant;
import java.util.List;

public final class CommunicationDtos {

    private CommunicationDtos() {}

    public record StartConversationRequest(
            String recipientId,
            String projectId
    ) {}

    public record SendMessageRequest(
            String content
    ) {}

    public record ConversationResponse(
            String id,
            List<String> participantIds,
            String projectId,
            String lastMessage,
            Instant lastMessageAt,
            Instant createdAt,
            long unreadCount
    ) {}

    public record MessageResponse(
            String id,
            String conversationId,
            String senderId,
            String recipientId,
            String content,
            boolean read,
            Instant createdAt
    ) {}
}

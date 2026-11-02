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
            String content,
            String clientMessageId
    ) {}

    public record ConversationResponse(
            String id,
            List<String> participantIds,
            String projectId,
            String lastMessage,
            String lastSenderId,
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
            String clientMessageId,
            boolean read,
            Instant createdAt
    ) {}

    public record ReadReceipt(
            String conversationId,
            String readerId,
            Instant readAt
    ) {}

    public record UnreadCountResponse(
            long count
    ) {}
}

package com.ga.lanceify.communication.service;

import com.ga.lanceify.communication.dto.CommunicationDtos.ConversationResponse;
import com.ga.lanceify.communication.dto.CommunicationDtos.MessageResponse;
import com.ga.lanceify.communication.dto.CommunicationDtos.SendMessageRequest;
import com.ga.lanceify.communication.dto.CommunicationDtos.StartConversationRequest;
import com.ga.lanceify.communication.model.Conversation;
import com.ga.lanceify.communication.model.Message;
import com.ga.lanceify.communication.repository.ConversationRepository;
import com.ga.lanceify.communication.repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommunicationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SseNotificationService sseNotificationService;

    public CommunicationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            SseNotificationService sseNotificationService
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.sseNotificationService = sseNotificationService;
    }

    public ConversationResponse startOrGetConversation(String currentUserId, StartConversationRequest req) {
        if (req.recipientId() == null || req.recipientId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient ID is required");
        }
        String cleanRecipientId = req.recipientId().trim();
        if (cleanRecipientId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start a conversation with yourself");
        }

        Optional<Conversation> existing;
        if (req.projectId() != null && !req.projectId().isBlank()) {
            existing = conversationRepository.findByParticipantIdsContainingAndParticipantIdsContainingAndProjectId(
                    currentUserId, cleanRecipientId, req.projectId().trim());
        } else {
            existing = conversationRepository.findByParticipantIdsContainingAndParticipantIdsContaining(
                    currentUserId, cleanRecipientId);
        }

        Conversation conversation = existing.orElseGet(() -> {
            Conversation created = new Conversation(
                    UUID.randomUUID().toString(),
                    List.of(currentUserId, cleanRecipientId),
                    req.projectId() != null ? req.projectId().trim() : null
            );
            return conversationRepository.save(created);
        });

        return toConversationResponse(conversation, currentUserId);
    }

    public List<ConversationResponse> getUserConversations(String currentUserId) {
        return conversationRepository.findByParticipantIdsContainingOrderByLastMessageAtDesc(currentUserId)
                .stream()
                .map(c -> toConversationResponse(c, currentUserId))
                .toList();
    }

    public ConversationResponse getConversationDetails(String currentUserId, String conversationId) {
        Conversation conversation = getAndVerifyParticipant(conversationId, currentUserId);
        return toConversationResponse(conversation, currentUserId);
    }

    public List<MessageResponse> getConversationMessages(String currentUserId, String conversationId) {
        getAndVerifyParticipant(conversationId, currentUserId);

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        List<Message> unreadForUser = messages.stream()
                .filter(m -> currentUserId.equals(m.getRecipientId()) && !m.isRead())
                .toList();

        if (!unreadForUser.isEmpty()) {
            unreadForUser.forEach(m -> m.setRead(true));
            messageRepository.saveAll(unreadForUser);
        }

        return messages.stream().map(this::toMessageResponse).toList();
    }

    public MessageResponse sendMessage(String currentUserId, String conversationId, SendMessageRequest req) {
        if (req.content() == null || req.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be empty");
        }
        String cleanContent = req.content().trim();
        if (cleanContent.length() > 3000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message exceeds 3000 character limit");
        }

        Conversation conversation = getAndVerifyParticipant(conversationId, currentUserId);

        String recipientId = conversation.getParticipantIds().stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Recipient not found"));

        Message message = new Message(
                UUID.randomUUID().toString(),
                conversationId,
                currentUserId,
                recipientId,
                cleanContent
        );
        Message savedMessage = messageRepository.save(message);

        conversation.setLastMessage(cleanContent);
        conversation.setLastMessageAt(savedMessage.getCreatedAt());
        conversationRepository.save(conversation);

        MessageResponse response = toMessageResponse(savedMessage);

        sseNotificationService.broadcastMessage(conversationId, response);

        return response;
    }

    public void markConversationAsRead(String currentUserId, String conversationId) {
        getAndVerifyParticipant(conversationId, currentUserId);
        List<Message> unread = messageRepository.findByConversationIdAndRecipientIdAndReadFalse(conversationId, currentUserId);
        if (!unread.isEmpty()) {
            unread.forEach(m -> m.setRead(true));
            messageRepository.saveAll(unread);
        }
    }

    public SseEmitter subscribeToStream(String currentUserId, String conversationId) {
        getAndVerifyParticipant(conversationId, currentUserId);
        return sseNotificationService.subscribeConversation(conversationId, currentUserId);
    }

    private Conversation getAndVerifyParticipant(String conversationId, String currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant in this conversation");
        }
        return conversation;
    }

    private ConversationResponse toConversationResponse(Conversation conversation, String currentUserId) {
        long unreadCount = messageRepository.countByConversationIdAndRecipientIdAndReadFalse(
                conversation.getId(), currentUserId);

        return new ConversationResponse(
                conversation.getId(),
                conversation.getParticipantIds(),
                conversation.getProjectId(),
                conversation.getLastMessage(),
                conversation.getLastMessageAt(),
                conversation.getCreatedAt(),
                unreadCount
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getRecipientId(),
                message.getContent(),
                message.isRead(),
                message.getCreatedAt()
        );
    }
}

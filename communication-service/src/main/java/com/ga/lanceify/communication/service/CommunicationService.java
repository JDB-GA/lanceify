package com.ga.lanceify.communication.service;

import com.ga.lanceify.communication.dto.CommunicationDtos.ConversationResponse;
import com.ga.lanceify.communication.dto.CommunicationDtos.MessageResponse;
import com.ga.lanceify.communication.dto.CommunicationDtos.ReadReceipt;
import com.ga.lanceify.communication.dto.CommunicationDtos.SendMessageRequest;
import com.ga.lanceify.communication.dto.CommunicationDtos.StartConversationRequest;
import com.ga.lanceify.communication.dto.CommunicationDtos.UnreadCountResponse;
import com.ga.lanceify.communication.model.Conversation;
import com.ga.lanceify.communication.model.Message;
import com.ga.lanceify.communication.repository.ConversationRepository;
import com.ga.lanceify.communication.repository.MessageRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommunicationService {

    private static final int MAX_CONTENT = 3000;
    private static final int MAX_PREVIEW = 140;
    private static final int MAX_CLIENT_ID = 64;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SseNotificationService sseNotificationService;
    private final MongoTemplate mongoTemplate;

    public CommunicationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            SseNotificationService sseNotificationService,
            MongoTemplate mongoTemplate
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.sseNotificationService = sseNotificationService;
        this.mongoTemplate = mongoTemplate;
    }

    public ConversationResponse startOrGetConversation(String currentUserId, StartConversationRequest req) {
        if (req.recipientId() == null || req.recipientId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient ID is required");
        }
        String recipientId = req.recipientId().trim();
        if (recipientId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start a conversation with yourself");
        }
        String projectId = (req.projectId() == null || req.projectId().isBlank()) ? null : req.projectId().trim();
        List<String> participants = List.of(currentUserId, recipientId);
        String key = Conversation.keyFor(participants, projectId);

        Conversation conversation = conversationRepository.findByParticipantKey(key)
                .orElseGet(() -> createOrFetch(participants, projectId, key));

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
        Conversation conversation = getAndVerifyParticipant(conversationId, currentUserId);
        markRead(conversation, currentUserId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    public MessageResponse sendMessage(String currentUserId, String conversationId, SendMessageRequest req) {
        if (req.content() == null || req.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be empty");
        }
        String content = req.content().trim();
        if (content.length() > MAX_CONTENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message exceeds 3000 character limit");
        }
        String clientMessageId = (req.clientMessageId() == null || req.clientMessageId().isBlank())
                ? null : req.clientMessageId().trim();
        if (clientMessageId != null && clientMessageId.length() > MAX_CLIENT_ID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client message ID is too long");
        }

        Conversation conversation = getAndVerifyParticipant(conversationId, currentUserId);

        if (clientMessageId != null) {
            Optional<Message> duplicate = messageRepository.findBySenderIdAndClientMessageId(currentUserId, clientMessageId);
            if (duplicate.isPresent()) {
                return toMessageResponse(duplicate.get());
            }
        }

        String recipientId = conversation.getParticipantIds().stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Recipient not found"));

        Message message = new Message(UUID.randomUUID().toString(), conversationId, currentUserId, recipientId, content);
        message.setClientMessageId(clientMessageId);

        Message saved;
        try {
            saved = messageRepository.insert(message);
        } catch (DuplicateKeyException e) {
            return messageRepository.findBySenderIdAndClientMessageId(currentUserId, clientMessageId)
                    .map(this::toMessageResponse)
                    .orElseThrow(() -> e);
        }

        advanceLastMessage(conversationId, saved);

        MessageResponse response = toMessageResponse(saved);
        sseNotificationService.publishToConversation(conversationId, SseNotificationService.NEW_MESSAGE, response);
        sseNotificationService.publishToUsers(conversation.getParticipantIds(), SseNotificationService.NEW_MESSAGE, response);
        return response;
    }

    public void markConversationAsRead(String currentUserId, String conversationId) {
        Conversation conversation = getAndVerifyParticipant(conversationId, currentUserId);
        markRead(conversation, currentUserId);
    }

    public UnreadCountResponse getUnreadCount(String currentUserId) {
        return new UnreadCountResponse(messageRepository.countByRecipientIdAndReadFalse(currentUserId));
    }

    public SseEmitter subscribeToStream(String currentUserId, String conversationId) {
        getAndVerifyParticipant(conversationId, currentUserId);
        return sseNotificationService.subscribeConversation(conversationId, currentUserId);
    }

    public SseEmitter subscribeToInbox(String currentUserId) {
        return sseNotificationService.subscribeUser(currentUserId);
    }

    private Conversation createOrFetch(List<String> participants, String projectId, String key) {
        try {
            return conversationRepository.insert(new Conversation(UUID.randomUUID().toString(), participants, projectId));
        } catch (DuplicateKeyException e) {
            return conversationRepository.findByParticipantKey(key).orElseThrow(() -> e);
        }
    }

    private void advanceLastMessage(String conversationId, Message message) {
        Query query = Query.query(new Criteria().andOperator(
                Criteria.where("_id").is(conversationId),
                new Criteria().orOperator(
                        Criteria.where("lastMessageAt").lte(message.getCreatedAt()),
                        Criteria.where("lastMessageAt").is(null))));
        Update update = new Update()
                .set("lastMessage", preview(message.getContent()))
                .set("lastSenderId", message.getSenderId())
                .set("lastMessageAt", message.getCreatedAt());
        mongoTemplate.updateFirst(query, update, Conversation.class);
    }

    private void markRead(Conversation conversation, String readerId) {
        Query query = Query.query(Criteria.where("conversationId").is(conversation.getId())
                .and("recipientId").is(readerId)
                .and("read").is(false));
        long changed = mongoTemplate.updateMulti(query, Update.update("read", true), Message.class).getModifiedCount();
        if (changed > 0) {
            ReadReceipt receipt = new ReadReceipt(conversation.getId(), readerId, Instant.now());
            sseNotificationService.publishToConversation(conversation.getId(), SseNotificationService.READ, receipt);
            sseNotificationService.publishToUsers(conversation.getParticipantIds(), SseNotificationService.READ, receipt);
        }
    }

    private String preview(String content) {
        return content.length() <= MAX_PREVIEW ? content : content.substring(0, MAX_PREVIEW - 3) + "...";
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
                conversation.getLastSenderId(),
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
                message.getClientMessageId(),
                message.isRead(),
                message.getCreatedAt()
        );
    }
}

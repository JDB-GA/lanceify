package com.ga.lanceify.communication.service;

import com.ga.lanceify.communication.dto.CommunicationDtos.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SseNotificationService.class);
    private static final Long SSE_TIMEOUT = 30 * 60 * 1000L;

    private final Map<String, List<SseEmitter>> conversationEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeConversation(String conversationId, String userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        conversationEmitters.computeIfAbsent(conversationId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(conversationId, emitter));
        emitter.onTimeout(() -> removeEmitter(conversationId, emitter));
        emitter.onError(e -> removeEmitter(conversationId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected to conversation stream: " + conversationId));
        } catch (IOException e) {
            removeEmitter(conversationId, emitter);
        }

        log.debug("User {} subscribed to SSE stream for conversation {}", userId, conversationId);
        return emitter;
    }

    public void broadcastMessage(String conversationId, MessageResponse message) {
        List<SseEmitter> emitters = conversationEmitters.get(conversationId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("NEW_MESSAGE")
                        .data(message));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }

    private void removeEmitter(String conversationId, SseEmitter emitter) {
        List<SseEmitter> emitters = conversationEmitters.get(conversationId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                conversationEmitters.remove(conversationId);
            }
        }
    }
}

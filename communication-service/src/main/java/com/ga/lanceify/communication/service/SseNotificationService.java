package com.ga.lanceify.communication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@Service
public class SseNotificationService {

    public static final String NEW_MESSAGE = "NEW_MESSAGE";
    public static final String READ = "READ";

    private static final Logger log = LoggerFactory.getLogger(SseNotificationService.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;
    private static final int MAX_STREAMS_PER_KEY = 10;

    private final Map<String, List<SseEmitter>> conversationEmitters = new ConcurrentHashMap<>();
    private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeConversation(String conversationId, String userId) {
        log.debug("User {} subscribed to conversation {}", userId, conversationId);
        return register(conversationEmitters, conversationId);
    }

    public SseEmitter subscribeUser(String userId) {
        log.debug("User {} subscribed to inbox stream", userId);
        return register(userEmitters, userId);
    }

    public void publishToConversation(String conversationId, String event, Object payload) {
        send(conversationEmitters, conversationId, () -> SseEmitter.event().name(event).data(payload));
    }

    public void publishToUsers(Collection<String> userIds, String event, Object payload) {
        userIds.forEach(userId -> send(userEmitters, userId, () -> SseEmitter.event().name(event).data(payload)));
    }

    @Scheduled(fixedRate = 25_000)
    public void heartbeat() {
        conversationEmitters.keySet().forEach(key -> send(conversationEmitters, key, () -> SseEmitter.event().comment("ping")));
        userEmitters.keySet().forEach(key -> send(userEmitters, key, () -> SseEmitter.event().comment("ping")));
    }

    private SseEmitter register(Map<String, List<SseEmitter>> registry, String key) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        List<SseEmitter> evicted = new ArrayList<>();
        registry.compute(key, (k, existing) -> {
            List<SseEmitter> list = existing != null ? existing : new CopyOnWriteArrayList<>();
            while (list.size() >= MAX_STREAMS_PER_KEY) {
                evicted.add(list.remove(0));
            }
            list.add(emitter);
            return list;
        });
        evicted.forEach(SseEmitter::complete);

        emitter.onCompletion(() -> remove(registry, key, emitter));
        emitter.onTimeout(() -> remove(registry, key, emitter));
        emitter.onError(e -> remove(registry, key, emitter));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("connected"));
        } catch (IOException | IllegalStateException e) {
            remove(registry, key, emitter);
        }
        return emitter;
    }

    private void send(Map<String, List<SseEmitter>> registry, String key, Supplier<SseEmitter.SseEventBuilder> event) {
        List<SseEmitter> emitters = registry.get(key);
        if (emitters == null) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event.get());
            } catch (IOException | IllegalStateException e) {
                remove(registry, key, emitter);
            }
        }
    }

    private void remove(Map<String, List<SseEmitter>> registry, String key, SseEmitter emitter) {
        registry.computeIfPresent(key, (k, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }
}

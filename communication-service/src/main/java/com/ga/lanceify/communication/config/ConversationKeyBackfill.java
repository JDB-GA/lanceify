package com.ga.lanceify.communication.config;

import com.ga.lanceify.communication.model.Conversation;
import com.ga.lanceify.communication.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
public class ConversationKeyBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ConversationKeyBackfill.class);

    private final ConversationRepository conversationRepository;

    public ConversationKeyBackfill(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (Conversation conversation : conversationRepository.findByParticipantKeyIsNull()) {
            conversation.setParticipantKey(Conversation.keyFor(conversation.getParticipantIds(), conversation.getProjectId()));
            try {
                conversationRepository.save(conversation);
            } catch (DuplicateKeyException e) {
                log.warn("Conversation {} duplicates an existing thread and keeps no key", conversation.getId());
            }
        }
    }

}

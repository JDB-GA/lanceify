package com.ga.lanceify.communication.controller;

import com.ga.lanceify.communication.dto.CommunicationDtos.ConversationResponse;
import com.ga.lanceify.communication.dto.CommunicationDtos.MessageResponse;
import com.ga.lanceify.communication.dto.CommunicationDtos.SendMessageRequest;
import com.ga.lanceify.communication.dto.CommunicationDtos.StartConversationRequest;
import com.ga.lanceify.communication.service.CommunicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final CommunicationService communicationService;

    public ConversationController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> startConversation(
            Authentication authentication,
            @RequestBody StartConversationRequest request
    ) {
        ConversationResponse response = communicationService.startOrGetConversation(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getUserConversations(Authentication authentication) {
        List<ConversationResponse> conversations = communicationService.getUserConversations(authentication.getName());
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getConversation(
            Authentication authentication,
            @PathVariable String id
    ) {
        ConversationResponse response = communicationService.getConversationDetails(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            Authentication authentication,
            @PathVariable String id
    ) {
        List<MessageResponse> messages = communicationService.getConversationMessages(authentication.getName(), id);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            Authentication authentication,
            @PathVariable String id,
            @RequestBody SendMessageRequest request
    ) {
        MessageResponse response = communicationService.sendMessage(authentication.getName(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamConversation(
            Authentication authentication,
            @PathVariable String id
    ) {
        return communicationService.subscribeToStream(authentication.getName(), id);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            Authentication authentication,
            @PathVariable String id
    ) {
        communicationService.markConversationAsRead(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}

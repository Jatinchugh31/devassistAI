package com.devassist.model;

import com.devassist.repository.RedisChatMemoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis-backed ChatMemory implementation that stores serialized Message JSON in a Redis list.
 *
 * Implements the Spring AI ChatMemory interface:
 *   void add(String conversationId, List<Message> messages);
 *   List<Message> get(String conversationId);
 *   void clear(String conversationId);
 *
 * This implementation attempts to (de)serialize the full Message polymorphic JSON via Jackson.
 * If that fails, it falls back to a MinimalMessage { role, content } representation.
 */
@Component
public class RedisChatMemory implements ChatMemory {

    private static final Logger log = LoggerFactory.getLogger(RedisChatMemory.class);

    private final RedisChatMemoryRepository repo;
    private final ObjectMapper mapper;

    public RedisChatMemory(RedisChatMemoryRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (conversationId == null || conversationId.isBlank()) {
            log.warn("conversationId is null or blank; skipping add()");
            return;
        }
        if (messages == null || messages.isEmpty()) {
            log.debug("No messages to add for conversationId={}", conversationId);
            return;
        }

        log.info("💾 ADDING {} messages to Redis for conversationId={}", messages.size(), conversationId);
        for (Message m : messages) {
            log.debug("  - Message type: {}, content preview: {}", 
                m.getClass().getSimpleName(), 
                safeMessageContent(m).substring(0, Math.min(50, safeMessageContent(m).length())));
            try {
                // Create a wrapper object with type info
                MessageWrapper wrapper = new MessageWrapper(m);
                String json = mapper.writeValueAsString(wrapper);
                repo.append(conversationId, json);
                log.debug("  ✓ Stored as: {}", wrapper.messageType);
            } catch (Exception ex) {
                log.error("Failed to serialize message for conversationId={}: {}", conversationId, ex.getMessage(), ex);
            }
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        log.info("📖 RETRIEVING messages for conversationId={}", conversationId);
        List<String> raw = repo.readAll(conversationId);
        log.info("   Found {} raw JSON entries in Redis", raw.size());
        
        List<Message> out = new ArrayList<>(raw.size());
        for (String json : raw) {
            if (json == null || json.isBlank()) continue;
            try {
                // Deserialize the wrapper which contains type info
                MessageWrapper wrapper = mapper.readValue(json, MessageWrapper.class);
                Message m = wrapper.toMessage();
                out.add(m);
                log.debug("  ✓ Loaded: {} with {} chars", wrapper.messageType, wrapper.content.length());
            } catch (Exception e) {
                log.warn("Could not deserialize message from Redis: {}", e.getMessage());
            }
        }
        log.info("   Returning {} Message objects to advisor", out.size());
        return out;
    }

    @Override
    public void clear(String conversationId) {
        repo.clear(conversationId);
    }

    // Helper: extract a simple text from Message (best-effort)
    private String safeMessageContent(Message m) {
        try {
            String content = m.getText();
            if (content != null) return content;
        } catch (Exception e) {
            // ignore
        }
        return m.toString();
    }

    /**
     * MessageWrapper - Simple wrapper for Redis storage.
     * Stores the message type (system/user/assistant) and content separately
     * to avoid Jackson polymorphic deserialization issues.
     */
    public static class MessageWrapper {
        private String messageType;  // "system", "user", or "assistant"
        private String content;
        
        // Default constructor for Jackson
        public MessageWrapper() {
        }
        
        // Constructor that extracts type and content from Message
        public MessageWrapper(Message message) {
            this.content = extractContent(message);
            this.messageType = determineType(message);
        }
        
        private String extractContent(Message m) {
            try {
                return m.getText();
            } catch (Exception e) {
                return m.toString();
            }
        }
        
        private String determineType(Message m) {
            if (m instanceof SystemMessage) return "system";
            if (m instanceof AssistantMessage) return "assistant";
            if (m instanceof UserMessage) return "user";
            return "user"; // fallback
        }
        
        // Convert wrapper back to concrete Message type
        public Message toMessage() {
            if (content == null) content = "";
            return switch (messageType != null ? messageType.toLowerCase() : "user") {
                case "system" -> new SystemMessage(content);
                case "assistant" -> new AssistantMessage(content);
                default -> new UserMessage(content);
            };
        }
        
        // Getters and setters for Jackson
        public String getMessageType() {
            return messageType;
        }
        
        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}

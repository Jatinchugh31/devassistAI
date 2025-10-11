package com.devassist.repository;

import com.devassist.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RedisChatMemoryRepository {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    // Keep last N messages per convo
    private final int maxMessages = 1000;
    // TTL for conversation keys (if you want expiry)
    private final Duration ttl = Duration.ofDays(30);

    public RedisChatMemoryRepository(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    private String key(String conversationId) {
        return "devassist:conversation:" + conversationId;
    }

    public void append(String conversationId, String messageJson) {
        String k = key(conversationId);
        // RPUSH then trim to keep last maxMessages entries
        redis.opsForList().rightPush(k, messageJson);
        redis.opsForList().trim(k, -maxMessages, -1);
        redis.expire(k, ttl);
    }

    public List<String> readAll(String conversationId) {
        String k = key(conversationId);
        List<String> range = redis.opsForList().range(k, 0, -1);
        return range == null ? List.of() : range;
    }

    public void clear(String conversationId) {
        redis.delete(key(conversationId));
    }
}

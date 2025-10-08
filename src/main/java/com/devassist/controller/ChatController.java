package com.devassist.controller;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final OpenAiChatModel chatModel;

    @Autowired
    public ChatController(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/generate")
    public Map<String, String> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        // Simple sync call - the Spring AI model will handle request/response
        String response = this.chatModel.call(message);
        return Map.of("generation", response);
    }

    @GetMapping("/debug/env")
    public Map<String, Object> debugEnv() {
        Map<String, Object> out = new HashMap<>();

        String openai = System.getenv("OPENAI_API_KEY");
        String springOpenai = System.getenv("SPRING_AI_OPENAI_API_KEY");
        String springProp = System.getProperty("spring.ai.openai.api-key");

        out.put("OPENAI_API_KEY_present", openai != null && openai.startsWith("sk-"));
        out.put("SPRING_AI_OPENAI_API_KEY_present", springOpenai != null && springOpenai.startsWith("sk-"));
        out.put("springProperty_set", springProp != null && !springProp.isBlank());

        // Masked previews (safe)
        out.put("OPENAI_API_KEY_preview", openai != null ? mask(openai) : null);
        out.put("SPRING_AI_OPENAI_API_KEY_preview", springOpenai != null ? mask(springOpenai) : null);
        out.put("springProperty_preview", springProp != null ? mask(springProp) : null);

        return out;
    }
    private String mask(String s) {
        if (s == null) return null;
        int len = s.length();
        if (len <= 8) return "****";
        return s.substring(0, 6) + "..." + s.substring(len - 4);
    }
}

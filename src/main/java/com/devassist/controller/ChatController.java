package com.devassist.controller;

import com.devassist.constant.RoleType;
import com.devassist.model.AiResponse;
import com.devassist.service.PromptBuilderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Log4j2
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private PromptBuilderService promptBuilderService;

    @GetMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "GENERAL") RoleType role, @RequestParam(required = false,defaultValue = "false") boolean rawResponse) {

        Prompt prompt = promptBuilderService.buildPrompt(role, message);
        log.info("Prompt {}", prompt);
        if(rawResponse) {
            return ResponseEntity.ok(chatClient.prompt()
                    .messages(prompt.getInstructions())
                    .call().content());
        }else {
            return ResponseEntity.ok(chatClient.prompt()
                    .messages(prompt.getInstructions())
                    .call()
                    .entity(AiResponse.class));
        }

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

        out.put("OPENAI_API_KEY_preview", mask(openai));
        out.put("SPRING_AI_OPENAI_API_KEY_preview", mask(springOpenai));
        out.put("springProperty_preview", mask(springProp));

        return out;
    }

    private String mask(String s) {
        if (s == null) return null;
        int len = s.length();
        if (len <= 8) return "****";
        return s.substring(0, 6) + "..." + s.substring(len - 4);
    }
}

package com.devassist.service;

import com.devassist.constant.RoleType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.devassist.constant.RoleType.JAVA;

@Service
public class PromptBuilderService {

    public Prompt buildPrompt(RoleType role, String message) {
        String systemInstruction = switch (role) {
            case JAVA -> "You are a highly skilled Java backend engineer. Explain code, frameworks, and performance optimizations clearly.";
            case SQL -> "You are a database expert. Help with SQL queries, optimization, and data modeling.";
            case CLOUD -> "You are a Cloud Architect. Guide users about AWS, microservices, and scalability patterns.";
            case DEVOPS -> "You are a DevOps engineer helping with CI/CD, Kubernetes, and infrastructure automation.";
            default -> "You are DevAssist — a general AI assistant for developers.";
        };

        return new Prompt(List.of(
                new SystemMessage(systemInstruction),
                new UserMessage(message)
        ));
    }
}

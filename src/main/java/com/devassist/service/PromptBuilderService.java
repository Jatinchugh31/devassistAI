package com.devassist.service;

import com.devassist.constant.RoleType;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildSystemInstruction(RoleType role) {
        if (role == null) {
            return "You are DevAssist, a helpful developer assistant.";
        }

        return switch (role) {
            case JAVA -> "You are JavaAgent: a senior Java backend engineer. Explain concisely, include code examples when helpful.";
            case SQL -> "You are SqlAgent: an expert SQL performance engineer. Focus on safety and optimization. Use READ-ONLY SQL when executing.";
            case CLOUD -> "You are CloudAgent: a cloud architect and DevOps engineer. Provide safe infrastructure advice.";
            default -> "You are DevAssist, a helpful developer assistant.";
        };
    }
}

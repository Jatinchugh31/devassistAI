package com.devassist.model;

import com.devassist.constant.RoleType;
import lombok.Data;

@Data
public class AgentRequest {

    private RoleType role;
    private String task;     // user message
    private String code;     // optional inline code
    private String file;     // optional file path in repo
    private String sessionId; // conversationId
    private String responseType; // optional: RAW/STRUCTURED

}

package com.devassist.model;

import lombok.Data;

import java.util.Map;

@Data
public class AiResponse {
    private String role;
    private String summary;
    private String explanation;
    private String deepDive;
    private String codeExample;
    private Map<String, Object> meta;

}

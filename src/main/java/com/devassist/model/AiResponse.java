package com.devassist.model;

import lombok.Data;

@Data
public class AiResponse {
    private String role;
    private String message;
    private  String definition;
    private String detailExplanation;
    private  String codeExample;
    private String nextStep;

}

package com.devassist.advisor;

import com.devassist.service.LLMFilterAdvisorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Log4j2
@RequiredArgsConstructor
public class FilterGenerationAdvisor implements CallAdvisor {

    public static final String FILTER_CONTEXT_KEY = "llm_filter_expression";

    private final LLMFilterAdvisorService llmFilterAdvisorService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(FORMATTER);

        String filter = llmFilterAdvisorService.generateFilterExpression(chatClientRequest.prompt().getUserMessage().getText());
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        log.info("filter extracted: " + filter);
        long duration = System.currentTimeMillis() - startTime;

        return chatClientResponse;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }


    @Override
    public int getOrder() {
        return -1;
    }
}

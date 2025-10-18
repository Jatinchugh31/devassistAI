package com.devassist.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HttpTool: Make HTTP requests to external APIs
 * 
 * Demonstrates:
 * - Multiple HTTP methods (GET, POST)
 * - Request/Response handling
 * - Error handling
 * - AI can call external APIs through this tool
 */
@Component
@Log4j2
public class HttpTool {

    private final RestTemplate restTemplate;
    private final int timeoutMs = 10000; // 10 seconds

    public HttpTool() {
        this.restTemplate = new RestTemplate();
        log.info("HttpTool initialized with timeout: {}ms", timeoutMs);
    }

    // ==================== TOOL 1: HTTP GET ====================
    
    public record HttpGetRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The URL to send GET request to")
        String url,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("Optional headers as key-value pairs")
        Map<String, String> headers
    ) {}

    public record HttpResponse(
        int statusCode,
        String statusText,
        String body,
        Map<String, String> responseHeaders,
        long responseTimeMs,
        String message
    ) {}

    @Tool(
        name = "http_get",
        description = "Make an HTTP GET request to an external API. " +
                     "Provide the URL and optional headers. " +
                     "Returns status code, response body, headers, and response time. " +
                     "Useful for fetching data from REST APIs, checking service health, or retrieving external information."
    )
    public HttpResponse httpGet(HttpGetRequest request) {
        log.info("http_get tool called with url: {}", request.url());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Build headers
            HttpHeaders headers = new HttpHeaders();
            if (request.headers() != null) {
                request.headers().forEach(headers::add);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Make request
            ResponseEntity<String> response = restTemplate.exchange(
                request.url(),
                HttpMethod.GET,
                entity,
                String.class
            );
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Extract response headers
            Map<String, String> responseHeaders = response.getHeaders().toSingleValueMap();
            
            log.info("GET {} - Status: {}, Time: {}ms", 
                     request.url(), response.getStatusCode(), responseTime);
            
            return new HttpResponse(
                response.getStatusCode().value(),
                response.getStatusCode().toString(),
                response.getBody(),
                responseHeaders,
                responseTime,
                "Request successful"
            );
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Error making GET request to {}: {}", request.url(), e.getMessage());
            
            return new HttpResponse(
                0,
                "ERROR",
                null,
                Map.of(),
                responseTime,
                "Error: " + e.getMessage()
            );
        }
    }

    // ==================== TOOL 2: HTTP POST ====================
    
    public record HttpPostRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The URL to send POST request to")
        String url,
        
        @JsonProperty(required = true)
        @JsonPropertyDescription("Request body as JSON string")
        String body,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("Optional headers as key-value pairs")
        Map<String, String> headers
    ) {}

    @Tool(
        name = "http_post",
        description = "Make an HTTP POST request to an external API. " +
                     "Provide the URL, request body (as JSON string), and optional headers. " +
                     "Returns status code, response body, headers, and response time. " +
                     "Useful for submitting data to APIs, triggering webhooks, or creating resources."
    )
    public HttpResponse httpPost(HttpPostRequest request) {
        log.info("http_post tool called with url: {}", request.url());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (request.headers() != null) {
                request.headers().forEach(headers::add);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(request.body(), headers);
            
            // Make request
            ResponseEntity<String> response = restTemplate.exchange(
                request.url(),
                HttpMethod.POST,
                entity,
                String.class
            );
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Extract response headers
            Map<String, String> responseHeaders = response.getHeaders().toSingleValueMap();
            
            log.info("POST {} - Status: {}, Time: {}ms", 
                     request.url(), response.getStatusCode(), responseTime);
            
            return new HttpResponse(
                response.getStatusCode().value(),
                response.getStatusCode().toString(),
                response.getBody(),
                responseHeaders,
                responseTime,
                "Request successful"
            );
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Error making POST request to {}: {}", request.url(), e.getMessage());
            
            return new HttpResponse(
                0,
                "ERROR",
                null,
                Map.of(),
                responseTime,
                "Error: " + e.getMessage()
            );
        }
    }

    // ==================== TOOL 3: CHECK URL ====================
    
    public record CheckUrlRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The URL to check")
        String url
    ) {}

    public record UrlStatus(
        String url,
        boolean reachable,
        int statusCode,
        long responseTimeMs,
        String message
    ) {}

    @Tool(
        name = "check_url",
        description = "Check if a URL is reachable and get its HTTP status. " +
                     "Provide the URL to check. " +
                     "Returns reachability status, HTTP status code, and response time. " +
                     "Useful for health checks, monitoring, or validating URLs."
    )
    public UrlStatus checkUrl(CheckUrlRequest request) {
        log.info("check_url tool called with url: {}", request.url());
        
        long startTime = System.currentTimeMillis();
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                request.url(),
                HttpMethod.HEAD,  // HEAD is lighter than GET
                null,
                String.class
            );
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.info("URL {} is reachable - Status: {}, Time: {}ms", 
                     request.url(), response.getStatusCode(), responseTime);
            
            return new UrlStatus(
                request.url(),
                true,
                response.getStatusCode().value(),
                responseTime,
                "URL is reachable"
            );
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("URL {} is not reachable: {}", request.url(), e.getMessage());
            
            return new UrlStatus(
                request.url(),
                false,
                0,
                responseTime,
                "URL not reachable: " + e.getMessage()
            );
        }
    }
}


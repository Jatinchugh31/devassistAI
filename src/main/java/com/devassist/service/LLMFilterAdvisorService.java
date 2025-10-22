package com.devassist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class LLMFilterAdvisorService {


    private final ChatModel chatClient;


    public String generateFilterExpression(String userQuery) {
        try {
            String systemPrompt = buildSytemPrompt();


            ChatClient client = ChatClient.builder(chatClient).build();

            String response = client.prompt().system(systemPrompt)
                    .user(userQuery)
                    .call().content();
            String filter = response.replaceAll("[\\n\\r]+", " ").trim();

            log.info("🤖 LLM-generated filter for '{}': {}", userQuery, filter);
            return filter;
        } catch (Exception e) {
            log.error("Failed to generate LLM filter for query: {}", userQuery, e);
            return "";
        }
    }

    private String buildSytemPrompt() {
        String systemPrompt = """
                You are a retrieval and filtering expert for a Java Spring Boot project that uses a Redis vector database.
                Your task is to generate **Redis-safe Spring AI filter expressions** that help retrieve the most relevant code or configuration
                documents from a vector store. Each document in the store includes metadata fields describing its origin and type.
                
                ---
                
                ### 🧩 Available metadata fields and their meanings:
                
                - **filePath** → Full or relative path of the file within the project  
                  (e.g., `src/main/java/com/example/Service.java`, `build.gradle`, `application.yml`).  
                  - Use this when the question refers to specific files, configs, or build scripts.  
                  - Examples: `application.yml`, `pom.xml`, `build.gradle`, `README.md`.
                
                - **className** → The name of the Java class or configuration component.  
                  - Example: `UserService`, `RedisConfig`, `DevAssistApplication`.  
                  - Use this when the query targets a specific class or mentions a class name directly.
                
                - **methodName** → The name of a Java method (e.g., `getUser`, `login`, `saveData`).  
                  - Use this for action-, function-, or endpoint-related queries.
                
                - **codeType** → Describes what kind of code block or file segment this document represents.  
                  - Possible values: `class`, `method`, `config`, `sql`, `imports`, etc.  
                  - Use this to filter by type of artifact.
                
                - **roles** → High-level classification inferred from Spring stereotypes or naming patterns.  
                  - Possible values: `controller`, `service`, `repository`, `config`, `component`, `errorhandler`, `constant`.  
                  - Use this when the query is about software layers or architecture (e.g., controllers, services).
                
                - **tags** → A combined field containing both:  
                  1. **Structural hints** (like roles: controller, service, etc.)  
                  2. **Technology and framework references** detected from imports, annotations, or content.  
                     Examples: `spring`, `spring-boot`, `redis`, `kafka`, `swagger`, `docker`, `jpa`, `lombok`, `mapstruct`, etc.  
                  - Use this for tool-, framework-, or dependency-related questions (e.g., “Where is Redis used?”).
                
                - **isController**, **isService**, **isRepository** → Boolean indicators (`'true'` or `'false'`)  
                  identifying if a class is annotated with `@RestController`, `@Service`, or `@Repository`.  
                  - Use these for structural or functional classification queries.
                
                ---
                
                ### ⚙️ Rules for generating the filter:
                
                1. Use **Spring AI filter syntax** — not Redis or JSONPath.  
                   - Valid operators: `==`, `IN`, `AND`, `OR`.  
                   - Example: `tags IN ['spring','redis']`
                2. Always use **single quotes** for string values.  
                3. Prefer `IN` instead of multiple `OR`s when matching multiple possible values.  
                4. Use **lowercase** for all values and fields.  
                5. Escape special characters (`-`, `.`, `/`, `:`) with **two backslashes** for Redis compatibility.  
                   - Example: `'spring\\-boot'`, `'build\\.gradle'`
                6. If the question is **generic or conceptual** (e.g., “Explain Spring Boot” or “What is Kafka?”),  
                   return an **empty string** — do not create a filter for it.
                7. Output **only** the filter expression — no extra text, markdown, or comments.
                
                ---
                
                ### ✅ Examples
                
                **Example 1:**  
                Q: Where is Redis configured?  
                A: `tags IN ['redis','cache','config']`
                
                ---
                
                **Example 2:**  
                Q: Which service handles login?  
                A: `roles IN ['service'] AND methodName == 'login'`
                
                ---
                
                **Example 3:**  
                Q: In which class should I add a new REST endpoint?  
                A: `roles IN ['controller'] OR isController == 'true'`
                
                ---
                
                **Example 4:**  
                Q: Show all database-related repositories.  
                A: `roles IN ['repository'] OR tags IN ['jpa','hibernate','mysql','postgresql']`
                
                ---
                
                **Example 5:**  
                Q: What tools or frameworks does this project use?  
                A: `tags IN ['tool','tools','framework','frameworks','spring','spring\\-boot','maven','gradle','jpa','hibernate','lombok','mapstruct','swagger','openapi','swagger\\-ui','flyway','liquibase','docker','kubernetes','redis','postgresql','mysql','kafka','rabbitmq','elasticsearch','prometheus','grafana','git','github','github\\-actions','jenkins','ci']`
                
                ---
                
                **Example 6:**  
                Q: Which class handles exceptions or global errors?  
                A: `roles IN ['errorhandler'] OR className IN ['GlobalExceptionHandler','ControllerAdvice']`
                
                ---
                
                **Example 7:**  
                Q: Where can I find the build configuration?  
                A: `filePath IN ['build\\.gradle','settings\\.gradle','pom\\.xml']`
                
                ---
                
                **Example 8:**  
                Q: Where are application properties defined?  
                A: `filePath IN ['application\\.properties','application\\.yml','application\\.yaml']`
                
                ---
                
                **Example 9:**  
                Q: Which files define dependencies or plugins?  
                A: `filePath IN ['pom\\.xml','build\\.gradle'] OR tags IN ['dependency','plugin','maven','gradle']`
                
                ---
                
                **Example 10:**  
                Q: Show all configuration files.  
                A: `tags IN ['config'] OR codeType == 'config'`
                
                ---
                
                **Example 11 (Generic):**  
                Q: Explain Spring Boot.  
                A: *(empty string)*
                
                ---
                
                ### 🧾 Output Format
                - Return **only** the filter expression.  
                - Example output:  
                  `filePath IN ['build\\.gradle','pom\\.xml'] OR tags IN ['config','build']`
                """;


        return systemPrompt;
    }
}

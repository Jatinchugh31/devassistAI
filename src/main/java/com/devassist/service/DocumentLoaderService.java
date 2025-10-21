package com.devassist.service;

import com.devassist.model.CodeDocument;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * DocumentLoaderService - Loads, parses, and structures project files for RAG.
 * Uses JavaParser to extract accurate metadata (classes, methods, annotations, roles, tech tags).
 */
@Service
@Log4j2
public class DocumentLoaderService {

    private final String projectRoot = System.getProperty("user.dir");
    private final JavaParser javaParser;

    public DocumentLoaderService() {
        ParserConfiguration config = new ParserConfiguration();
        this.javaParser = new JavaParser(config);
    }

    /**
     * Entry point: loads all relevant project files.
     */
    public List<CodeDocument> loadAllProjectFiles() {
        log.info("🚀 Starting to load all project files from: {}", projectRoot);

        List<CodeDocument> documents = new ArrayList<>();
        try {
            documents.addAll(loadJavaFiles());
            documents.addAll(loadConfigFiles());
            documents.addAll(loadBuildFiles());
            documents.addAll(loadDocumentationFiles());
            documents.addAll(loadSqlFiles());

            log.info("✅ Loaded {} code chunks from {} project files",
                    documents.size(),
                    documents.stream().map(CodeDocument::getFilePath).distinct().count());
        } catch (Exception e) {
            log.error("❌ Failed to scan project files", e);
        }

        return documents;
    }

    /* ===================== JAVA FILES ===================== */

    private List<CodeDocument> loadJavaFiles() {
        List<CodeDocument> documents = new ArrayList<>();
        try {
            Path javaDir = Paths.get(projectRoot, "src", "main", "java");
            if (!Files.exists(javaDir)) {
                log.warn("⚠️ Java source directory not found: {}", javaDir);
                return documents;
            }

            Files.walk(javaDir)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(javaFile -> {
                        try {
                            List<CodeDocument> parsed = parseJavaFile(javaFile);
                            documents.addAll(parsed);
                            log.debug("📄 Loaded {} chunks from {}", parsed.size(), javaFile.getFileName());
                        } catch (Exception e) {
                            log.error("❌ Failed to parse {}", javaFile, e);
                        }
                    });

        } catch (IOException e) {
            log.error("❌ Failed to walk Java files", e);
        }

        return documents;
    }

    /**
     * Parse Java file using JavaParser (AST-based).
     */
    private List<CodeDocument> parseJavaFile(Path filePath) throws IOException {
        String source = Files.readString(filePath);
        String relativePath = getRelativePath(filePath);
        var parseResult = javaParser.parse(source);
        Optional<CompilationUnit> maybeCu = parseResult.getResult();
        if (maybeCu.isEmpty()) return Collections.emptyList();
        CompilationUnit cu = maybeCu.get();

        List<CodeDocument> results = new ArrayList<>();

        // imports chunk
        List<ImportDeclaration> imports = cu.getImports();
        List<String> importNames = imports.stream().map(i -> i.getNameAsString().toLowerCase()).toList();

        if (!imports.isEmpty()) {
            String importsBlock = imports.stream().map(Object::toString).collect(Collectors.joining("\n"));
            results.add(CodeDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .filePath(relativePath)
                    .className("imports")
                    .content(importsBlock)
                    .codeType("imports")
                    .tags(List.of("imports"))
                    .roles(List.of("imports"))
                    .lineRange("1-1")
                    .createdAt(LocalDateTime.now())
                    .contentSize(importsBlock.length())
                    .build());
        }

        // classes/interfaces/enums
        for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            String className = clazz.getNameAsString();
            int start = clazz.getBegin().map(p -> p.line).orElse(1);
            int end = clazz.getEnd().map(p -> p.line).orElse(start);
            String classContent = clazz.toString();
            List<String> annotations = extractAnnotations(clazz);

            // infer roles and technology tags
            Set<String> roles = inferRoles(className, annotations, classContent);
            Set<String> techTags = inferTags(classContent, className, importNames);

            // merge them into combined tags
            Set<String> combinedTags = new HashSet<>();
            combinedTags.addAll(roles);
            combinedTags.addAll(techTags);

            log.debug("🧩 Class [{}] roles={} techTags={}", className, roles, techTags);

            // create class-level doc
            results.add(CodeDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .filePath(relativePath)
                    .className(className)
                    .content(classContent)
                    .lineRange(start + "-" + end)
                    .codeType("class")
                    .roles(new ArrayList<>(roles))
                    .tags(new ArrayList<>(combinedTags))
                    .createdAt(LocalDateTime.now())
                    .contentSize(classContent.length())
                    .build());

            // method-level docs
            for (MethodDeclaration method : clazz.findAll(MethodDeclaration.class)) {
                String methodName = method.getNameAsString();
                int mStart = method.getBegin().map(p -> p.line).orElse(start);
                int mEnd = method.getEnd().map(p -> p.line).orElse(mStart);
                String methodContent = method.toString();
                List<String> methodAnnotations = extractAnnotations(method);

                Set<String> methodRoles = new HashSet<>(roles);
                if (methodAnnotations.stream().anyMatch(a ->
                        a.contains("GetMapping") || a.contains("PostMapping") || a.contains("PutMapping") ||
                                a.contains("DeleteMapping") || a.contains("RequestMapping")))
                    methodRoles.add("endpoint");
                if (methodContent.contains("@RequestBody")) methodRoles.add("request");
                if (methodContent.contains("ResponseEntity") || methodContent.contains("HttpStatus"))
                    methodRoles.add("response");

                // merge tech tags too
                Set<String> methodTags = new HashSet<>(techTags);
                methodTags.addAll(methodRoles);

                results.add(CodeDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .filePath(relativePath)
                        .className(className)
                        .methodName(methodName)
                        .content(methodContent)
                        .lineRange(mStart + "-" + mEnd)
                        .codeType("method")
                        .roles(new ArrayList<>(methodRoles))
                        .tags(new ArrayList<>(methodTags))
                        .createdAt(LocalDateTime.now())
                        .contentSize(methodContent.length())
                        .build());
            }
        }

        return results;
    }

    private List<String> extractAnnotations(NodeWithAnnotations<?> node) {
        return node.getAnnotations().stream()
                .map(AnnotationExpr::getNameAsString)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /** Infer Spring roles (controller, service, repository, etc.) */
    private Set<String> inferRoles(String className, List<String> annotations, String content) {
        Set<String> roles = new HashSet<>();
        Set<String> normalized = annotations.stream()
                .map(s -> s.replaceAll("^@?", "").toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        for (String ann : normalized) {
            if (ann.contains("restcontroller") || ann.contains("controller")) roles.add("controller");
            else if (ann.contains("service")) roles.add("service");
            else if (ann.contains("repository")) roles.add("repository");
            else if (ann.contains("component")) roles.add("component");
            else if (ann.contains("configuration")) roles.add("config");
            else if (ann.contains("controlleradvice")) roles.add("errorhandler");
        }

        String lower = className.toLowerCase();
        if (lower.contains("controller")) roles.add("controller");
        if (lower.contains("service")) roles.add("service");
        if (lower.contains("repository") || lower.contains("repo")) roles.add("repository");
        if (lower.contains("config")) roles.add("config");
        if (lower.contains("constant")) roles.add("constant");
        if (content.contains("@Slf4j")) roles.add("logging");
        return roles;
    }

    /** Infer technologies from imports and content (redis, kafka, etc.) */
    private Set<String> inferTags(String content, String className, List<String> imports) {
        Set<String> tags = new HashSet<>();
        String allText = (content + " " + className + " " + String.join(" ", imports)).toLowerCase();

        Map<String, List<String>> techKeywords = Map.ofEntries(
                Map.entry("spring", List.of("springframework", "springboot")),
                Map.entry("spring-boot", List.of("springboot")),
                Map.entry("redis", List.of("redis", "lettuce", "jedis")),
                Map.entry("jpa", List.of("jakarta.persistence", "javax.persistence", "jpa")),
                Map.entry("hibernate", List.of("hibernate")),
                Map.entry("kafka", List.of("kafka")),
                Map.entry("rabbitmq", List.of("amqp", "rabbit")),
                Map.entry("swagger", List.of("swagger", "openapi")),
                Map.entry("mapstruct", List.of("mapstruct")),
                Map.entry("lombok", List.of("lombok")),
                Map.entry("postgresql", List.of("postgres", "pgjdbc")),
                Map.entry("mysql", List.of("mysql")),
                Map.entry("liquibase", List.of("liquibase")),
                Map.entry("flyway", List.of("flyway")),
                Map.entry("docker", List.of("docker")),
                Map.entry("kubernetes", List.of("kubernetes", "k8s")),
                Map.entry("elasticsearch", List.of("elasticsearch")),
                Map.entry("prometheus", List.of("prometheus")),
                Map.entry("grafana", List.of("grafana")),
                Map.entry("jenkins", List.of("jenkins")),
                Map.entry("github", List.of("github")),
                Map.entry("ci", List.of("pipeline", "ci"))
        );

        techKeywords.forEach((tag, patterns) -> {
            if (patterns.stream().anyMatch(allText::contains)) {
                tags.add(tag);
            }
        });

        return tags;
    }

    private List<CodeDocument> loadConfigFiles() {
        return loadSimpleFiles("src/main/resources", List.of(".properties", ".yml", ".yaml"));
    }

    private List<CodeDocument> loadBuildFiles() {
        return loadSimpleFiles("", List.of("pom.xml", "build.gradle", "settings.gradle"));
    }

    private List<CodeDocument> loadDocumentationFiles() {
        return loadSimpleFiles("", List.of(".md", "README"));
    }

    private List<CodeDocument> loadSqlFiles() {
        return loadSimpleFiles("", List.of(".sql"));
    }

    private List<CodeDocument> loadSimpleFiles(String base, List<String> extensions) {
        List<CodeDocument> docs = new ArrayList<>();
        try {
            Path basePath = base.isEmpty() ? Paths.get(projectRoot) : Paths.get(projectRoot, base);
            if (!Files.exists(basePath)) return docs;

            Files.walk(basePath)
                    .filter(path -> extensions.stream().anyMatch(ext -> path.toString().endsWith(ext)
                            || path.getFileName().toString().equalsIgnoreCase(ext)))
                    .forEach(file -> {
                        try {
                            String content = Files.readString(file);
                            docs.add(CodeDocument.builder()
                                    .id(UUID.randomUUID().toString())
                                    .filePath(getRelativePath(file))
                                    .className(file.getFileName().toString())
                                    .content(content)
                                    .lineRange("1-" + content.lines().count())
                                    .codeType("config")
                                    .roles(List.of("config"))
                                    .tags(List.of("config"))
                                    .createdAt(LocalDateTime.now())
                                    .contentSize(content.length())
                                    .build());
                        } catch (IOException e) {
                            log.error("❌ Failed to read {}", file, e);
                        }
                    });
        } catch (IOException e) {
            log.error("❌ Failed to scan {}", base, e);
        }
        return docs;
    }

    private String getRelativePath(Path filePath) {
        return projectRoot.equals(filePath.getParent().toString())
                ? filePath.getFileName().toString()
                : filePath.toString().replace(projectRoot, "");
    }

    private String extractClassName(String content) {
        Pattern classPattern = Pattern.compile("(?:class|interface|enum)\\s+(\\w+)");
        var matcher = classPattern.matcher(content);
        return matcher.find() ? matcher.group(1) : "Unknown";
    }
}

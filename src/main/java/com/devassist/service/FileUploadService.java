package com.devassist.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * FileUploadService - Handle file upload operations
 * 
 * Features:
 * - Save uploaded files temporarily
 * - Read file content
 * - Clean up temporary files
 * - Validate file types and sizes
 */
@Service
@Log4j2
public class FileUploadService {

    private final String tempDirectory;
    private final long maxFileSize = 10 * 1024 * 1024; // 10MB limit

    public FileUploadService() {
        // Create temp directory in system temp folder
        this.tempDirectory = System.getProperty("java.io.tmpdir") + "/devassist-uploads";
        createTempDirectory();
    }

    /**
     * Create temporary directory if it doesn't exist
     */
    private void createTempDirectory() {
        try {
            Path path = Paths.get(tempDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created temporary upload directory: {}", tempDirectory);
            }
        } catch (IOException e) {
            log.error("Failed to create temp directory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create temp directory", e);
        }
    }

    /**
     * Save uploaded file temporarily
     * 
     * @param file The uploaded file
     * @return Path to the saved file
     * @throws IOException if file cannot be saved
     */
    public String saveTemporaryFile(MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path targetPath = Paths.get(tempDirectory, uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Saved file: {} -> {}", originalFilename, targetPath);
        
        return targetPath.toString();
    }

    /**
     * Read file content as string
     * 
     * @param filePath Path to the file
     * @return File content as string
     * @throws IOException if file cannot be read
     */
    public String readFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        // Check if file is text-based
        String mimeType = Files.probeContentType(path);
        if (mimeType != null && !mimeType.startsWith("text/") && !isTextFile(filePath)) {
            return "[Binary file - cannot display content. File type: " + mimeType + "]";
        }
        
        // Read file content
        byte[] bytes = Files.readAllBytes(path);
        String content = new String(bytes);
        
        log.info("Read file content: {} ({} characters)", filePath, content.length());
        
        return content;
    }

    /**
     * Delete temporary file
     * 
     * @param filePath Path to the file
     */
    public void deleteTemporaryFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Deleted temporary file: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete temporary file: {}", filePath, e);
        }
    }

    /**
     * Validate uploaded file
     * 
     * @param file The uploaded file
     * @throws IllegalArgumentException if file is invalid
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                String.format("File too large: %d bytes (max: %d bytes)", 
                    file.getSize(), maxFileSize)
            );
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Invalid filename");
        }
        
        // Check for dangerous file extensions
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".exe") || 
            lowerFilename.endsWith(".dll") || 
            lowerFilename.endsWith(".bat") ||
            lowerFilename.endsWith(".sh") ||
            lowerFilename.endsWith(".cmd")) {
            throw new IllegalArgumentException("File type not allowed: " + filename);
        }
    }

    /**
     * Check if file is likely a text file based on extension
     */
    private boolean isTextFile(String filePath) {
        String lower = filePath.toLowerCase();
        return lower.endsWith(".txt") ||
               lower.endsWith(".log") ||
               lower.endsWith(".java") ||
               lower.endsWith(".properties") ||
               lower.endsWith(".xml") ||
               lower.endsWith(".json") ||
               lower.endsWith(".yml") ||
               lower.endsWith(".yaml") ||
               lower.endsWith(".md") ||
               lower.endsWith(".csv") ||
               lower.endsWith(".sql") ||
               lower.endsWith(".html") ||
               lower.endsWith(".css") ||
               lower.endsWith(".js") ||
               lower.endsWith(".ts");
    }

    /**
     * Clean up old temporary files (older than 1 hour)
     */
    public void cleanupOldFiles() {
        try {
            File tempDir = new File(tempDirectory);
            if (!tempDir.exists()) {
                return;
            }
            
            long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
            File[] files = tempDir.listFiles();
            
            if (files != null) {
                int deletedCount = 0;
                for (File file : files) {
                    if (file.lastModified() < oneHourAgo) {
                        if (file.delete()) {
                            deletedCount++;
                        }
                    }
                }
                if (deletedCount > 0) {
                    log.info("Cleaned up {} old temporary files", deletedCount);
                }
            }
        } catch (Exception e) {
            log.warn("Error during cleanup: {}", e.getMessage());
        }
    }
}


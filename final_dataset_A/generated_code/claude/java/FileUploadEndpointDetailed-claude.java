package com.example.upload;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FileUploadEndpointDetailed {

    private static final String UPLOAD_DIR = "uploads/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "pdf", "txt", "csv");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private boolean isAllowedExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) return false;
        String ext = filename.substring(dotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, Object>> results = new ArrayList<>();

        Path uploadPath = Paths.get(UPLOAD_DIR);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Could not create upload directory");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        for (MultipartFile file : files) {
            Map<String, Object> result = new HashMap<>();
            String originalName = file.getOriginalFilename();

            if (originalName == null || originalName.isEmpty()) continue;

            if (!isAllowedExtension(originalName) || file.getSize() > MAX_FILE_SIZE) {
                result.put("filename", originalName);
                result.put("status", "rejected");
                results.add(result);
                continue;
            }

            String uniqueName = UUID.randomUUID().toString() + "_" + sanitizeFilename(originalName);
            File dest = new File(UPLOAD_DIR + uniqueName);

            try {
                file.transferTo(dest);
                result.put("original_filename", originalName);
                result.put("stored_filename", uniqueName);
                result.put("status", "uploaded");
                result.put("size_bytes", file.getSize());
            } catch (IOException e) {
                result.put("filename", originalName);
                result.put("status", "error");
            }
            results.add(result);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("uploaded", results);
        return ResponseEntity.ok(response);
    }
}

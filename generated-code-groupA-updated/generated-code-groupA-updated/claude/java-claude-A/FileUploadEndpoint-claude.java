package com.example.upload;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FileUploadEndpoint {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        File dest = new File(UPLOAD_DIR + file.getOriginalFilename());
        file.transferTo(dest);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("filename", file.getOriginalFilename());
        return response;
    }
}

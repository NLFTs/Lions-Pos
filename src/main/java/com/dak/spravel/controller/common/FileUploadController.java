package com.dak.spravel.controller.common;

import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.util.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/avatar")
    public ResponseEntity<ResData<Map<String, String>>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("[POST] /api/v1/upload/avatar - Uploading avatar...");
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            // Setup folder /uploads/avatar/
            String folderPath = uploadDir + "/avatar/";
            File dir = new File(folderPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // Save file
            Path path = Paths.get(folderPath + newFilename);
            Files.write(path, file.getBytes());

            String fileUrl = "/uploads/avatar/" + newFilename;
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);

            return ResponseBuilder.ok(result);

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    @PostMapping("/product")
    public ResponseEntity<ResData<Map<String, String>>> uploadProductPhoto(@RequestParam("file") MultipartFile file) {
        log.info("[POST] /api/v1/upload/product - Uploading product photo...");
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            // Setup folder /uploads/products/
            String folderPath = uploadDir + "/products/";
            File dir = new File(folderPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // Save file
            Path path = Paths.get(folderPath + newFilename);
            Files.write(path, file.getBytes());

            String fileUrl = "/uploads/products/" + newFilename;
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);

            return ResponseBuilder.ok(result);

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }
}


package com.imagecorrectness.image_analysis.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Collections;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    private static final String UPLOAD_DIR = "D:/dontopen/image correctness/imagapp/image-analysis/uploads/"; // Fixed
                                                                                                              // upload
                                                                                                              // path
    private static final String FLASK_API_URL = "http://localhost:5000/analyze";

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // Ensure the upload directory exists
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Save the file
            String filePath = UPLOAD_DIR + file.getOriginalFilename();
            File dest = new File(filePath);
            file.transferTo(dest);

            // Send file to Flask API
            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("file", new FileSystemResource(filePath));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(FLASK_API_URL, requestEntity, String.class);

            // Return Flask API response
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "File upload failed: " + e.getMessage()));
        }
    }
}

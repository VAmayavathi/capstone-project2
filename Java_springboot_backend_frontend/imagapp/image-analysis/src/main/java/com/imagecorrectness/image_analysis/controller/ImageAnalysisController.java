package com.imagecorrectness.image_analysis.controller;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageAnalysisController {

    private static final String FLASK_API_URL = "http://127.0.0.1:5000/predict";

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeImage(@RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "url", required = false) String imageUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();

            if (file != null && !file.isEmpty()) {
                // Send file
                requestBody.add("file", new HttpEntity<>(file.getResource(), headers));
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                // Send URL
                requestBody.add("url", imageUrl);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "No image file or URL provided."));
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    FLASK_API_URL, requestEntity, (Class<Map<String, Object>>) (Class<?>) Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error processing image: " + e.getMessage()));
        }
    }
}

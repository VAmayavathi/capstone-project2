package com.imagecorrectness.image_analysis.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;

@Service
public class ImageAnalysisService {
    private final String FLASK_API_URL = "http://127.0.0.1:5000/predict";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> analyzeImage(MultipartFile file) {
        try {
            // Check if file is empty
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Uploaded file is empty.");
            }

            // Prepare request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename(); // Prevents filename error
                }
            });

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Call Flask API
            ResponseEntity<Map> response = restTemplate.postForEntity(FLASK_API_URL, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Flask API returned an error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return Map.of("error", e.getMessage()); // Return error in JSON format
        }
    }
}

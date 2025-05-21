package com.imagecorrectness.image_analysis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // Prefix to avoid conflicts
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}

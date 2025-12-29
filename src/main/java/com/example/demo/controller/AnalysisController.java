package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ClassificationResponse;
import com.example.demo.service.NlpCloudService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AnalysisController {

    private final NlpCloudService service;

    public AnalysisController(NlpCloudService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/analyze")
    @ResponseBody
    public ApiResponse<ClassificationResponse> analyze(@RequestParam String text) {
        long start = System.currentTimeMillis();
        ClassificationResponse response = service.classify(text);
        return ApiResponse.fromPayload(response, start);
    }
}

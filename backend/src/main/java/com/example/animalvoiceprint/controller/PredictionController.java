package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.dto.PredictionRequest;
import com.example.animalvoiceprint.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prediction")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/single")
    @PreAuthorize("hasAnyRole('admin', 'algorithm', 'annotator')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> predict(@Valid @RequestBody PredictionRequest request) {
        Map<String, Object> result = predictionService.predict(request);
        return ResponseEntity.ok(ApiResponse.success("预测完成", result));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchPredict(@RequestBody List<PredictionRequest> requests) {
        Map<String, Object> result = predictionService.batchPredict(requests);
        return ResponseEntity.ok(ApiResponse.success("批量预测完成", result));
    }
}
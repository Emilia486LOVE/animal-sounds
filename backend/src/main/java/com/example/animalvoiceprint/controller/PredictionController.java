package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.dto.PredictionRequest;
import com.example.animalvoiceprint.service.PredictionService;
import com.example.animalvoiceprint.service.SmartPredictionService;
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
    private final SmartPredictionService smartPredictionService;

    public PredictionController(PredictionService predictionService, SmartPredictionService smartPredictionService) {
        this.predictionService = predictionService;
        this.smartPredictionService = smartPredictionService;
    }

    @PostMapping("/single")
    @PreAuthorize("hasAnyRole('admin', 'algorithm', 'annotator')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> predict(@Valid @RequestBody PredictionRequest request) {
        Map<String, Object> result = smartPredictionService.predict(request.getAudioId(), request.getTaskId());
        return ResponseEntity.ok(ApiResponse.success("预测完成", result));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchPredict(@RequestBody List<PredictionRequest> requests) {
        Map<String, Object> result = predictionService.batchPredict(requests);
        return ResponseEntity.ok(ApiResponse.success("批量预测完成", result));
    }

    @GetMapping("/data-quality")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDataQualityAnalysis() {
        Map<String, Object> result = smartPredictionService.analyzeDataQuality();
        return ResponseEntity.ok(ApiResponse.success("数据质量分析完成", result));
    }

    @GetMapping("/confusion-matrix")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfusionMatrix() {
        Map<String, Object> result = smartPredictionService.generateConfusionMatrix();
        return ResponseEntity.ok(ApiResponse.success("混淆矩阵生成完成", result));
    }
}
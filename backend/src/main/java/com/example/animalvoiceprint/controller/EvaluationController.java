package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.entity.EvaluationResult;
import com.example.animalvoiceprint.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
@PreAuthorize("hasAnyRole('admin', 'algorithm')")
public class EvaluationController {
    
    private final EvaluationService evaluationService;
    
    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }
    
    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<List<EvaluationResult>>> getEvaluationsByTaskId(@PathVariable("taskId") Integer taskId) {
        List<EvaluationResult> results = evaluationService.getEvaluationsByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluationResult>> getEvaluationById(@PathVariable("id") Integer evalId) {
        EvaluationResult result = evaluationService.getEvaluationById(evalId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/task/{taskId}/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaskEvaluationSummary(@PathVariable("taskId") Integer taskId) {
        Map<String, Object> summary = evaluationService.getTaskEvaluationSummary(taskId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
    
    @PostMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<List<EvaluationResult>>> saveEvaluations(
            @PathVariable("taskId") Integer taskId,
            @RequestBody Map<String, Map<String, Double>> metrics) {
        List<EvaluationResult> results = evaluationService.saveEvaluations(taskId, metrics);
        return ResponseEntity.ok(ApiResponse.success("评估结果已保存", results));
    }
    
    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvaluations(@PathVariable("taskId") Integer taskId) {
        evaluationService.deleteEvaluations(taskId);
        return ResponseEntity.ok(ApiResponse.success("评估结果已删除", null));
    }
}
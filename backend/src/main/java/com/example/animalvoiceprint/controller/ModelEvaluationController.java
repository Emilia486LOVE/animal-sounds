package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.entity.ModelEvaluation;
import com.example.animalvoiceprint.service.ModelEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/model-evaluation")
public class ModelEvaluationController {
    
    private final ModelEvaluationService evaluationService;
    
    public ModelEvaluationController(ModelEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ModelEvaluation>>> getAllEvaluations() {
        List<ModelEvaluation> evaluations = evaluationService.getAllEvaluations();
        return ResponseEntity.ok(ApiResponse.success(evaluations));
    }
    
    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<ModelEvaluation>> getEvaluationByTaskId(@PathVariable("taskId") Integer taskId) {
        return evaluationService.getEvaluationByTaskId(taskId)
                .map(evaluation -> ResponseEntity.ok(ApiResponse.success(evaluation)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Void>> deleteEvaluationByTaskId(@PathVariable("taskId") Integer taskId) {
        evaluationService.deleteEvaluationByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.success("评估记录已删除", null));
    }
}
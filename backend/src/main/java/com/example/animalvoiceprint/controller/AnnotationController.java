package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.dto.AnnotationCreateRequest;
import com.example.animalvoiceprint.dto.AnnotationReviewRequest;
import com.example.animalvoiceprint.entity.AnnotationRecord;
import com.example.animalvoiceprint.service.AnnotationService;
import com.example.animalvoiceprint.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annotations")
public class AnnotationController {
    
    private final AnnotationService annotationService;
    private final AuthService authService;
    
    public AnnotationController(AnnotationService annotationService, AuthService authService) {
        this.annotationService = annotationService;
        this.authService = authService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<AnnotationRecord>>> getAllAnnotations() {
        List<AnnotationRecord> annotations = annotationService.getAllAnnotations();
        return ResponseEntity.ok(ApiResponse.success(annotations));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnotationRecord>> getAnnotationById(@PathVariable("id") Integer annotationId) {
        AnnotationRecord annotation = annotationService.getAnnotationById(annotationId);
        return ResponseEntity.ok(ApiResponse.success(annotation));
    }
    
    @GetMapping("/audio/{audioId}")
    public ResponseEntity<ApiResponse<List<AnnotationRecord>>> getAnnotationsByAudioId(@PathVariable("audioId") Integer audioId) {
        List<AnnotationRecord> annotations = annotationService.getAnnotationsByAudioId(audioId);
        return ResponseEntity.ok(ApiResponse.success(annotations));
    }
    
    @GetMapping("/label/{labelId}")
    public ResponseEntity<ApiResponse<List<AnnotationRecord>>> getAnnotationsByLabelId(@PathVariable("labelId") Integer labelId) {
        List<AnnotationRecord> annotations = annotationService.getAnnotationsByLabelId(labelId);
        return ResponseEntity.ok(ApiResponse.success(annotations));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AnnotationRecord>>> getAnnotationsByStatus(@PathVariable("status") String status) {
        List<AnnotationRecord> annotations = annotationService.getAnnotationsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(annotations));
    }
    
    @GetMapping("/annotator/{annotatorId}")
    public ResponseEntity<ApiResponse<List<AnnotationRecord>>> getAnnotationsByAnnotatorId(@PathVariable("annotatorId") Integer annotatorId) {
        List<AnnotationRecord> annotations = annotationService.getAnnotationsByAnnotatorId(annotatorId);
        return ResponseEntity.ok(ApiResponse.success(annotations));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'annotator')")
    public ResponseEntity<ApiResponse<AnnotationRecord>> createAnnotation(@Valid @RequestBody AnnotationCreateRequest request) {
        Integer userId = authService.getCurrentUser().getUserId();
        AnnotationRecord annotation = annotationService.createAnnotation(request, userId);
        return ResponseEntity.ok(ApiResponse.success("标注创建成功", annotation));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'annotator')")
    public ResponseEntity<ApiResponse<AnnotationRecord>> updateAnnotation(
            @PathVariable("id") Integer annotationId,
            @Valid @RequestBody AnnotationCreateRequest request) {
        AnnotationRecord annotation = annotationService.updateAnnotation(annotationId, request);
        return ResponseEntity.ok(ApiResponse.success("标注更新成功", annotation));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'annotator')")
    public ResponseEntity<ApiResponse<Void>> deleteAnnotation(@PathVariable("id") Integer annotationId) {
        annotationService.deleteAnnotation(annotationId);
        return ResponseEntity.ok(ApiResponse.success("标注已删除", null));
    }
    
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('admin', 'annotator')")
    public ResponseEntity<ApiResponse<AnnotationRecord>> submitAnnotation(@PathVariable("id") Integer annotationId) {
        AnnotationRecord annotation = annotationService.submitAnnotation(annotationId);
        return ResponseEntity.ok(ApiResponse.success("标注已提交", annotation));
    }
    
    @PostMapping("/{id}/save-draft")
    @PreAuthorize("hasAnyRole('admin', 'annotator')")
    public ResponseEntity<ApiResponse<AnnotationRecord>> saveDraft(@PathVariable("id") Integer annotationId) {
        AnnotationRecord annotation = annotationService.saveDraft(annotationId);
        return ResponseEntity.ok(ApiResponse.success("标注已保存为草稿", annotation));
    }
    
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('admin')")
    public ResponseEntity<ApiResponse<AnnotationRecord>> reviewAnnotation(
            @PathVariable("id") Integer annotationId,
            @Valid @RequestBody AnnotationReviewRequest request) {
        Integer reviewerId = authService.getCurrentUser().getUserId();
        AnnotationRecord annotation = annotationService.reviewAnnotation(annotationId, request, reviewerId);
        return ResponseEntity.ok(ApiResponse.success("审核完成", annotation));
    }
    
    @GetMapping("/count/label/{labelId}")
    public ResponseEntity<ApiResponse<Long>> countByLabelId(@PathVariable("labelId") Integer labelId) {
        long count = annotationService.countByLabelId(labelId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @GetMapping("/count/status/{status}")
    public ResponseEntity<ApiResponse<Long>> countByStatus(@PathVariable("status") String status) {
        long count = annotationService.countByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
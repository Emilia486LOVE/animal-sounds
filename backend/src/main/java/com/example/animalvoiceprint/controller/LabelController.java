package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.dto.LabelCreateRequest;
import com.example.animalvoiceprint.entity.TaxonomyLabel;
import com.example.animalvoiceprint.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labels")
public class LabelController {
    
    private final LabelService labelService;
    
    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxonomyLabel>>> getAllLabels() {
        List<TaxonomyLabel> labels = labelService.getAllLabels();
        return ResponseEntity.ok(ApiResponse.success(labels));
    }
    
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLabelTree() {
        List<Map<String, Object>> tree = labelService.getLabelTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxonomyLabel>> getLabelById(@PathVariable("id") Integer labelId) {
        TaxonomyLabel label = labelService.getLabelById(labelId);
        return ResponseEntity.ok(ApiResponse.success(label));
    }
    
    
    
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<TaxonomyLabel>> createLabel(@Valid @RequestBody LabelCreateRequest request) {
        TaxonomyLabel label = labelService.createLabel(request);
        return ResponseEntity.ok(ApiResponse.success("标签创建成功", label));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<TaxonomyLabel>> updateLabel(@PathVariable("id") Integer labelId,
                                                                  @Valid @RequestBody LabelCreateRequest request) {
        TaxonomyLabel label = labelService.updateLabel(labelId, request);
        return ResponseEntity.ok(ApiResponse.success("标签更新成功", label));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Void>> deleteLabel(@PathVariable("id") Integer labelId) {
        labelService.deleteLabel(labelId);
        return ResponseEntity.ok(ApiResponse.success("标签删除成功", null));
    }
}
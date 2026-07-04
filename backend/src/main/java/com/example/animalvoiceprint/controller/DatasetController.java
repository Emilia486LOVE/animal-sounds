package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.dto.DatasetCreateRequest;
import com.example.animalvoiceprint.entity.Dataset;
import com.example.animalvoiceprint.service.AuthService;
import com.example.animalvoiceprint.service.DatasetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {
    
    private final DatasetService datasetService;
    private final AuthService authService;
    
    public DatasetController(DatasetService datasetService, AuthService authService) {
        this.datasetService = datasetService;
        this.authService = authService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Dataset>>> getAllDatasets() {
        List<Dataset> datasets = datasetService.getAllDatasets();
        return ResponseEntity.ok(ApiResponse.success(datasets));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Dataset>> getDatasetById(@PathVariable("id") Integer datasetId) {
        Dataset dataset = datasetService.getDatasetById(datasetId);
        return ResponseEntity.ok(ApiResponse.success(dataset));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Dataset>>> searchDatasets(@RequestParam("keyword") String keyword) {
        List<Dataset> datasets = datasetService.searchDatasets(keyword);
        return ResponseEntity.ok(ApiResponse.success(datasets));
    }
    
    
    
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Dataset>> createDataset(@Valid @RequestBody DatasetCreateRequest request) {
        Integer userId = authService.getCurrentUser().getUserId();
        Dataset dataset = datasetService.createDataset(request, userId);
        return ResponseEntity.ok(ApiResponse.success("数据集创建成功", dataset));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Dataset>> updateDataset(@PathVariable("id") Integer datasetId,
                                                              @Valid @RequestBody DatasetCreateRequest request) {
        Dataset dataset = datasetService.updateDataset(datasetId, request);
        return ResponseEntity.ok(ApiResponse.success("数据集更新成功", dataset));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'algorithm')")
    public ResponseEntity<ApiResponse<Void>> deleteDataset(@PathVariable("id") Integer datasetId) {
        datasetService.deleteDataset(datasetId);
        return ResponseEntity.ok(ApiResponse.success("数据集删除成功", null));
    }
    
    @PostMapping("/{id}/refresh-count")
    public ResponseEntity<ApiResponse<Dataset>> refreshAudioCount(@PathVariable("id") Integer datasetId) {
        Dataset dataset = datasetService.refreshAudioCount(datasetId);
        return ResponseEntity.ok(ApiResponse.success("音频数量已更新", dataset));
    }
}
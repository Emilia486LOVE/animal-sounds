package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.repository.AudioFileRepository;
import com.example.animalvoiceprint.repository.AnnotationRecordRepository;
import com.example.animalvoiceprint.repository.DatasetRepository;
import com.example.animalvoiceprint.repository.TaxonomyLabelRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    
    private final DatasetRepository datasetRepository;
    private final AudioFileRepository audioFileRepository;
    private final AnnotationRecordRepository annotationRepository;
    private final TaxonomyLabelRepository labelRepository;
    
    public StatisticsController(DatasetRepository datasetRepository, AudioFileRepository audioFileRepository,
                                AnnotationRecordRepository annotationRepository, TaxonomyLabelRepository labelRepository) {
        this.datasetRepository = datasetRepository;
        this.audioFileRepository = audioFileRepository;
        this.annotationRepository = annotationRepository;
        this.labelRepository = labelRepository;
    }
    
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("datasetCount", datasetRepository.count());
        overview.put("audioCount", audioFileRepository.count());
        overview.put("annotationCount", annotationRepository.count());
        overview.put("labelCount", labelRepository.count());
        return ResponseEntity.ok(ApiResponse.success(overview));
    }
    
    @GetMapping("/dataset")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDatasetStatistics() {
        List<Map<String, Object>> stats = datasetRepository.findAll().stream()
                .map(dataset -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("datasetId", dataset.getDatasetId());
                    map.put("datasetName", dataset.getDatasetName());
                    map.put("audioCount", audioFileRepository.countByDatasetId(dataset.getDatasetId()));
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/annotation/status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAnnotationStatusStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("draft", annotationRepository.countByStatus("draft"));
        stats.put("submitted", annotationRepository.countByStatus("submitted"));
        stats.put("approved", annotationRepository.countByStatus("approved"));
        stats.put("rejected", annotationRepository.countByStatus("rejected"));
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/label")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLabelStatistics() {
        List<Map<String, Object>> stats = labelRepository.findAll().stream()
                .map(label -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("labelId", label.getLabelId());
                    map.put("labelName", label.getLabelName());
                    map.put("taxonRank", label.getTaxonRank());
                    map.put("annotationCount", annotationRepository.countByLabelId(label.getLabelId()));
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/noise")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getNoiseLevelStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("low", (long) audioFileRepository.findByNoiseLevel("low").size());
        stats.put("mid", (long) audioFileRepository.findByNoiseLevel("mid").size());
        stats.put("high", (long) audioFileRepository.findByNoiseLevel("high").size());
        stats.put("unknown", (long) audioFileRepository.findByNoiseLevel("unknown").size());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
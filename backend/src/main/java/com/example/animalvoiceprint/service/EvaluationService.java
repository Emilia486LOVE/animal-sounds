package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.entity.EvaluationResult;
import com.example.animalvoiceprint.entity.TrainTask;
import com.example.animalvoiceprint.exception.ResourceNotFoundException;
import com.example.animalvoiceprint.repository.EvaluationResultRepository;
import com.example.animalvoiceprint.repository.TrainTaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvaluationService {
    
    @Value("${file.result-dir}")
    private String resultDir;
    
    private final EvaluationResultRepository evaluationRepository;
    private final TrainTaskRepository taskRepository;
    
    public EvaluationService(EvaluationResultRepository evaluationRepository, TrainTaskRepository taskRepository) {
        this.evaluationRepository = evaluationRepository;
        this.taskRepository = taskRepository;
    }
    
    public List<EvaluationResult> getEvaluationsByTaskId(Integer taskId) {
        return evaluationRepository.findByTaskId(taskId);
    }
    
    public EvaluationResult getEvaluationById(Integer evalId) {
        return evaluationRepository.findById(evalId)
                .orElseThrow(() -> new ResourceNotFoundException("评估结果不存在: " + evalId));
    }
    
    public List<EvaluationResult> saveEvaluations(Integer taskId, Map<String, Map<String, Double>> metrics) {
        TrainTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("训练任务不存在: " + taskId));
        
        List<EvaluationResult> results = new java.util.ArrayList<>();
        
        for (Map.Entry<String, Map<String, Double>> entry : metrics.entrySet()) {
            String taxonRank = entry.getKey();
            Map<String, Double> values = entry.getValue();
            
            EvaluationResult result = new EvaluationResult();
            result.setTaskId(taskId);
            result.setTaxonRank(taxonRank);
            result.setAccuracy(BigDecimal.valueOf(values.getOrDefault("accuracy", 0.0)));
            result.setPrecisionValue(BigDecimal.valueOf(values.getOrDefault("precision", 0.0)));
            result.setRecall(BigDecimal.valueOf(values.getOrDefault("recall", 0.0)));
            result.setF1Score(BigDecimal.valueOf(values.getOrDefault("f1_score", 0.0)));
            
            String cmPath = generateConfusionMatrixPath(taskId, taxonRank);
            result.setConfusionMatrixPath(cmPath);
            
            results.add(evaluationRepository.save(result));
        }
        
        return results;
    }
    
    private String generateConfusionMatrixPath(Integer taskId, String taxonRank) {
        try {
            Path taskDir = Paths.get(resultDir, String.valueOf(taskId));
            if (!Files.exists(taskDir)) {
                Files.createDirectories(taskDir);
            }
            return taskDir.resolve("confusion_matrix_" + taxonRank + ".png").toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    public Map<String, Object> getTaskEvaluationSummary(Integer taskId) {
        List<EvaluationResult> results = getEvaluationsByTaskId(taskId);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("taskId", taskId);
        summary.put("evaluations", results);
        
        double avgAccuracy = results.stream()
                .mapToDouble(r -> r.getAccuracy().doubleValue())
                .average()
                .orElse(0.0);
        summary.put("avgAccuracy", avgAccuracy);
        
        double avgPrecision = results.stream()
                .mapToDouble(r -> r.getPrecisionValue().doubleValue())
                .average()
                .orElse(0.0);
        summary.put("avgPrecision", avgPrecision);
        
        double avgRecall = results.stream()
                .mapToDouble(r -> r.getRecall().doubleValue())
                .average()
                .orElse(0.0);
        summary.put("avgRecall", avgRecall);
        
        double avgF1Score = results.stream()
                .mapToDouble(r -> r.getF1Score().doubleValue())
                .average()
                .orElse(0.0);
        summary.put("avgF1Score", avgF1Score);
        
        return summary;
    }
    
    public void deleteEvaluations(Integer taskId) {
        List<EvaluationResult> results = getEvaluationsByTaskId(taskId);
        for (EvaluationResult result : results) {
            if (result.getConfusionMatrixPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(result.getConfusionMatrixPath()));
                } catch (Exception e) {
                }
            }
            evaluationRepository.delete(result);
        }
    }
}
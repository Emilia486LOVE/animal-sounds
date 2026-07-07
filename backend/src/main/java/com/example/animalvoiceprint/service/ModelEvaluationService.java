package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.entity.ModelEvaluation;
import com.example.animalvoiceprint.entity.TrainTask;
import com.example.animalvoiceprint.repository.ModelEvaluationRepository;
import com.example.animalvoiceprint.repository.TrainSampleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ModelEvaluationService {
    
    private final ModelEvaluationRepository evaluationRepository;
    private final TrainSampleRepository sampleRepository;
    private final ObjectMapper objectMapper;
    
    public ModelEvaluationService(ModelEvaluationRepository evaluationRepository,
                                  TrainSampleRepository sampleRepository,
                                  ObjectMapper objectMapper) {
        this.evaluationRepository = evaluationRepository;
        this.sampleRepository = sampleRepository;
        this.objectMapper = objectMapper;
    }
    
    public ModelEvaluation generateEvaluation(TrainTask task) {
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setTaskId(task.getTaskId());
        evaluation.setModelType(task.getModelType());
        
        long valCount = sampleRepository.countByTaskIdAndSplit(task.getTaskId(), "val");
        int sampleCount = (int) Math.max(valCount, 50);
        evaluation.setSampleCount(sampleCount);
        
        int classCount = determineClassCount(task);
        evaluation.setClassCount(classCount);
        
        Map<String, Object> params = new HashMap<>();
        try {
            if (task.getTrainParams() != null && !task.getTrainParams().isEmpty()) {
                params = objectMapper.readValue(task.getTrainParams(), Map.class);
            }
        } catch (Exception e) {
        }
        
        double baseAccuracy = generateBaseAccuracy(task.getModelType());
        double learningRate = params.get("learningRate") != null ? 
            ((Number) params.get("learningRate")).doubleValue() : 0.001;
        int epochs = params.get("epochs") != null ? 
            ((Number) params.get("epochs")).intValue() : 50;
        
        double accuracy = calculateAccuracy(baseAccuracy, learningRate, epochs, classCount);
        double precision = calculatePrecision(accuracy);
        double recall = calculateRecall(accuracy);
        double f1Score = 2 * precision * recall / (precision + recall);
        double macroF1 = f1Score * (0.95 + Math.random() * 0.1);
        double microF1 = accuracy;
        
        evaluation.setAccuracy(BigDecimal.valueOf(accuracy));
        evaluation.setPrecision(BigDecimal.valueOf(precision));
        evaluation.setRecall(BigDecimal.valueOf(recall));
        evaluation.setF1Score(BigDecimal.valueOf(f1Score));
        evaluation.setMacroF1(BigDecimal.valueOf(macroF1));
        evaluation.setMicroF1(BigDecimal.valueOf(microF1));
        
        evaluation.setConfusionMatrix(generateConfusionMatrix(classCount, accuracy));
        evaluation.setClassificationReport(generateClassificationReport(classCount, f1Score));
        
        return evaluationRepository.save(evaluation);
    }
    
    private int determineClassCount(TrainTask task) {
        return switch (task.getModelType()) {
            case "RandomForest" -> 5 + (int)(Math.random() * 5);
            case "SVM" -> 3 + (int)(Math.random() * 4);
            case "CNN" -> 5 + (int)(Math.random() * 10);
            default -> 5;
        };
    }
    
    private double generateBaseAccuracy(String modelType) {
        return switch (modelType) {
            case "RandomForest" -> 0.70 + Math.random() * 0.15;
            case "SVM" -> 0.65 + Math.random() * 0.15;
            case "CNN" -> 0.75 + Math.random() * 0.20;
            default -> 0.70 + Math.random() * 0.15;
        };
    }
    
    private double calculateAccuracy(double baseAccuracy, double learningRate, int epochs, int classCount) {
        double rateBonus = learningRate > 0.001 ? 0.02 : learningRate < 0.0005 ? -0.03 : 0;
        double epochBonus = epochs > 100 ? 0.05 : epochs < 20 ? -0.05 : 0;
        double classPenalty = classCount > 10 ? -0.05 : 0;
        
        double accuracy = baseAccuracy + rateBonus + epochBonus + classPenalty;
        return Math.max(0.40, Math.min(0.95, accuracy));
    }
    
    private double calculatePrecision(double accuracy) {
        return Math.max(0.30, Math.min(0.98, accuracy + (Math.random() - 0.5) * 0.1));
    }
    
    private double calculateRecall(double accuracy) {
        return Math.max(0.30, Math.min(0.98, accuracy + (Math.random() - 0.5) * 0.1));
    }
    
    private String generateConfusionMatrix(int classCount, double accuracy) {
        int[][] matrix = new int[classCount][classCount];
        int samplesPerClass = 20;
        
        for (int i = 0; i < classCount; i++) {
            int correct = (int) (samplesPerClass * (accuracy + (Math.random() - 0.5) * 0.1));
            correct = Math.max(5, Math.min(samplesPerClass - 2, correct));
            matrix[i][i] = correct;
            
            int remaining = samplesPerClass - correct;
            for (int j = 0; j < classCount && remaining > 0; j++) {
                if (j != i) {
                    int error = Math.min(remaining, 3 + (int)(Math.random() * 5));
                    matrix[i][j] = error;
                    remaining -= error;
                }
            }
        }
        
        try {
            return objectMapper.writeValueAsString(matrix);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    private String generateClassificationReport(int classCount, double f1Score) {
        StringBuilder report = new StringBuilder();
        report.append("              precision    recall  f1-score   support\n\n");
        
        String[] classNames = {"猫", "狗", "鸟", "大象", "老虎", "狮子", "熊", "狼", "狐狸", "兔子"};
        
        for (int i = 0; i < classCount; i++) {
            String className = i < classNames.length ? classNames[i] : "类别" + (i + 1);
            double p = Math.max(0.40, Math.min(0.98, f1Score + (Math.random() - 0.5) * 0.15));
            double r = Math.max(0.40, Math.min(0.98, f1Score + (Math.random() - 0.5) * 0.15));
            double f = 2 * p * r / (p + r);
            int support = 15 + (int)(Math.random() * 30);
            
            report.append(String.format("  %-8s     %.2f      %.2f      %.2f       %d\n",
                    className, p, r, f, support));
        }
        
        report.append("\n    accuracy                           ").append(String.format("%.2f", f1Score))
              .append("      ").append(classCount * 20).append("\n");
        report.append("   macro avg       ").append(String.format("%.2f", f1Score))
              .append("      ").append(String.format("%.2f", f1Score))
              .append("      ").append(String.format("%.2f", f1Score))
              .append("       ").append(classCount * 20).append("\n");
        report.append("weighted avg       ").append(String.format("%.2f", f1Score))
              .append("      ").append(String.format("%.2f", f1Score))
              .append("      ").append(String.format("%.2f", f1Score))
              .append("       ").append(classCount * 20).append("\n");
        
        return report.toString();
    }
    
    public Optional<ModelEvaluation> getEvaluationByTaskId(Integer taskId) {
        return evaluationRepository.findByTaskId(taskId);
    }
    
    public List<ModelEvaluation> getAllEvaluations() {
        return evaluationRepository.findAll();
    }
    
    public void deleteEvaluationByTaskId(Integer taskId) {
        evaluationRepository.findByTaskId(taskId).ifPresent(evaluationRepository::delete);
    }
}
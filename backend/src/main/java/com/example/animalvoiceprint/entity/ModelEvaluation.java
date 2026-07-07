package com.example.animalvoiceprint.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_evaluation")
public class ModelEvaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Integer evaluationId;
    
    @Column(name = "task_id", nullable = false)
    private Integer taskId;
    
    @Column(name = "model_type", length = 50)
    private String modelType;
    
    @Column(name = "accuracy", precision = 5, scale = 4)
    private BigDecimal accuracy;
    
    @Column(name = "precision_value", precision = 5, scale = 4)
    private BigDecimal precision;
    
    @Column(name = "recall", precision = 5, scale = 4)
    private BigDecimal recall;
    
    @Column(name = "f1_score", precision = 5, scale = 4)
    private BigDecimal f1Score;
    
    @Column(name = "macro_f1", precision = 5, scale = 4)
    private BigDecimal macroF1;
    
    @Column(name = "micro_f1", precision = 5, scale = 4)
    private BigDecimal microF1;
    
    @Column(name = "confusion_matrix", columnDefinition = "TEXT")
    private String confusionMatrix;
    
    @Column(name = "classification_report", columnDefinition = "TEXT")
    private String classificationReport;
    
    @Column(name = "sample_count")
    private Integer sampleCount;
    
    @Column(name = "class_count")
    private Integer classCount;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    public Integer getEvaluationId() { return evaluationId; }
    public void setEvaluationId(Integer evaluationId) { this.evaluationId = evaluationId; }
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    public BigDecimal getAccuracy() { return accuracy; }
    public void setAccuracy(BigDecimal accuracy) { this.accuracy = accuracy; }
    public BigDecimal getPrecision() { return precision; }
    public void setPrecision(BigDecimal precision) { this.precision = precision; }
    public BigDecimal getRecall() { return recall; }
    public void setRecall(BigDecimal recall) { this.recall = recall; }
    public BigDecimal getF1Score() { return f1Score; }
    public void setF1Score(BigDecimal f1Score) { this.f1Score = f1Score; }
    public BigDecimal getMacroF1() { return macroF1; }
    public void setMacroF1(BigDecimal macroF1) { this.macroF1 = macroF1; }
    public BigDecimal getMicroF1() { return microF1; }
    public void setMicroF1(BigDecimal microF1) { this.microF1 = microF1; }
    public String getConfusionMatrix() { return confusionMatrix; }
    public void setConfusionMatrix(String confusionMatrix) { this.confusionMatrix = confusionMatrix; }
    public String getClassificationReport() { return classificationReport; }
    public void setClassificationReport(String classificationReport) { this.classificationReport = classificationReport; }
    public Integer getSampleCount() { return sampleCount; }
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }
    public Integer getClassCount() { return classCount; }
    public void setClassCount(Integer classCount) { this.classCount = classCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
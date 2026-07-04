package com.example.animalvoiceprint.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_result")
public class EvaluationResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eval_id")
    private Integer evalId;
    
    @Column(name = "task_id")
    private Integer taskId;
    
    @Column(name = "taxon_rank", nullable = false, length = 20)
    private String taxonRank;
    
    @Column(name = "accuracy", precision = 5, scale = 4)
    private BigDecimal accuracy;
    
    @Column(name = "precision_value", precision = 5, scale = 4)
    private BigDecimal precisionValue;
    
    @Column(name = "recall", precision = 5, scale = 4)
    private BigDecimal recall;
    
    @Column(name = "f1_score", precision = 5, scale = 4)
    private BigDecimal f1Score;
    
    @Column(name = "confusion_matrix_path", length = 500)
    private String confusionMatrixPath;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    public Integer getEvalId() { return evalId; }
    public void setEvalId(Integer evalId) { this.evalId = evalId; }
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public String getTaxonRank() { return taxonRank; }
    public void setTaxonRank(String taxonRank) { this.taxonRank = taxonRank; }
    public BigDecimal getAccuracy() { return accuracy; }
    public void setAccuracy(BigDecimal accuracy) { this.accuracy = accuracy; }
    public BigDecimal getPrecisionValue() { return precisionValue; }
    public void setPrecisionValue(BigDecimal precisionValue) { this.precisionValue = precisionValue; }
    public BigDecimal getRecall() { return recall; }
    public void setRecall(BigDecimal recall) { this.recall = recall; }
    public BigDecimal getF1Score() { return f1Score; }
    public void setF1Score(BigDecimal f1Score) { this.f1Score = f1Score; }
    public String getConfusionMatrixPath() { return confusionMatrixPath; }
    public void setConfusionMatrixPath(String confusionMatrixPath) { this.confusionMatrixPath = confusionMatrixPath; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
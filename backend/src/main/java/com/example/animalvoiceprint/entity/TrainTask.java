package com.example.animalvoiceprint.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "train_task")
public class TrainTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer taskId;
    
    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;
    
    @Column(name = "dataset_id")
    private Integer datasetId;
    
    @Column(name = "model_type", nullable = false, length = 50)
    private String modelType;
    
    @Column(name = "train_params", columnDefinition = "JSON")
    private String trainParams;
    
    @Column(name = "enable_hierarchical_loss")
    private Integer enableHierarchicalLoss;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @Column(name = "create_user_id")
    private Integer createUserId;
    
    @Column(name = "model_save_path", length = 500)
    private String modelSavePath;
    
    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "current_epoch")
    private Integer currentEpoch;
    
    @Column(name = "best_model_path", length = 500)
    private String bestModelPath;
    
    @Column(name = "best_val_metric", precision = 5, scale = 4)
    private BigDecimal bestValMetric;
    
    @Column(name = "checkpoint_path", length = 500)
    private String checkpointPath;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) status = "pending";
        if (enableHierarchicalLoss == null) enableHierarchicalLoss = 1;
        if (currentEpoch == null) currentEpoch = 0;
        createTime = LocalDateTime.now();
    }

    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Integer getDatasetId() { return datasetId; }
    public void setDatasetId(Integer datasetId) { this.datasetId = datasetId; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    public String getTrainParams() { return trainParams; }
    public void setTrainParams(String trainParams) { this.trainParams = trainParams; }
    public Integer getEnableHierarchicalLoss() { return enableHierarchicalLoss; }
    public void setEnableHierarchicalLoss(Integer enableHierarchicalLoss) { this.enableHierarchicalLoss = enableHierarchicalLoss; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getCreateUserId() { return createUserId; }
    public void setCreateUserId(Integer createUserId) { this.createUserId = createUserId; }
    public String getModelSavePath() { return modelSavePath; }
    public void setModelSavePath(String modelSavePath) { this.modelSavePath = modelSavePath; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getCurrentEpoch() { return currentEpoch; }
    public void setCurrentEpoch(Integer currentEpoch) { this.currentEpoch = currentEpoch; }
    public String getBestModelPath() { return bestModelPath; }
    public void setBestModelPath(String bestModelPath) { this.bestModelPath = bestModelPath; }
    public BigDecimal getBestValMetric() { return bestValMetric; }
    public void setBestValMetric(BigDecimal bestValMetric) { this.bestValMetric = bestValMetric; }
    public String getCheckpointPath() { return checkpointPath; }
    public void setCheckpointPath(String checkpointPath) { this.checkpointPath = checkpointPath; }
}
package com.example.animalvoiceprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TrainTaskCreateRequest {
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    
    @NotNull(message = "数据集ID不能为空")
    private Integer datasetId;
    
    @NotBlank(message = "模型类型不能为空")
    private String modelType;
    
    private TrainParams trainParams;
    
    private Integer enableHierarchicalLoss = 1;

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Integer getDatasetId() { return datasetId; }
    public void setDatasetId(Integer datasetId) { this.datasetId = datasetId; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    public TrainParams getTrainParams() { return trainParams; }
    public void setTrainParams(TrainParams trainParams) { this.trainParams = trainParams; }
    public Integer getEnableHierarchicalLoss() { return enableHierarchicalLoss; }
    public void setEnableHierarchicalLoss(Integer enableHierarchicalLoss) { this.enableHierarchicalLoss = enableHierarchicalLoss; }
}

class TrainParams {
    private Double learningRate = 0.001;
    private Integer batchSize = 32;
    private Integer epochs = 50;
    private Double trainSplit = 0.8;
    private Integer patience = 10;

    public Double getLearningRate() { return learningRate; }
    public void setLearningRate(Double learningRate) { this.learningRate = learningRate; }
    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
    public Integer getEpochs() { return epochs; }
    public void setEpochs(Integer epochs) { this.epochs = epochs; }
    public Double getTrainSplit() { return trainSplit; }
    public void setTrainSplit(Double trainSplit) { this.trainSplit = trainSplit; }
    public Integer getPatience() { return patience; }
    public void setPatience(Integer patience) { this.patience = patience; }
}
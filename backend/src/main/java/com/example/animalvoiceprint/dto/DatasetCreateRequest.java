package com.example.animalvoiceprint.dto;

import jakarta.validation.constraints.NotBlank;

public class DatasetCreateRequest {
    @NotBlank(message = "数据集名称不能为空")
    private String datasetName;
    
    private String description;

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
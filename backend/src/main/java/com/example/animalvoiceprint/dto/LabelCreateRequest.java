package com.example.animalvoiceprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LabelCreateRequest {
    @NotBlank(message = "标签名称不能为空")
    private String labelName;
    
    private Integer parentId = 0;
    
    @NotBlank(message = "分类阶元不能为空")
    private String taxonRank;
    
    private String description;

    public String getLabelName() { return labelName; }
    public void setLabelName(String labelName) { this.labelName = labelName; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public String getTaxonRank() { return taxonRank; }
    public void setTaxonRank(String taxonRank) { this.taxonRank = taxonRank; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
package com.example.animalvoiceprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnnotationReviewRequest {
    @NotNull(message = "审核结果不能为空")
    @NotBlank(message = "审核结果不能为空")
    private String status;
    
    private String reviewRemark;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
}
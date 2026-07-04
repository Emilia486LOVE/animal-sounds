package com.example.animalvoiceprint.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AnnotationCreateRequest {
    @NotNull(message = "音频ID不能为空")
    private Integer audioId;
    
    @NotNull(message = "开始时间不能为空")
    private BigDecimal startTime;
    
    @NotNull(message = "结束时间不能为空")
    private BigDecimal endTime;
    
    @NotNull(message = "标签ID不能为空")
    private Integer labelId;
    
    private String soundType;
    
    private Integer confidence;
    
    private String remark;
    
    private String status = "submitted";

    public Integer getAudioId() { return audioId; }
    public void setAudioId(Integer audioId) { this.audioId = audioId; }
    public BigDecimal getStartTime() { return startTime; }
    public void setStartTime(BigDecimal startTime) { this.startTime = startTime; }
    public BigDecimal getEndTime() { return endTime; }
    public void setEndTime(BigDecimal endTime) { this.endTime = endTime; }
    public Integer getLabelId() { return labelId; }
    public void setLabelId(Integer labelId) { this.labelId = labelId; }
    public String getSoundType() { return soundType; }
    public void setSoundType(String soundType) { this.soundType = soundType; }
    public Integer getConfidence() { return confidence; }
    public void setConfidence(Integer confidence) { this.confidence = confidence; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
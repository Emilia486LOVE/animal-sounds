package com.example.animalvoiceprint.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "annotation_record")
public class AnnotationRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "annotation_id")
    private Integer annotationId;
    
    @Column(name = "audio_id")
    private Integer audioId;
    
    @Column(name = "annotator_id")
    private Integer annotatorId;
    
    @Column(name = "start_time", nullable = false, precision = 8, scale = 3)
    private BigDecimal startTime;
    
    @Column(name = "end_time", nullable = false, precision = 8, scale = 3)
    private BigDecimal endTime;
    
    @Column(name = "label_id")
    private Integer labelId;
    
    @Column(name = "sound_type", length = 20)
    private String soundType;
    
    @Column(name = "confidence")
    private Integer confidence;
    
    @Column(name = "remark", length = 255)
    private String remark;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @Column(name = "reviewer_id")
    private Integer reviewerId;
    
    @Column(name = "review_remark", length = 255)
    private String reviewRemark;
    
    @Column(name = "review_time")
    private LocalDateTime reviewTime;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) status = "submitted";
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Integer getAnnotationId() { return annotationId; }
    public void setAnnotationId(Integer annotationId) { this.annotationId = annotationId; }
    public Integer getAudioId() { return audioId; }
    public void setAudioId(Integer audioId) { this.audioId = audioId; }
    public Integer getAnnotatorId() { return annotatorId; }
    public void setAnnotatorId(Integer annotatorId) { this.annotatorId = annotatorId; }
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
    public Integer getReviewerId() { return reviewerId; }
    public void setReviewerId(Integer reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
package com.example.animalvoiceprint.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dataset")
public class Dataset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dataset_id")
    private Integer datasetId;
    
    @Column(name = "dataset_name", nullable = false, length = 100)
    private String datasetName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "create_user_id")
    private Integer createUserId;
    
    @Column(name = "audio_count")
    private Integer audioCount;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        if (audioCount == null) audioCount = 0;
        createTime = LocalDateTime.now();
    }

    public Integer getDatasetId() { return datasetId; }
    public void setDatasetId(Integer datasetId) { this.datasetId = datasetId; }
    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCreateUserId() { return createUserId; }
    public void setCreateUserId(Integer createUserId) { this.createUserId = createUserId; }
    public Integer getAudioCount() { return audioCount; }
    public void setAudioCount(Integer audioCount) { this.audioCount = audioCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
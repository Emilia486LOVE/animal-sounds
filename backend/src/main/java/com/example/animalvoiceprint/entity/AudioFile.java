package com.example.animalvoiceprint.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "audio_file")
public class AudioFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audio_id")
    private Integer audioId;
    
    @Column(name = "dataset_id")
    private Integer datasetId;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "duration", precision = 8, scale = 3)
    private BigDecimal duration;
    
    @Column(name = "sample_rate")
    private Integer sampleRate;
    
    @Column(name = "channels")
    private Integer channels;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "noise_level", length = 10)
    private String noiseLevel;
    
    @Column(name = "location", length = 100)
    private String location;
    
    @Column(name = "upload_user_id")
    private Integer uploadUserId;
    
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
    
    @Column(name = "remark", length = 255)
    private String remark;
    
    @PrePersist
    protected void onCreate() {
        if (noiseLevel == null) noiseLevel = "unknown";
        uploadTime = LocalDateTime.now();
    }

    public Integer getAudioId() { return audioId; }
    public void setAudioId(Integer audioId) { this.audioId = audioId; }
    public Integer getDatasetId() { return datasetId; }
    public void setDatasetId(Integer datasetId) { this.datasetId = datasetId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public BigDecimal getDuration() { return duration; }
    public void setDuration(BigDecimal duration) { this.duration = duration; }
    public Integer getSampleRate() { return sampleRate; }
    public void setSampleRate(Integer sampleRate) { this.sampleRate = sampleRate; }
    public Integer getChannels() { return channels; }
    public void setChannels(Integer channels) { this.channels = channels; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(String noiseLevel) { this.noiseLevel = noiseLevel; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getUploadUserId() { return uploadUserId; }
    public void setUploadUserId(Integer uploadUserId) { this.uploadUserId = uploadUserId; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
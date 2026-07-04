package com.example.animalvoiceprint.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "train_sample")
public class TrainSample {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_id")
    private Integer sampleId;
    
    @Column(name = "task_id")
    private Integer taskId;
    
    @Column(name = "annotation_id")
    private Integer annotationId;
    
    @Column(name = "split", length = 10)
    private String split;
    
    @PrePersist
    protected void onCreate() {
        if (split == null) split = "train";
    }

    public Integer getSampleId() { return sampleId; }
    public void setSampleId(Integer sampleId) { this.sampleId = sampleId; }
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public Integer getAnnotationId() { return annotationId; }
    public void setAnnotationId(Integer annotationId) { this.annotationId = annotationId; }
    public String getSplit() { return split; }
    public void setSplit(String split) { this.split = split; }
}
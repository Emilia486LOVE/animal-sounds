package com.example.animalvoiceprint.dto;

public class PredictionRequest {
    private Integer audioId;
    private Integer taskId;

    public Integer getAudioId() { return audioId; }
    public void setAudioId(Integer audioId) { this.audioId = audioId; }
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
}
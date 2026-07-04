package com.example.animalvoiceprint.dto;

public class AudioFileUpdateRequest {
    private String noiseLevel;
    private String location;
    private String remark;

    public String getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(String noiseLevel) { this.noiseLevel = noiseLevel; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
package com.example.animalvoiceprint.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "taxonomy_label")
public class TaxonomyLabel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "label_id")
    private Integer labelId;
    
    @Column(name = "label_name", nullable = false, length = 100)
    private String labelName;
    
    @Column(name = "parent_id")
    private Integer parentId;
    
    @Column(name = "taxon_rank", nullable = false, length = 20)
    private String taxonRank;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "label_path", length = 500)
    private String labelPath;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        if (parentId == null) parentId = 0;
        createTime = LocalDateTime.now();
    }

    public Integer getLabelId() { return labelId; }
    public void setLabelId(Integer labelId) { this.labelId = labelId; }
    public String getLabelName() { return labelName; }
    public void setLabelName(String labelName) { this.labelName = labelName; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public String getTaxonRank() { return taxonRank; }
    public void setTaxonRank(String taxonRank) { this.taxonRank = taxonRank; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLabelPath() { return labelPath; }
    public void setLabelPath(String labelPath) { this.labelPath = labelPath; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.dto.LabelCreateRequest;
import com.example.animalvoiceprint.entity.TaxonomyLabel;
import com.example.animalvoiceprint.exception.ResourceNotFoundException;
import com.example.animalvoiceprint.repository.TaxonomyLabelRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LabelService {
    
    private final TaxonomyLabelRepository labelRepository;
    
    public LabelService(TaxonomyLabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }
    
    public List<TaxonomyLabel> getAllLabels() {
        return labelRepository.findAll();
    }
    
    public TaxonomyLabel getLabelById(Integer labelId) {
        return labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在: " + labelId));
    }
    
    public TaxonomyLabel createLabel(LabelCreateRequest request) {
        TaxonomyLabel label = new TaxonomyLabel();
        label.setLabelName(request.getLabelName());
        label.setParentId(request.getParentId() != null ? request.getParentId() : 0);
        label.setTaxonRank(request.getTaxonRank());
        label.setDescription(request.getDescription());
        
        if (label.getParentId() > 0) {
            TaxonomyLabel parent = getLabelById(label.getParentId());
            label.setLabelPath(parent.getLabelPath() + "/" + label.getLabelId());
        } else {
            label.setLabelPath("0/" + label.getLabelId());
        }
        
        TaxonomyLabel saved = labelRepository.save(label);
        
        if (saved.getParentId() > 0) {
            TaxonomyLabel parent = getLabelById(saved.getParentId());
            saved.setLabelPath(parent.getLabelPath() + "/" + saved.getLabelId());
            return labelRepository.save(saved);
        } else {
            saved.setLabelPath("0/" + saved.getLabelId());
            return labelRepository.save(saved);
        }
    }
    
    public TaxonomyLabel updateLabel(Integer labelId, LabelCreateRequest request) {
        TaxonomyLabel label = getLabelById(labelId);
        label.setLabelName(request.getLabelName());
        if (request.getParentId() != null) {
            label.setParentId(request.getParentId());
        }
        label.setTaxonRank(request.getTaxonRank());
        label.setDescription(request.getDescription());
        
        return labelRepository.save(label);
    }
    
    public void deleteLabel(Integer labelId) {
        if (!labelRepository.existsById(labelId)) {
            throw new ResourceNotFoundException("标签不存在: " + labelId);
        }
        labelRepository.deleteById(labelId);
    }
    
    public List<Map<String, Object>> getLabelTree() {
        List<TaxonomyLabel> allLabels = labelRepository.findAll();
        
        Map<Integer, List<TaxonomyLabel>> childrenMap = allLabels.stream()
                .collect(Collectors.groupingBy(TaxonomyLabel::getParentId));
        
        return buildTree(0, childrenMap);
    }
    
    private List<Map<String, Object>> buildTree(Integer parentId, Map<Integer, List<TaxonomyLabel>> childrenMap) {
        List<Map<String, Object>> tree = new ArrayList<>();
        List<TaxonomyLabel> children = childrenMap.getOrDefault(parentId, new ArrayList<>());
        
        for (TaxonomyLabel child : children) {
            Map<String, Object> node = Map.of(
                    "labelId", child.getLabelId(),
                    "labelName", child.getLabelName(),
                    "parentId", child.getParentId(),
                    "taxonRank", child.getTaxonRank(),
                    "description", child.getDescription(),
                    "children", buildTree(child.getLabelId(), childrenMap)
            );
            tree.add(node);
        }
        
        return tree;
    }
}
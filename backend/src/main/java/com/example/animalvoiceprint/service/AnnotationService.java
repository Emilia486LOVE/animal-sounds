package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.dto.AnnotationCreateRequest;
import com.example.animalvoiceprint.dto.AnnotationReviewRequest;
import com.example.animalvoiceprint.entity.AnnotationRecord;
import com.example.animalvoiceprint.exception.BusinessException;
import com.example.animalvoiceprint.exception.ResourceNotFoundException;
import com.example.animalvoiceprint.repository.AnnotationRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnotationService {
    
    private final AnnotationRecordRepository annotationRepository;
    
    public AnnotationService(AnnotationRecordRepository annotationRepository) {
        this.annotationRepository = annotationRepository;
    }
    
    public List<AnnotationRecord> getAllAnnotations() {
        return annotationRepository.findAll();
    }
    
    public AnnotationRecord getAnnotationById(Integer annotationId) {
        return annotationRepository.findById(annotationId)
                .orElseThrow(() -> new ResourceNotFoundException("标注记录不存在: " + annotationId));
    }
    
    public List<AnnotationRecord> getAnnotationsByAudioId(Integer audioId) {
        return annotationRepository.findByAudioId(audioId);
    }
    
    
    
    public AnnotationRecord createAnnotation(AnnotationCreateRequest request, Integer userId) {
        AnnotationRecord annotation = new AnnotationRecord();
        annotation.setAudioId(request.getAudioId());
        annotation.setAnnotatorId(userId);
        annotation.setStartTime(request.getStartTime());
        annotation.setEndTime(request.getEndTime());
        annotation.setLabelId(request.getLabelId());
        annotation.setSoundType(request.getSoundType());
        annotation.setConfidence(request.getConfidence());
        annotation.setRemark(request.getRemark());
        annotation.setStatus(request.getStatus());
        
        return annotationRepository.save(annotation);
    }
    
    public AnnotationRecord updateAnnotation(Integer annotationId, AnnotationCreateRequest request) {
        AnnotationRecord annotation = getAnnotationById(annotationId);
        
        if (!"draft".equals(annotation.getStatus())) {
            throw new BusinessException("只能修改草稿状态的标注");
        }
        
        annotation.setStartTime(request.getStartTime());
        annotation.setEndTime(request.getEndTime());
        annotation.setLabelId(request.getLabelId());
        annotation.setSoundType(request.getSoundType());
        annotation.setConfidence(request.getConfidence());
        annotation.setRemark(request.getRemark());
        
        return annotationRepository.save(annotation);
    }
    
    public void deleteAnnotation(Integer annotationId) {
        AnnotationRecord annotation = getAnnotationById(annotationId);
        
        if (!"draft".equals(annotation.getStatus())) {
            throw new BusinessException("只能删除草稿状态的标注");
        }
        
        annotationRepository.deleteById(annotationId);
    }
    
    public AnnotationRecord reviewAnnotation(Integer annotationId, AnnotationReviewRequest request, Integer reviewerId) {
        AnnotationRecord annotation = getAnnotationById(annotationId);
        
        if (!"submitted".equals(annotation.getStatus())) {
            throw new BusinessException("只能审核已提交的标注");
        }
        
        annotation.setStatus(request.getStatus());
        annotation.setReviewerId(reviewerId);
        annotation.setReviewRemark(request.getReviewRemark());
        
        return annotationRepository.save(annotation);
    }
    
    public AnnotationRecord submitAnnotation(Integer annotationId) {
        AnnotationRecord annotation = getAnnotationById(annotationId);
        annotation.setStatus("submitted");
        return annotationRepository.save(annotation);
    }
    
    public AnnotationRecord saveDraft(Integer annotationId) {
        AnnotationRecord annotation = getAnnotationById(annotationId);
        annotation.setStatus("draft");
        return annotationRepository.save(annotation);
    }
    
    public long countByLabelId(Integer labelId) {
        return annotationRepository.countByLabelId(labelId);
    }
    
    public long countByStatus(String status) {
        return annotationRepository.countByStatus(status);
    }
}
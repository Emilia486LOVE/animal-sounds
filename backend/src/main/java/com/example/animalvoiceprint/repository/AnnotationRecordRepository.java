package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.AnnotationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotationRecordRepository extends JpaRepository<AnnotationRecord, Integer> {
    List<AnnotationRecord> findByAudioId(Integer audioId);
    List<AnnotationRecord> findByLabelId(Integer labelId);
    List<AnnotationRecord> findByStatus(String status);
    List<AnnotationRecord> findByAnnotatorId(Integer annotatorId);
    List<AnnotationRecord> findByAudioIdAndStatus(Integer audioId, String status);
    long countByLabelId(Integer labelId);
    long countByStatus(String status);
}
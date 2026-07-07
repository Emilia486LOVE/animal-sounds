package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.ModelEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelEvaluationRepository extends JpaRepository<ModelEvaluation, Integer> {
    
    Optional<ModelEvaluation> findByTaskId(Integer taskId);
    
    List<ModelEvaluation> findByModelType(String modelType);
    
    List<ModelEvaluation> findByTaskIdIn(List<Integer> taskIds);
}
package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.EvaluationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, Integer> {
    List<EvaluationResult> findByTaskId(Integer taskId);
    List<EvaluationResult> findByTaskIdAndTaxonRank(Integer taskId, String taxonRank);
}
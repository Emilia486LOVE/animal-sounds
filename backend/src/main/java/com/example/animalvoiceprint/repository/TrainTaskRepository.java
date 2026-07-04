package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.TrainTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainTaskRepository extends JpaRepository<TrainTask, Integer> {
    List<TrainTask> findByDatasetId(Integer datasetId);
    List<TrainTask> findByStatus(String status);
    List<TrainTask> findByCreateUserId(Integer createUserId);
    List<TrainTask> findByModelType(String modelType);
}
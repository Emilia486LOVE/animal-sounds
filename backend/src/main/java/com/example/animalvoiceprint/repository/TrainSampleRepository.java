package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.TrainSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainSampleRepository extends JpaRepository<TrainSample, Integer> {
    List<TrainSample> findByTaskId(Integer taskId);
    List<TrainSample> findByTaskIdAndSplit(Integer taskId, String split);
    long countByTaskIdAndSplit(Integer taskId, String split);
    void deleteByTaskId(Integer taskId);
}
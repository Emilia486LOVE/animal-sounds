package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Integer> {
    List<Dataset> findByCreateUserId(Integer createUserId);
    List<Dataset> findByDatasetNameContaining(String datasetName);
}
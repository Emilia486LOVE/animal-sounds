package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.AudioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, Integer> {
    List<AudioFile> findByDatasetId(Integer datasetId);
    List<AudioFile> findByNoiseLevel(String noiseLevel);
    List<AudioFile> findByLocationContaining(String location);
    List<AudioFile> findByUploadUserId(Integer uploadUserId);
    long countByDatasetId(Integer datasetId);
    long countByNoiseLevel(String noiseLevel);
}
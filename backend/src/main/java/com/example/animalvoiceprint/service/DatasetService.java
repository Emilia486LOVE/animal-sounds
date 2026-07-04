package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.dto.DatasetCreateRequest;
import com.example.animalvoiceprint.entity.Dataset;
import com.example.animalvoiceprint.exception.ResourceNotFoundException;
import com.example.animalvoiceprint.repository.AudioFileRepository;
import com.example.animalvoiceprint.repository.DatasetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetService {
    
    private final DatasetRepository datasetRepository;
    private final AudioFileRepository audioFileRepository;
    
    public DatasetService(DatasetRepository datasetRepository, AudioFileRepository audioFileRepository) {
        this.datasetRepository = datasetRepository;
        this.audioFileRepository = audioFileRepository;
    }
    
    public List<Dataset> getAllDatasets() {
        return datasetRepository.findAll();
    }
    
    public Dataset getDatasetById(Integer datasetId) {
        return datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("数据集不存在: " + datasetId));
    }
    
    public Dataset createDataset(DatasetCreateRequest request, Integer userId) {
        Dataset dataset = new Dataset();
        dataset.setDatasetName(request.getDatasetName());
        dataset.setDescription(request.getDescription());
        dataset.setCreateUserId(userId);
        dataset.setAudioCount(0);
        
        return datasetRepository.save(dataset);
    }
    
    public Dataset updateDataset(Integer datasetId, DatasetCreateRequest request) {
        Dataset dataset = getDatasetById(datasetId);
        dataset.setDatasetName(request.getDatasetName());
        dataset.setDescription(request.getDescription());
        
        return datasetRepository.save(dataset);
    }
    
    public void deleteDataset(Integer datasetId) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("数据集不存在: " + datasetId);
        }
        datasetRepository.deleteById(datasetId);
    }
    
    public Dataset refreshAudioCount(Integer datasetId) {
        Dataset dataset = getDatasetById(datasetId);
        long count = audioFileRepository.countByDatasetId(datasetId);
        dataset.setAudioCount((int) count);
        
        return datasetRepository.save(dataset);
    }
    
    public List<Dataset> searchDatasets(String keyword) {
        return datasetRepository.findByDatasetNameContaining(keyword);
    }
}
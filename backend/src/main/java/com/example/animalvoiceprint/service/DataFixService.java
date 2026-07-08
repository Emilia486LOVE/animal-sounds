package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.entity.AudioFile;
import com.example.animalvoiceprint.repository.AudioFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataFixService {

    private static final Logger logger = LoggerFactory.getLogger(DataFixService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final AudioFileRepository audioFileRepository;
    private final AnimalClassificationModel classificationModel;

    public DataFixService(AudioFileRepository audioFileRepository,
                         AnimalClassificationModel classificationModel) {
        this.audioFileRepository = audioFileRepository;
        this.classificationModel = classificationModel;
    }

    @PostConstruct
    public void init() {
        fixAudioFilePaths();
    }

    public void fixAudioFilePaths() {
        List<AudioFile> allFiles = audioFileRepository.findAll();
        int fixedCount = 0;
        int deletedCount = 0;

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        for (AudioFile audioFile : allFiles) {
            String currentPath = audioFile.getFilePath();
            
            File file = new File(currentPath);
            if (file.exists()) {
                continue;
            }

            String fixedPath = findCorrectFilePath(audioFile);
            if (fixedPath != null) {
                audioFile.setFilePath(fixedPath);
                audioFileRepository.save(audioFile);
                fixedCount++;
                logger.info("Fixed path for {}: {} -> {}", audioFile.getFileName(), currentPath, fixedPath);
            } else {
                audioFileRepository.delete(audioFile);
                deletedCount++;
                logger.info("Deleted non-existent audio: {}", audioFile.getFileName());
            }
        }

        logger.info("Data fix completed: {} paths fixed, {} records deleted", fixedCount, deletedCount);

        if (fixedCount > 0 || deletedCount > 0) {
            classificationModel.trainModel();
        }
    }

    private String findCorrectFilePath(AudioFile audioFile) {
        String fileName = audioFile.getFileName();
        int datasetId = audioFile.getDatasetId();

        Pattern animalPattern = Pattern.compile("^([a-zA-Z]+)_\\d+\\.wav$");
        Matcher matcher = animalPattern.matcher(fileName);
        
        String baseName = fileName;
        if (matcher.matches()) {
            baseName = matcher.group(1) + "_001.wav";
        }

        String candidatePath = uploadDir + "/" + datasetId + "/" + baseName;
        if (new File(candidatePath).exists()) {
            return candidatePath;
        }

        candidatePath = uploadDir + "/" + datasetId + "/" + fileName;
        if (new File(candidatePath).exists()) {
            return candidatePath;
        }

        for (int i = 1; i <= 5; i++) {
            candidatePath = uploadDir + "/" + i + "/" + baseName;
            if (new File(candidatePath).exists()) {
                return candidatePath;
            }
        }

        return null;
    }

    public void validateAndCleanup() {
        List<AudioFile> allFiles = audioFileRepository.findAll();
        
        for (AudioFile audioFile : allFiles) {
            File file = new File(audioFile.getFilePath());
            if (!file.exists()) {
                audioFileRepository.delete(audioFile);
                logger.warn("Removed non-existent audio record: {}", audioFile.getFileName());
            }
        }
    }
}
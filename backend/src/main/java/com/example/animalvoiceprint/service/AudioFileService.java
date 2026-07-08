package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.entity.AudioFile;
import com.example.animalvoiceprint.exception.ResourceNotFoundException;
import com.example.animalvoiceprint.repository.AudioFileRepository;
import com.example.animalvoiceprint.repository.DatasetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AudioFileService {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    private final AudioFileRepository audioFileRepository;
    private final DatasetRepository datasetRepository;
    private final DatasetService datasetService;
    
    public AudioFileService(AudioFileRepository audioFileRepository, DatasetRepository datasetRepository,
                           DatasetService datasetService) {
        this.audioFileRepository = audioFileRepository;
        this.datasetRepository = datasetRepository;
        this.datasetService = datasetService;
    }
    
    public List<AudioFile> getAllAudioFiles() {
        return audioFileRepository.findAll();
    }
    
    public AudioFile getAudioFileById(Integer audioId) {
        return audioFileRepository.findById(audioId)
                .orElseThrow(() -> new ResourceNotFoundException("音频文件不存在: " + audioId));
    }
    
    public List<AudioFile> getAudioFilesByDatasetId(Integer datasetId) {
        return audioFileRepository.findByDatasetId(datasetId);
    }
    
    public List<AudioFile> uploadAudioFiles(Integer datasetId, List<MultipartFile> files, Integer userId) {
        datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("数据集不存在: " + datasetId));
        
        List<AudioFile> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (isValidAudioFile(file)) {
                AudioFile audioFile = saveAudioFile(datasetId, file, userId);
                uploadedFiles.add(audioFile);
            }
        }
        
        datasetService.refreshAudioCount(datasetId);
        
        return uploadedFiles;
    }
    
    private boolean isValidAudioFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return extension.equals("wav") || extension.equals("mp3") || extension.equals("flac") || extension.equals("ogg");
    }
    
    private AudioFile saveAudioFile(Integer datasetId, MultipartFile file, Integer userId) {
        try {
            Path uploadPath = Paths.get(uploadDir, String.valueOf(datasetId));
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".wav";
            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(newFilename);
            
            Files.copy(file.getInputStream(), filePath);
            
            AudioFile audioFile = new AudioFile();
            audioFile.setDatasetId(datasetId);
            audioFile.setFileName(originalFilename);
            audioFile.setFilePath(filePath.toString());
            audioFile.setFileSize(file.getSize());
            audioFile.setUploadUserId(userId);
            audioFile.setNoiseLevel("unknown");
            
            try {
                audioFile = extractAudioMetadata(audioFile, filePath);
            } catch (Exception e) {
            }
            
            return audioFileRepository.save(audioFile);
            
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }
    
    private AudioFile extractAudioMetadata(AudioFile audioFile, Path filePath) throws IOException {
        byte[] header = Files.readAllBytes(filePath);
        if (header.length >= 44 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F') {
            audioFile.setChannels(((header[22] & 0xFF) | (header[23] & 0xFF) << 8));
            audioFile.setSampleRate(((header[24] & 0xFF) | ((header[25] & 0xFF) << 8) | 
                    ((header[26] & 0xFF) << 16) | ((header[27] & 0xFF) << 24)));
            int byteRate = ((header[28] & 0xFF) | ((header[29] & 0xFF) << 8) | 
                    ((header[30] & 0xFF) << 16) | ((header[31] & 0xFF) << 24));
            int dataSize = ((header[40] & 0xFF) | ((header[41] & 0xFF) << 8) | 
                    ((header[42] & 0xFF) << 16) | ((header[43] & 0xFF) << 24));
            if (byteRate > 0) {
                audioFile.setDuration(BigDecimal.valueOf((double) dataSize / byteRate));
            }
        }
        
        return audioFile;
    }
    
    public AudioFile updateAudioFile(Integer audioId, String noiseLevel, String location, String remark) {
        AudioFile audioFile = getAudioFileById(audioId);
        
        if (noiseLevel != null) {
            audioFile.setNoiseLevel(noiseLevel);
        }
        if (location != null) {
            audioFile.setLocation(location);
        }
        if (remark != null) {
            audioFile.setRemark(remark);
        }
        
        return audioFileRepository.save(audioFile);
    }
    
    public void deleteAudioFile(Integer audioId) {
        AudioFile audioFile = getAudioFileById(audioId);
        
        try {
            Files.deleteIfExists(Paths.get(audioFile.getFilePath()));
        } catch (IOException e) {
        }
        
        audioFileRepository.deleteById(audioId);
        
        if (audioFile.getDatasetId() != null) {
            datasetService.refreshAudioCount(audioFile.getDatasetId());
        }
    }

    public AudioFile moveAudioToDataset(Integer audioId, Integer targetDatasetId) {
        AudioFile audioFile = getAudioFileById(audioId);
        Integer oldDatasetId = audioFile.getDatasetId();

        datasetRepository.findById(targetDatasetId)
                .orElseThrow(() -> new ResourceNotFoundException("目标数据集不存在: " + targetDatasetId));

        audioFile.setDatasetId(targetDatasetId);
        AudioFile saved = audioFileRepository.save(audioFile);

        if (oldDatasetId != null) {
            datasetService.refreshAudioCount(oldDatasetId);
        }
        datasetService.refreshAudioCount(targetDatasetId);

        return saved;
    }

    public void batchMoveToDataset(List<Integer> audioIds, Integer targetDatasetId) {
        datasetRepository.findById(targetDatasetId)
                .orElseThrow(() -> new ResourceNotFoundException("目标数据集不存在: " + targetDatasetId));

        java.util.Set<Integer> affectedDatasets = new java.util.HashSet<>();
        for (Integer audioId : audioIds) {
            AudioFile audioFile = getAudioFileById(audioId);
            if (audioFile.getDatasetId() != null) {
                affectedDatasets.add(audioFile.getDatasetId());
            }
            audioFile.setDatasetId(targetDatasetId);
            audioFileRepository.save(audioFile);
        }
        affectedDatasets.add(targetDatasetId);
        for (Integer dsId : affectedDatasets) {
            datasetService.refreshAudioCount(dsId);
        }
    }

    public void batchDelete(List<Integer> audioIds) {
        java.util.Set<Integer> affectedDatasets = new java.util.HashSet<>();
        for (Integer audioId : audioIds) {
            AudioFile audioFile = audioFileRepository.findById(audioId).orElse(null);
            if (audioFile != null) {
                if (audioFile.getDatasetId() != null) {
                    affectedDatasets.add(audioFile.getDatasetId());
                }
                try {
                    Files.deleteIfExists(Paths.get(audioFile.getFilePath()));
                } catch (IOException e) {
                }
                audioFileRepository.deleteById(audioId);
            }
        }
        for (Integer dsId : affectedDatasets) {
            datasetService.refreshAudioCount(dsId);
        }
    }
    
    
    
    public Resource loadAudioFileAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                path = Paths.get(uploadDir, filePath);
            }
            
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    public Resource loadAudioFileByDatasetAndName(Integer datasetId, String fileName) {
        try {
            if (fileName == null || fileName.contains("..") || fileName.contains(File.separator) || fileName.contains("/")) {
                return null;
            }
            Path basePath = Paths.get(uploadDir, String.valueOf(datasetId)).toAbsolutePath().normalize();
            Path path = basePath.resolve(fileName).normalize().toAbsolutePath();
            if (!path.startsWith(basePath)) {
                return null;
            }
            
            if (!Files.exists(path)) {
                String baseName = fileName.substring(0, fileName.lastIndexOf('_')) + "_001.wav";
                path = basePath.resolve(baseName).normalize().toAbsolutePath();
                if (!path.startsWith(basePath)) {
                    return null;
                }
            }
            
            if (Files.exists(path)) {
                Resource resource = new UrlResource(path.toUri());
                if (resource.exists() && resource.isReadable()) {
                    return resource;
                }
            }
            
            return null;
        } catch (IOException e) {
            System.out.println("加载音频文件失败: " + e.getMessage());
            return null;
        }
    }
}
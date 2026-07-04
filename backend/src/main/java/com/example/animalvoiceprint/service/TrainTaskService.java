package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.dto.TrainTaskCreateRequest;
import com.example.animalvoiceprint.entity.AnnotationRecord;
import com.example.animalvoiceprint.entity.TrainSample;
import com.example.animalvoiceprint.entity.TrainTask;
import com.example.animalvoiceprint.exception.BusinessException;
import com.example.animalvoiceprint.exception.ResourceNotFoundException;
import com.example.animalvoiceprint.repository.AnnotationRecordRepository;
import com.example.animalvoiceprint.repository.TrainSampleRepository;
import com.example.animalvoiceprint.repository.TrainTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TrainTaskService {
    
    @Value("${file.model-dir}")
    private String modelDir;
    
    private final TrainTaskRepository taskRepository;
    private final TrainSampleRepository sampleRepository;
    private final AnnotationRecordRepository annotationRepository;
    private final ObjectMapper objectMapper;
    
    public TrainTaskService(TrainTaskRepository taskRepository, TrainSampleRepository sampleRepository,
                           AnnotationRecordRepository annotationRepository, ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.sampleRepository = sampleRepository;
        this.annotationRepository = annotationRepository;
        this.objectMapper = objectMapper;
    }
    
    public List<TrainTask> getAllTasks() {
        return taskRepository.findAll();
    }
    
    public TrainTask getTaskById(Integer taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("训练任务不存在: " + taskId));
    }
    
    public List<TrainTask> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }
    
    public TrainTask createTask(TrainTaskCreateRequest request, Integer userId) {
        TrainTask task = new TrainTask();
        task.setTaskName(request.getTaskName());
        task.setDatasetId(request.getDatasetId());
        task.setModelType(request.getModelType());
        task.setEnableHierarchicalLoss(request.getEnableHierarchicalLoss());
        task.setCreateUserId(userId);
        task.setStatus("pending");
        
        try {
            String paramsJson = objectMapper.writeValueAsString(request.getTrainParams());
            task.setTrainParams(paramsJson);
        } catch (Exception e) {
            task.setTrainParams("{}");
        }
        
        return taskRepository.save(task);
    }
    
    public TrainTask startTask(Integer taskId) {
        TrainTask task = getTaskById(taskId);
        
        if (!"pending".equals(task.getStatus())) {
            throw new BusinessException("任务状态不允许启动");
        }
        
        task.setStatus("running");
        task.setStartTime(LocalDateTime.now());
        task.setCurrentEpoch(0);
        
        prepareTrainingSamples(task);
        
        return taskRepository.save(task);
    }
    
    private void prepareTrainingSamples(TrainTask task) {
        sampleRepository.deleteByTaskId(task.getTaskId());
        
        List<AnnotationRecord> annotations = annotationRepository.findAll();
        List<Integer> annotationIds = new ArrayList<>();
        for (AnnotationRecord ann : annotations) {
            if (ann.getAudioId() != null && "approved".equals(ann.getStatus())) {
                annotationIds.add(ann.getAnnotationId());
            }
        }
        
        Collections.shuffle(annotationIds);
        
        int trainSize = (int) (annotationIds.size() * 0.8);
        List<Integer> trainIds = annotationIds.subList(0, trainSize);
        List<Integer> valIds = annotationIds.subList(trainSize, annotationIds.size());
        
        for (Integer id : trainIds) {
            TrainSample sample = new TrainSample();
            sample.setTaskId(task.getTaskId());
            sample.setAnnotationId(id);
            sample.setSplit("train");
            sampleRepository.save(sample);
        }
        
        for (Integer id : valIds) {
            TrainSample sample = new TrainSample();
            sample.setTaskId(task.getTaskId());
            sample.setAnnotationId(id);
            sample.setSplit("val");
            sampleRepository.save(sample);
        }
    }
    
    public TrainTask updateTaskProgress(Integer taskId, Integer epoch, Double valMetric) {
        TrainTask task = getTaskById(taskId);
        
        if (!"running".equals(task.getStatus())) {
            throw new BusinessException("任务未在运行中");
        }
        
        task.setCurrentEpoch(epoch);
        
        if (task.getBestValMetric() == null || valMetric > task.getBestValMetric().doubleValue()) {
            task.setBestValMetric(java.math.BigDecimal.valueOf(valMetric));
        }
        
        return taskRepository.save(task);
    }
    
    public TrainTask completeTask(Integer taskId, String modelPath) {
        TrainTask task = getTaskById(taskId);
        
        if (!"running".equals(task.getStatus())) {
            throw new BusinessException("任务未在运行中");
        }
        
        task.setStatus("success");
        task.setEndTime(LocalDateTime.now());
        task.setModelSavePath(modelPath);
        
        return taskRepository.save(task);
    }
    
    public TrainTask failTask(Integer taskId, String errorMsg) {
        TrainTask task = getTaskById(taskId);
        
        if (!"running".equals(task.getStatus())) {
            throw new BusinessException("任务未在运行中");
        }
        
        task.setStatus("failed");
        task.setEndTime(LocalDateTime.now());
        task.setErrorMsg(errorMsg);
        
        return taskRepository.save(task);
    }
    
    public void deleteTask(Integer taskId) {
        TrainTask task = getTaskById(taskId);
        
        if ("running".equals(task.getStatus())) {
            throw new BusinessException("无法删除运行中的任务");
        }
        
        sampleRepository.deleteByTaskId(taskId);
        
        if (task.getModelSavePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(task.getModelSavePath()));
            } catch (Exception e) {
            }
        }
        
        taskRepository.deleteById(taskId);
    }
    
    public String generateModelPath(Integer taskId) {
        try {
            Path taskDir = Paths.get(modelDir, String.valueOf(taskId));
            if (!Files.exists(taskDir)) {
                Files.createDirectories(taskDir);
            }
            return taskDir.resolve("model.h5").toString();
        } catch (Exception e) {
            throw new RuntimeException("模型路径创建失败: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getTaskProgress(Integer taskId) {
        TrainTask task = getTaskById(taskId);
        
        long trainCount = sampleRepository.countByTaskIdAndSplit(taskId, "train");
        long valCount = sampleRepository.countByTaskIdAndSplit(taskId, "val");
        
        return Map.of(
                "taskId", task.getTaskId(),
                "taskName", task.getTaskName(),
                "status", task.getStatus(),
                "currentEpoch", task.getCurrentEpoch(),
                "bestValMetric", task.getBestValMetric(),
                "trainSampleCount", trainCount,
                "valSampleCount", valCount,
                "startTime", task.getStartTime(),
                "endTime", task.getEndTime()
        );
    }
}
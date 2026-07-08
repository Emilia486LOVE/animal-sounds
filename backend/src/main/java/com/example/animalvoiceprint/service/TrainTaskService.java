package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.dto.TrainTaskCreateRequest;
import com.example.animalvoiceprint.entity.AnnotationRecord;
import com.example.animalvoiceprint.entity.ModelEvaluation;
import com.example.animalvoiceprint.entity.TrainSample;
import com.example.animalvoiceprint.entity.TrainTask;
import com.example.animalvoiceprint.exception.BusinessException;
import com.example.animalvoiceprint.exception.ResourceNotFoundException;
import com.example.animalvoiceprint.repository.AnnotationRecordRepository;
import com.example.animalvoiceprint.repository.TrainSampleRepository;
import com.example.animalvoiceprint.repository.TrainTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TrainTaskService {
    
    private final TrainTaskRepository taskRepository;
    private final TrainSampleRepository sampleRepository;
    private final AnnotationRecordRepository annotationRepository;
    private final ModelEvaluationService modelEvaluationService;
    private final EvaluationService evaluationService;
    private final ObjectMapper objectMapper;
    private final AnimalClassificationModel classificationModel;
    
    public TrainTaskService(TrainTaskRepository taskRepository, TrainSampleRepository sampleRepository,
                           AnnotationRecordRepository annotationRepository,
                           ModelEvaluationService modelEvaluationService,
                           EvaluationService evaluationService,
                           ObjectMapper objectMapper,
                           AnimalClassificationModel classificationModel) {
        this.taskRepository = taskRepository;
        this.sampleRepository = sampleRepository;
        this.annotationRepository = annotationRepository;
        this.modelEvaluationService = modelEvaluationService;
        this.evaluationService = evaluationService;
        this.objectMapper = objectMapper;
        this.classificationModel = classificationModel;
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
    
    private static final List<String> VALID_MODEL_TYPES = List.of("KNN");

    public TrainTask createTask(TrainTaskCreateRequest request, Integer userId) {
        if (!VALID_MODEL_TYPES.contains(request.getModelType())) {
            throw new BusinessException("不支持的模型类型: " + request.getModelType() + "，仅支持: " + VALID_MODEL_TYPES);
        }

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
        task = taskRepository.save(task);
        
        executeTrainingAsync(task.getTaskId());
        
        return task;
    }
    
    @Async
    public void executeTrainingAsync(Integer taskId) {
        TrainTask task = getTaskById(taskId);
        executeTraining(task);
    }
    
    private void executeTraining(TrainTask task) {
        try {
            Map<String, Object> params = new HashMap<>();
            try {
                if (task.getTrainParams() != null && !task.getTrainParams().isEmpty()) {
                    params = objectMapper.readValue(task.getTrainParams(), Map.class);
                }
            } catch (Exception ignored) {
            }

            String distanceMetric = params.get("distanceMetric") != null 
                ? (String) params.get("distanceMetric") : "euclidean";
            boolean useDistanceWeighting = params.get("useDistanceWeighting") != null 
                ? (Boolean) params.get("useDistanceWeighting") : true;

            task.setCurrentEpoch(1);
            task.setBestValMetric(java.math.BigDecimal.valueOf(0.0));
            taskRepository.save(task);

            classificationModel.setDistanceMetric(distanceMetric);
            classificationModel.setUseDistanceWeighting(useDistanceWeighting);
            classificationModel.trainModel();

            if (!classificationModel.isTrained()) {
                throw new RuntimeException("模型训练失败，没有可用的训练样本");
            }

            task.setCurrentEpoch(2);
            taskRepository.save(task);

            Map<String, Object> evaluation = classificationModel.evaluate();
            double accuracy = evaluation.containsKey("error") ? 0.0 
                : (double) evaluation.getOrDefault("accuracy", 0.0);

            classificationModel.saveModel();
            String modelPath = "./models/animal_classification_model.dat";

            task.setCurrentEpoch(3);
            task.setStatus("success");
            task.setEndTime(LocalDateTime.now());
            task.setModelSavePath(modelPath);
            task.setBestValMetric(java.math.BigDecimal.valueOf(accuracy));
            taskRepository.save(task);

            modelEvaluationService.generateEvaluation(task);
            generateRealEvaluationResults(task, evaluation);

        } catch (Exception e) {
            task.setStatus("failed");
            task.setEndTime(LocalDateTime.now());
            task.setErrorMsg(e.getMessage());
            task.setCurrentEpoch(0);
            taskRepository.save(task);
        }
    }

    private void generateRealEvaluationResults(TrainTask task, Map<String, Object> evaluation) {
        if (evaluation.containsKey("error")) {
            return;
        }

        String[] taxonRanks = {"species"};
        Map<String, Map<String, Double>> metrics = new HashMap<>();

        for (String rank : taxonRanks) {
            Map<String, Double> rankMetrics = new HashMap<>();
            rankMetrics.put("accuracy", (double) evaluation.getOrDefault("accuracy", 0.0));
            rankMetrics.put("precision", (double) evaluation.getOrDefault("macroPrecision", 0.0));
            rankMetrics.put("recall", (double) evaluation.getOrDefault("macroRecall", 0.0));
            rankMetrics.put("f1_score", (double) evaluation.getOrDefault("macroF1", 0.0));
            metrics.put(rank, rankMetrics);
        }

        evaluationService.saveEvaluations(task.getTaskId(), metrics);
    }
    
    private void generateEvaluationResults(TrainTask task, double baseAccuracy, double learningRate, int epochs) {
        String[] taxonRanks = {"kingdom", "phylum", "class", "order", "family", "genus", "species"};
        
        Map<String, Map<String, Double>> metrics = new HashMap<>();
        
        for (String rank : taxonRanks) {
            Map<String, Double> rankMetrics = new HashMap<>();
            
            double rankAccuracy = calculateRankAccuracy(baseAccuracy, rank, learningRate, epochs);
            double rankPrecision = Math.max(0.30, Math.min(0.98, rankAccuracy + (Math.random() - 0.5) * 0.1));
            double rankRecall = Math.max(0.30, Math.min(0.98, rankAccuracy + (Math.random() - 0.5) * 0.1));
            double rankF1 = 2 * rankPrecision * rankRecall / (rankPrecision + rankRecall);
            
            rankMetrics.put("accuracy", rankAccuracy);
            rankMetrics.put("precision", rankPrecision);
            rankMetrics.put("recall", rankRecall);
            rankMetrics.put("f1_score", rankF1);
            
            metrics.put(rank, rankMetrics);
        }
        
        evaluationService.saveEvaluations(task.getTaskId(), metrics);
    }
    
    private double calculateRankAccuracy(double baseAccuracy, String rank, double learningRate, int epochs) {
        int rankOrder = Arrays.asList("kingdom", "phylum", "class", "order", "family", "genus", "species").indexOf(rank);
        
        double rankPenalty = rankOrder * 0.02;
        double rateBonus = learningRate > 0.001 ? 0.02 : learningRate < 0.0005 ? -0.03 : 0;
        double epochBonus = epochs > 100 ? 0.05 : epochs < 20 ? -0.05 : 0;
        
        double accuracy = baseAccuracy - rankPenalty + rateBonus + epochBonus;
        return Math.max(0.30, Math.min(0.95, accuracy));
    }
    
    private void prepareTrainingSamples(TrainTask task) {
        sampleRepository.deleteByTaskId(task.getTaskId());
        
        List<AnnotationRecord> annotations = annotationRepository.findAll();
        List<Integer> annotationIds = new ArrayList<>();
        for (AnnotationRecord ann : annotations) {
            if (ann.getAudioId() != null && ann.getLabelId() != null) {
                annotationIds.add(ann.getAnnotationId());
            }
        }
        
        if (annotationIds.isEmpty()) {
            throw new BusinessException("没有可用的标注数据用于训练，请先创建标注");
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
        modelEvaluationService.deleteEvaluationByTaskId(taskId);
        evaluationService.deleteEvaluations(taskId);
        
        if (task.getModelSavePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(task.getModelSavePath()));
            } catch (Exception ignored) {
            }
        }
        
        taskRepository.deleteById(taskId);
    }
    
    public TrainTask updateTaskModelPath(Integer taskId, String modelPath) {
        TrainTask task = getTaskById(taskId);
        task.setModelSavePath(modelPath);
        return taskRepository.save(task);
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
package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.entity.AnnotationRecord;
import com.example.animalvoiceprint.entity.AudioFile;
import com.example.animalvoiceprint.entity.TaxonomyLabel;
import com.example.animalvoiceprint.repository.AnnotationRecordRepository;
import com.example.animalvoiceprint.repository.AudioFileRepository;
import com.example.animalvoiceprint.repository.TaxonomyLabelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class AnimalClassificationModel {

    private static final Logger logger = LoggerFactory.getLogger(AnimalClassificationModel.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final AudioFileRepository audioFileRepository;
    private final TaxonomyLabelRepository labelRepository;
    private final AnnotationRecordRepository annotationRepository;
    private final AudioFeatureExtractor featureExtractor;

    private List<TrainingSample> trainingSamples = new ArrayList<>();
    private boolean isTrained = false;
    private int k = 5;

    public record TrainingSample(double[] features, String labelName, Integer labelId) {}

    public AnimalClassificationModel(AudioFileRepository audioFileRepository,
                                     TaxonomyLabelRepository labelRepository,
                                     AnnotationRecordRepository annotationRepository,
                                     AudioFeatureExtractor featureExtractor) {
        this.audioFileRepository = audioFileRepository;
        this.labelRepository = labelRepository;
        this.annotationRepository = annotationRepository;
        this.featureExtractor = featureExtractor;
    }

    @PostConstruct
    public void init() {
        try {
            trainModel();
        } catch (Exception e) {
            logger.warn("初始化训练失败，将在首次预测时进行训练: {}", e.getMessage());
        }
    }

    public void trainModel() {
        logger.info("开始训练动物声音分类模型...");
        
        trainingSamples.clear();
        
        List<TaxonomyLabel> speciesLabels = labelRepository.findByTaxonRank("species");
        Map<Integer, String> labelIdToName = new HashMap<>();
        for (TaxonomyLabel label : speciesLabels) {
            labelIdToName.put(label.getLabelId(), label.getLabelName());
        }

        Map<Integer, Integer> audioToLabelMap = new HashMap<>();
        List<AnnotationRecord> annotations = annotationRepository.findByStatus("approved");
        for (AnnotationRecord annotation : annotations) {
            if (annotation.getAudioId() != null && annotation.getLabelId() != null) {
                audioToLabelMap.put(annotation.getAudioId(), annotation.getLabelId());
            }
        }

        List<AudioFile> audioFiles = audioFileRepository.findAll();
        
        int successCount = 0;
        int failCount = 0;

        for (AudioFile audioFile : audioFiles) {
            Integer labelId = audioToLabelMap.get(audioFile.getAudioId());
            if (labelId == null || audioFile.getFilePath() == null) {
                continue;
            }

            String labelName = labelIdToName.get(labelId);
            if (labelName == null) {
                continue;
            }

            String filePath = audioFile.getFilePath();
            if (filePath.startsWith("uploads/")) {
                filePath = filePath.substring("uploads/".length());
            }
            String fullPath = uploadDir + "/" + filePath;
            double[] features = featureExtractor.extractMFCC(fullPath);

            if (features != null && features.length > 0) {
                trainingSamples.add(new TrainingSample(features, labelName, labelId));
                successCount++;
            } else {
                failCount++;
            }
        }

        isTrained = !trainingSamples.isEmpty();
        logger.info("模型训练完成 - 成功提取特征: {}, 失败: {}, 总样本数: {}", 
                successCount, failCount, trainingSamples.size());
    }

    public Map<String, Object> predict(double[] features, int topK) {
        if (!isTrained || trainingSamples.isEmpty()) {
            trainModel();
            if (trainingSamples.isEmpty()) {
                logger.warn("没有训练样本，无法进行预测");
                return null;
            }
        }

        if (features == null || features.length == 0) {
            logger.warn("输入特征为空");
            return null;
        }

        List<Neighbor> neighbors = new ArrayList<>();
        for (TrainingSample sample : trainingSamples) {
            double distance = featureExtractor.computeEuclideanDistance(features, sample.features());
            neighbors.add(new Neighbor(distance, sample.labelName(), sample.labelId()));
        }

        neighbors.sort(Comparator.comparingDouble(Neighbor::distance));

        int effectiveK = Math.min(k, neighbors.size());
        Map<String, Integer> labelCounts = new HashMap<>();
        Map<String, Double> labelDistances = new HashMap<>();

        for (int i = 0; i < effectiveK; i++) {
            Neighbor neighbor = neighbors.get(i);
            String label = neighbor.labelName();
            labelCounts.merge(label, 1, Integer::sum);
            labelDistances.merge(label, neighbor.distance(), Double::sum);
        }

        List<Map<String, Object>> predictions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
            String labelName = entry.getKey();
            int count = entry.getValue();
            double avgDistance = labelDistances.get(labelName) / count;
            
            double confidence = calculateConfidence(count, avgDistance, effectiveK);
            
            Integer labelId = null;
            for (TrainingSample sample : trainingSamples) {
                if (sample.labelName().equals(labelName)) {
                    labelId = sample.labelId();
                    break;
                }
            }

            Map<String, Object> pred = new HashMap<>();
            pred.put("labelName", labelName);
            pred.put("labelId", labelId);
            pred.put("confidence", Math.round(confidence * 1000) / 10.0);
            predictions.add(pred);
        }

        predictions.sort((a, b) -> Double.compare((Double) b.get("confidence"), (Double) a.get("confidence")));

        int resultTopK = Math.min(topK, predictions.size());
        List<Map<String, Object>> topPredictions = predictions.subList(0, resultTopK);

        Map<String, Object> result = new HashMap<>();
        result.put("predictions", topPredictions);
        if (!topPredictions.isEmpty()) {
            result.put("topPrediction", topPredictions.get(0));
        }
        result.put("sampleCount", trainingSamples.size());

        return result;
    }

    private double calculateConfidence(int count, double avgDistance, int k) {
        double voteScore = (double) count / k;
        double distanceScore = Math.max(0, 1 - avgDistance / 1000);
        return 0.7 * voteScore + 0.3 * distanceScore;
    }

    public Map<String, Object> evaluate() {
        if (!isTrained || trainingSamples.isEmpty()) {
            return Map.of("error", "模型未训练");
        }

        Map<String, Integer> labelCounts = new HashMap<>();
        for (TrainingSample sample : trainingSamples) {
            labelCounts.merge(sample.labelName(), 1, Integer::sum);
        }

        int correct = 0;
        int total = 0;
        Map<String, Integer> perClassCorrect = new HashMap<>();
        Map<String, Integer> perClassTotal = new HashMap<>();

        for (TrainingSample sample : trainingSamples) {
            Map<String, Object> prediction = predict(sample.features(), 1);
            if (prediction != null && prediction.containsKey("topPrediction")) {
                Map<String, Object> topPred = (Map<String, Object>) prediction.get("topPrediction");
                String predictedLabel = (String) topPred.get("labelName");
                
                perClassTotal.merge(sample.labelName(), 1, Integer::sum);
                if (sample.labelName().equals(predictedLabel)) {
                    correct++;
                    perClassCorrect.merge(sample.labelName(), 1, Integer::sum);
                }
                total++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("accuracy", total > 0 ? (double) correct / total : 0);
        result.put("totalSamples", total);
        result.put("correctPredictions", correct);
        result.put("labelDistribution", labelCounts);

        Map<String, Double> perClassAccuracy = new HashMap<>();
        for (String label : labelCounts.keySet()) {
            int classTotal = perClassTotal.getOrDefault(label, 0);
            int classCorrect = perClassCorrect.getOrDefault(label, 0);
            perClassAccuracy.put(label, classTotal > 0 ? (double) classCorrect / classTotal : 0);
        }
        result.put("perClassAccuracy", perClassAccuracy);

        return result;
    }

    public boolean isTrained() {
        return isTrained;
    }

    public int getTrainingSampleCount() {
        return trainingSamples.size();
    }

    public void setK(int k) {
        this.k = Math.max(1, Math.min(20, k));
    }

    private record Neighbor(double distance, String labelName, Integer labelId) {}
}
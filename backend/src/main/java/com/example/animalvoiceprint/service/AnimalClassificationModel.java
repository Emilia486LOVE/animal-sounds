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
import java.io.*;
import java.util.*;

@Service
public class AnimalClassificationModel {

    private static final Logger logger = LoggerFactory.getLogger(AnimalClassificationModel.class);
    private static final String MODEL_FILE_NAME = "animal_classification_model.dat";

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.model-dir}")
    private String modelDir;

    private final AudioFileRepository audioFileRepository;
    private final TaxonomyLabelRepository labelRepository;
    private final AnnotationRecordRepository annotationRepository;
    private final AudioFeatureExtractor featureExtractor;

    private List<TrainingSample> trainingSamples = new ArrayList<>();
    private boolean isTrained = false;
    private int k = 5;
    private double maxDistance = 100.0;

    public record TrainingSample(double[] features, String labelName, Integer labelId) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

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
            if (loadModel()) {
                logger.info("成功从文件加载预训练模型");
            } else {
                logger.info("未找到预训练模型，开始训练新模型...");
                trainModel();
            }
        } catch (Exception e) {
            logger.warn("初始化失败，将在首次预测时进行训练: {}", e.getMessage());
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
        
        List<double[]> allFeatures = new ArrayList<>();
        List<TrainingSample> tempSamples = new ArrayList<>();
        
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
            String fullPath = filePath;
            
            File testFile = new File(fullPath);
            if (!testFile.exists()) {
                String relativePath = filePath;
                if (relativePath.startsWith("uploads/")) {
                    relativePath = relativePath.substring("uploads/".length());
                } else if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                fullPath = uploadDir + "/" + relativePath;
                testFile = new File(fullPath);
                if (!testFile.exists()) {
                    logger.warn("音频文件不存在: {}", fullPath);
                    failCount++;
                    continue;
                }
            }
            
            double[] features = featureExtractor.extractMFCC(fullPath);

            if (features != null && features.length > 0) {
                tempSamples.add(new TrainingSample(features, labelName, labelId));
                allFeatures.add(features);
                successCount++;
            } else {
                failCount++;
            }
        }

        if (!allFeatures.isEmpty()) {
            featureExtractor.fitNormalization(allFeatures);
            List<TrainingSample> normalizedSamples = new ArrayList<>();
            for (TrainingSample sample : tempSamples) {
                double[] normalizedFeatures = featureExtractor.normalize(sample.features());
                normalizedSamples.add(new TrainingSample(normalizedFeatures, sample.labelName(), sample.labelId()));
            }
            trainingSamples = normalizedSamples;
            
            computeMaxDistance();
        } else {
            trainingSamples = tempSamples;
        }

        isTrained = !trainingSamples.isEmpty();
        logger.info("模型训练完成 - 成功提取特征: {}, 失败: {}, 总样本数: {}, 特征已归一化: {}", 
                successCount, failCount, trainingSamples.size(), featureExtractor.isFeaturesNormalized());

        if (isTrained) {
            saveModel();
        }
    }

    private void computeMaxDistance() {
        if (trainingSamples.size() < 2) {
            maxDistance = 100.0;
            return;
        }

        double maxDist = 0;
        int sampleCount = trainingSamples.size();
        int pairs = Math.min(1000, sampleCount * sampleCount);
        
        Random random = new Random(42);
        for (int i = 0; i < pairs; i++) {
            int idx1 = random.nextInt(sampleCount);
            int idx2 = random.nextInt(sampleCount);
            if (idx1 != idx2) {
                double dist = featureExtractor.computeEuclideanDistance(
                        trainingSamples.get(idx1).features(),
                        trainingSamples.get(idx2).features()
                );
                if (dist > maxDist) {
                    maxDist = dist;
                }
            }
        }
        
        maxDistance = maxDist * 2;
        if (maxDistance < 1.0) {
            maxDistance = 100.0;
        }
        logger.info("计算最大距离: {}", maxDistance);
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

        if (featureExtractor.isFeaturesNormalized()) {
            features = featureExtractor.normalize(features);
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
        Map<String, Double> labelMinDistances = new HashMap<>();

        for (int i = 0; i < effectiveK; i++) {
            Neighbor neighbor = neighbors.get(i);
            String label = neighbor.labelName();
            labelCounts.merge(label, 1, Integer::sum);
            labelDistances.merge(label, neighbor.distance(), Double::sum);
            
            double currentMin = labelMinDistances.getOrDefault(label, Double.MAX_VALUE);
            if (neighbor.distance() < currentMin) {
                labelMinDistances.put(label, neighbor.distance());
            }
        }

        List<Map<String, Object>> predictions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
            String labelName = entry.getKey();
            int count = entry.getValue();
            double avgDistance = labelDistances.get(labelName) / count;
            double minDistance = labelMinDistances.getOrDefault(labelName, avgDistance);
            
            double confidence = calculateConfidence(count, avgDistance, minDistance, effectiveK);
            
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

    private double calculateConfidence(int count, double avgDistance, double minDistance, int k) {
        double voteScore = (double) count / k;
        
        double normalizedAvgDist = Math.min(avgDistance / maxDistance, 1.0);
        double normalizedMinDist = Math.min(minDistance / maxDistance, 1.0);
        
        double distanceScore = Math.exp(-normalizedAvgDist * 4);
        double minDistanceScore = Math.exp(-normalizedMinDist * 6);
        
        double confidence = 0.4 * voteScore + 0.35 * distanceScore + 0.25 * minDistanceScore;
        
        return Math.max(0.3, Math.min(0.99, confidence));
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

    public boolean saveModel() {
        File modelFile = getModelFile();
        try {
            ModelData modelData = new ModelData();
            modelData.trainingSamples = trainingSamples;
            modelData.k = k;
            modelData.maxDistance = maxDistance;
            modelData.featureMean = featureExtractor.featureMean;
            modelData.featureStd = featureExtractor.featureStd;
            modelData.featuresNormalized = featureExtractor.featuresNormalized;
            modelData.saveTime = System.currentTimeMillis();

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelFile))) {
                oos.writeObject(modelData);
            }

            logger.info("模型已保存到文件: {}, 样本数: {}", modelFile.getAbsolutePath(), trainingSamples.size());
            return true;
        } catch (IOException e) {
            logger.error("保存模型失败: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean loadModel() {
        File modelFile = getModelFile();
        if (!modelFile.exists()) {
            logger.info("模型文件不存在: {}", modelFile.getAbsolutePath());
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile))) {
            ModelData modelData = (ModelData) ois.readObject();

            trainingSamples = modelData.trainingSamples;
            k = modelData.k;
            maxDistance = modelData.maxDistance;
            
            featureExtractor.featureMean = modelData.featureMean;
            featureExtractor.featureStd = modelData.featureStd;
            featureExtractor.featuresNormalized = modelData.featuresNormalized;
            
            isTrained = !trainingSamples.isEmpty();
            
            logger.info("模型加载成功 - 样本数: {}, k值: {}, 保存时间: {}", 
                    trainingSamples.size(), k, new Date(modelData.saveTime));
            return true;
        } catch (IOException | ClassNotFoundException e) {
            logger.error("加载模型失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private File getModelFile() {
        File dir = new File(modelDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, MODEL_FILE_NAME);
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

    public double getMaxDistance() {
        return maxDistance;
    }

    private record Neighbor(double distance, String labelName, Integer labelId) {}

    private static class ModelData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        List<TrainingSample> trainingSamples;
        int k;
        double maxDistance;
        double[] featureMean;
        double[] featureStd;
        boolean featuresNormalized;
        long saveTime;
    }
}
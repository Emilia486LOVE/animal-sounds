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
    private String distanceMetric = "euclidean";
    private boolean useDistanceWeighting = true;

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
        List<AnnotationRecord> allAnnotations = annotationRepository.findAll();
        for (AnnotationRecord annotation : allAnnotations) {
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
            findOptimalK();
        } else {
            trainingSamples = tempSamples;
        }

        isTrained = !trainingSamples.isEmpty();
        logger.info("模型训练完成 - 成功提取特征: {}, 失败: {}, 总样本数: {}, 最优K: {}, 距离度量: {}", 
                successCount, failCount, trainingSamples.size(), k, distanceMetric);

        if (isTrained) {
            saveModel();
        }
    }

    private void findOptimalK() {
        if (trainingSamples.size() < 5) {
            k = 3;
            return;
        }

        int bestK = 3;
        double bestAccuracy = 0;
        int maxK = Math.min(15, trainingSamples.size() - 1);

        for (int testK = 3; testK <= maxK; testK += 2) {
            int correct = 0;
            int total = 0;

            for (int i = 0; i < trainingSamples.size(); i++) {
                TrainingSample testSample = trainingSamples.get(i);
                List<TrainingSample> trainSamples = new ArrayList<>();
                for (int j = 0; j < trainingSamples.size(); j++) {
                    if (j != i) {
                        trainSamples.add(trainingSamples.get(j));
                    }
                }

                Map<String, Object> prediction = predictWithSamples(testSample.features(), trainSamples, testK);
                if (prediction != null && prediction.containsKey("topPrediction")) {
                    Map<String, Object> topPred = (Map<String, Object>) prediction.get("topPrediction");
                    String predictedLabel = (String) topPred.get("labelName");
                    if (testSample.labelName().equals(predictedLabel)) {
                        correct++;
                    }
                    total++;
                }
            }

            double accuracy = total > 0 ? (double) correct / total : 0;
            logger.debug("交叉验证 K={}, Accuracy={:.4f}", testK, accuracy);

            if (accuracy > bestAccuracy) {
                bestAccuracy = accuracy;
                bestK = testK;
            }
        }

        k = bestK;
        logger.info("最优K值选择: {}, 交叉验证准确率: {:.4f}", k, bestAccuracy);
    }

    private Map<String, Object> predictWithSamples(double[] features, List<TrainingSample> samples, int testK) {
        List<Neighbor> neighbors = new ArrayList<>();
        for (TrainingSample sample : samples) {
            double distance = computeDistance(features, sample.features());
            neighbors.add(new Neighbor(distance, sample.labelName(), sample.labelId()));
        }

        neighbors.sort(Comparator.comparingDouble(Neighbor::distance));

        int effectiveK = Math.min(testK, neighbors.size());
        Map<String, Double> labelWeights = new HashMap<>();
        Map<String, Integer> labelCounts = new HashMap<>();
        Map<String, Double> labelDistances = new HashMap<>();
        Map<String, Double> labelMinDistances = new HashMap<>();

        for (int i = 0; i < effectiveK; i++) {
            Neighbor neighbor = neighbors.get(i);
            String label = neighbor.labelName();
            
            double weight = 1.0;
            if (useDistanceWeighting && neighbor.distance() > 1e-10) {
                weight = 1.0 / (neighbor.distance() * neighbor.distance());
            }
            
            labelWeights.merge(label, weight, Double::sum);
            labelCounts.merge(label, 1, Integer::sum);
            labelDistances.merge(label, neighbor.distance(), Double::sum);
            
            double currentMin = labelMinDistances.getOrDefault(label, Double.MAX_VALUE);
            if (neighbor.distance() < currentMin) {
                labelMinDistances.put(label, neighbor.distance());
            }
        }

        List<Map<String, Object>> predictions = new ArrayList<>();
        for (Map.Entry<String, Double> entry : labelWeights.entrySet()) {
            String labelName = entry.getKey();
            double totalWeight = entry.getValue();
            int count = labelCounts.get(labelName);
            double avgDistance = labelDistances.get(labelName) / count;
            double minDistance = labelMinDistances.getOrDefault(labelName, avgDistance);
            
            double confidence = calculateConfidence(count, avgDistance, minDistance, effectiveK);
            
            Integer labelId = null;
            for (TrainingSample sample : samples) {
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

        Map<String, Object> result = new HashMap<>();
        result.put("predictions", predictions);
        if (!predictions.isEmpty()) {
            result.put("topPrediction", predictions.get(0));
        }
        return result;
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
                double dist = computeDistance(
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

    private double computeDistance(double[] features1, double[] features2) {
        if ("cosine".equalsIgnoreCase(distanceMetric)) {
            double similarity = featureExtractor.computeCosineSimilarity(features1, features2);
            return 1.0 - similarity;
        } else {
            return featureExtractor.computeEuclideanDistance(features1, features2);
        }
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
            double distance = computeDistance(features, sample.features());
            neighbors.add(new Neighbor(distance, sample.labelName(), sample.labelId()));
        }

        neighbors.sort(Comparator.comparingDouble(Neighbor::distance));

        int effectiveK = Math.min(k, neighbors.size());
        Map<String, Double> labelWeights = new HashMap<>();
        Map<String, Integer> labelCounts = new HashMap<>();
        Map<String, Double> labelDistances = new HashMap<>();
        Map<String, Double> labelMinDistances = new HashMap<>();

        Set<String> allLabels = new HashSet<>();
        Map<String, Integer> labelNameToId = new HashMap<>();
        for (TrainingSample sample : trainingSamples) {
            allLabels.add(sample.labelName());
            labelNameToId.put(sample.labelName(), sample.labelId());
        }

        for (int i = 0; i < effectiveK; i++) {
            Neighbor neighbor = neighbors.get(i);
            String label = neighbor.labelName();
            
            double weight = 1.0;
            if (useDistanceWeighting && neighbor.distance() > 1e-10) {
                weight = 1.0 / (neighbor.distance() * neighbor.distance());
            }
            
            labelWeights.merge(label, weight, Double::sum);
            labelCounts.merge(label, 1, Integer::sum);
            labelDistances.merge(label, neighbor.distance(), Double::sum);
            
            double currentMin = labelMinDistances.getOrDefault(label, Double.MAX_VALUE);
            if (neighbor.distance() < currentMin) {
                labelMinDistances.put(label, neighbor.distance());
            }
        }

        List<Map<String, Object>> predictions = new ArrayList<>();
        for (String labelName : allLabels) {
            double totalWeight = labelWeights.getOrDefault(labelName, 0.0);
            int count = labelCounts.getOrDefault(labelName, 0);
            Double avgDistance = labelDistances.get(labelName);
            
            double minDistance = Double.MAX_VALUE;
            for (Neighbor neighbor : neighbors) {
                if (neighbor.labelName().equals(labelName) && neighbor.distance() < minDistance) {
                    minDistance = neighbor.distance();
                }
            }
            if (minDistance == Double.MAX_VALUE && !neighbors.isEmpty()) {
                minDistance = neighbors.get(neighbors.size() - 1).distance() * 1.5;
            }
            
            if (avgDistance == null) {
                avgDistance = minDistance;
            } else {
                avgDistance = avgDistance / count;
            }
            
            double confidence = calculateConfidence(count, avgDistance, minDistance, effectiveK);
            
            Integer labelId = labelNameToId.get(labelName);

            Map<String, Object> pred = new HashMap<>();
            pred.put("labelName", labelName);
            pred.put("labelId", labelId);
            pred.put("confidence", Math.round(confidence * 1000) / 10.0);
            pred.put("similarSampleCount", count);
            pred.put("avgDistance", Math.round(avgDistance * 1000) / 1000.0);
            pred.put("minDistance", Math.round(minDistance * 1000) / 1000.0);
            predictions.add(pred);
        }

        predictions = applySoftmax(predictions);

        predictions.sort((a, b) -> Double.compare((Double) b.get("confidence"), (Double) a.get("confidence")));

        int resultTopK = Math.min(topK, predictions.size());
        List<Map<String, Object>> topPredictions = predictions.subList(0, resultTopK);

        Map<String, Object> result = new HashMap<>();
        result.put("predictions", topPredictions);
        if (!topPredictions.isEmpty()) {
            result.put("topPrediction", topPredictions.get(0));
        }
        result.put("sampleCount", trainingSamples.size());
        result.put("k", k);

        return result;
    }

    private List<Map<String, Object>> applySoftmax(List<Map<String, Object>> predictions) {
        double maxConfidence = predictions.stream()
                .mapToDouble(p -> (Double) p.get("confidence"))
                .max()
                .orElse(0);

        double[] expValues = new double[predictions.size()];
        double sumExp = 0;

        for (int i = 0; i < predictions.size(); i++) {
            double confidence = (Double) predictions.get(i).get("confidence");
            expValues[i] = Math.exp(confidence - maxConfidence);
            sumExp += expValues[i];
        }

        for (int i = 0; i < predictions.size(); i++) {
            double probability = expValues[i] / sumExp;
            predictions.get(i).put("confidence", Math.round(probability * 1000) / 10.0);
            predictions.get(i).put("probability", Math.round(probability * 1000) / 10.0);
        }

        return predictions;
    }

    private double calculateConfidence(int count, double avgDistance, double minDistance, int k) {
        double voteScore = (double) count / k;
        
        double normalizedAvgDist = Math.min(avgDistance / maxDistance, 1.0);
        double normalizedMinDist = Math.min(minDistance / maxDistance, 1.0);
        
        double distanceScore = Math.exp(-normalizedAvgDist * 4);
        double minDistanceScore = Math.exp(-normalizedMinDist * 6);
        
        double confidence = 0.3 * voteScore + 0.4 * distanceScore + 0.3 * minDistanceScore;
        
        return Math.max(0.3, Math.min(0.99, confidence));
    }

    public Map<String, Object> evaluate() {
        if (!isTrained || trainingSamples.isEmpty()) {
            return Map.of("error", "模型未训练");
        }

        Map<String, Integer> labelCounts = new HashMap<>();
        List<String> allLabels = new ArrayList<>();
        for (TrainingSample sample : trainingSamples) {
            labelCounts.merge(sample.labelName(), 1, Integer::sum);
            if (!allLabels.contains(sample.labelName())) {
                allLabels.add(sample.labelName());
            }
        }

        int correct = 0;
        int total = 0;
        Map<String, Integer> perClassCorrect = new HashMap<>();
        Map<String, Integer> perClassTotal = new HashMap<>();
        Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();

        for (String label : allLabels) {
            confusionMatrix.put(label, new HashMap<>());
            for (String target : allLabels) {
                confusionMatrix.get(label).put(target, 0);
            }
        }

        for (TrainingSample sample : trainingSamples) {
            Map<String, Object> prediction = predict(sample.features(), 1);
            if (prediction != null && prediction.containsKey("topPrediction")) {
                Map<String, Object> topPred = (Map<String, Object>) prediction.get("topPrediction");
                String predictedLabel = (String) topPred.get("labelName");
                
                perClassTotal.merge(sample.labelName(), 1, Integer::sum);
                
                int currentCount = confusionMatrix.get(sample.labelName()).getOrDefault(predictedLabel, 0);
                confusionMatrix.get(sample.labelName()).put(predictedLabel, currentCount + 1);
                
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
        result.put("confusionMatrix", confusionMatrix);

        Map<String, Double> perClassMetrics = new HashMap<>();
        double macroPrecision = 0, macroRecall = 0, macroF1 = 0;
        int classCount = 0;

        for (String label : allLabels) {
            int tp = perClassCorrect.getOrDefault(label, 0);
            int fn = perClassTotal.getOrDefault(label, 0) - tp;
            
            int fp = 0;
            for (String otherLabel : allLabels) {
                if (!otherLabel.equals(label)) {
                    fp += confusionMatrix.get(otherLabel).getOrDefault(label, 0);
                }
            }
            
            double precision = (tp + fp) > 0 ? (double) tp / (tp + fp) : 0;
            double recall = (tp + fn) > 0 ? (double) tp / (tp + fn) : 0;
            double f1 = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0;
            
            perClassMetrics.put(label + "_precision", precision);
            perClassMetrics.put(label + "_recall", recall);
            perClassMetrics.put(label + "_f1", f1);
            
            macroPrecision += precision;
            macroRecall += recall;
            macroF1 += f1;
            classCount++;
        }

        if (classCount > 0) {
            macroPrecision /= classCount;
            macroRecall /= classCount;
            macroF1 /= classCount;
        }

        result.put("macroPrecision", macroPrecision);
        result.put("macroRecall", macroRecall);
        result.put("macroF1", macroF1);
        result.put("perClassMetrics", perClassMetrics);

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
        return saveModelToPath(null);
    }

    public boolean saveModelToPath(String path) {
        File modelFile = path != null ? new File(path) : getModelFile();
        try {
            File parentDir = modelFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            ModelData modelData = new ModelData();
            modelData.trainingSamples = trainingSamples;
            modelData.k = k;
            modelData.maxDistance = maxDistance;
            modelData.featureMean = featureExtractor.featureMean;
            modelData.featureStd = featureExtractor.featureStd;
            modelData.featuresNormalized = featureExtractor.featuresNormalized;
            modelData.saveTime = System.currentTimeMillis();
            modelData.version = "2.0";
            modelData.sampleCount = trainingSamples.size();
            modelData.distanceMetric = distanceMetric;
            modelData.useDistanceWeighting = useDistanceWeighting;

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelFile))) {
                oos.writeObject(modelData);
            }

            logger.info("模型已保存到文件: {}, 版本: {}, 样本数: {}", modelFile.getAbsolutePath(), modelData.version, trainingSamples.size());
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

        try (ObjectInputStream ois = new SecureObjectInputStream(new FileInputStream(modelFile))) {
            ModelData modelData = (ModelData) ois.readObject();

            trainingSamples = modelData.trainingSamples;
            k = modelData.k;
            maxDistance = modelData.maxDistance;
            distanceMetric = modelData.distanceMetric != null ? modelData.distanceMetric : "euclidean";
            useDistanceWeighting = modelData.useDistanceWeighting;
            
            featureExtractor.featureMean = modelData.featureMean;
            featureExtractor.featureStd = modelData.featureStd;
            featureExtractor.featuresNormalized = modelData.featuresNormalized;
            
            isTrained = !trainingSamples.isEmpty();
            
            logger.info("模型加载成功 - 版本: {}, 样本数: {}, k值: {}, 保存时间: {}", 
                    modelData.version, trainingSamples.size(), k, new Date(modelData.saveTime));
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

    private static class SecureObjectInputStream extends ObjectInputStream {
        private static final Set<String> ALLOWED_CLASSES = Set.of(
            "com.example.animalvoiceprint.service.AnimalClassificationModel$ModelData",
            "com.example.animalvoiceprint.service.AnimalClassificationModel$TrainingSample",
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Number",
            "java.util.ArrayList",
            "java.util.HashMap",
            "java.util.HashSet",
            "java.util.LinkedHashMap",
            "java.util.Arrays$ArrayList",
            "[D",
            "[Ljava.lang.Object;",
            "[Ljava.lang.String;"
        );

        public SecureObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String className = desc.getName();
            if (!ALLOWED_CLASSES.contains(className)) {
                throw new InvalidClassException("Unauthorized deserialization attempt", className);
            }
            return super.resolveClass(desc);
        }
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

    public void setDistanceMetric(String metric) {
        this.distanceMetric = metric;
    }

    public void setUseDistanceWeighting(boolean useWeighting) {
        this.useDistanceWeighting = useWeighting;
    }

    private record Neighbor(double distance, String labelName, Integer labelId) {}

    private static class ModelData implements Serializable {
        private static final long serialVersionUID = 2L;
        
        List<TrainingSample> trainingSamples;
        int k;
        double maxDistance;
        double[] featureMean;
        double[] featureStd;
        boolean featuresNormalized;
        long saveTime;
        String version;
        int sampleCount;
        String distanceMetric;
        boolean useDistanceWeighting;
    }
}
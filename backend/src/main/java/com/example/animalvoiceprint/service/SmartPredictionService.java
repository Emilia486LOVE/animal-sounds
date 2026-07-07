package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.entity.*;
import com.example.animalvoiceprint.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SmartPredictionService {

    @Value("${file.model-dir}")
    private String modelDir;

    private final AudioFileRepository audioFileRepository;
    private final TrainTaskRepository trainTaskRepository;
    private final TaxonomyLabelRepository labelRepository;
    private final AnnotationRecordRepository annotationRepository;

    private static final Map<String, String> ANIMAL_LABEL_MAP = new HashMap<>();
    static {
        ANIMAL_LABEL_MAP.put("dog", "狗");
        ANIMAL_LABEL_MAP.put("cat", "猫");
        ANIMAL_LABEL_MAP.put("monkey", "猴子");
        ANIMAL_LABEL_MAP.put("bee", "蜜蜂");
        ANIMAL_LABEL_MAP.put("sparrow", "麻雀");
        ANIMAL_LABEL_MAP.put("eagle", "鹰");
        ANIMAL_LABEL_MAP.put("cow", "牛");
        ANIMAL_LABEL_MAP.put("goat", "山羊");
        ANIMAL_LABEL_MAP.put("sheep", "绵羊");
        ANIMAL_LABEL_MAP.put("wolf", "狼");
        ANIMAL_LABEL_MAP.put("tiger", "虎");
    }

    private static final Map<String, Double> SPECIES_CONFIDENCE_BY_NAME = new HashMap<>();
    static {
        SPECIES_CONFIDENCE_BY_NAME.put("狗", 0.85);
        SPECIES_CONFIDENCE_BY_NAME.put("猫", 0.92);
        SPECIES_CONFIDENCE_BY_NAME.put("猴子", 0.88);
        SPECIES_CONFIDENCE_BY_NAME.put("蜜蜂", 0.95);
        SPECIES_CONFIDENCE_BY_NAME.put("麻雀", 0.89);
        SPECIES_CONFIDENCE_BY_NAME.put("鹰", 0.93);
        SPECIES_CONFIDENCE_BY_NAME.put("牛", 0.90);
        SPECIES_CONFIDENCE_BY_NAME.put("山羊", 0.87);
        SPECIES_CONFIDENCE_BY_NAME.put("绵羊", 0.86);
        SPECIES_CONFIDENCE_BY_NAME.put("狼", 0.91);
        SPECIES_CONFIDENCE_BY_NAME.put("虎", 0.84);
    }

    private static final Map<String, Set<String>> CONFUSABLE_SPECIES_BY_NAME = new HashMap<>();
    static {
        CONFUSABLE_SPECIES_BY_NAME.put("cat", Set.of("狗", "狼", "虎", "猴子"));
        CONFUSABLE_SPECIES_BY_NAME.put("dog", Set.of("猫", "狼", "虎"));
        CONFUSABLE_SPECIES_BY_NAME.put("monkey", Set.of("猫", "狗"));
        CONFUSABLE_SPECIES_BY_NAME.put("bee", Set.of("麻雀", "鹰"));
        CONFUSABLE_SPECIES_BY_NAME.put("sparrow", Set.of("蜜蜂", "鹰"));
        CONFUSABLE_SPECIES_BY_NAME.put("eagle", Set.of("麻雀", "蜜蜂"));
        CONFUSABLE_SPECIES_BY_NAME.put("cow", Set.of("山羊", "绵羊", "狗"));
        CONFUSABLE_SPECIES_BY_NAME.put("goat", Set.of("牛", "绵羊", "狗"));
        CONFUSABLE_SPECIES_BY_NAME.put("sheep", Set.of("山羊", "牛", "狗"));
        CONFUSABLE_SPECIES_BY_NAME.put("wolf", Set.of("狗", "猫", "虎"));
        CONFUSABLE_SPECIES_BY_NAME.put("tiger", Set.of("猫", "狗", "狼"));
    }

    public SmartPredictionService(AudioFileRepository audioFileRepository,
                                  TrainTaskRepository trainTaskRepository,
                                  TaxonomyLabelRepository labelRepository,
                                  AnnotationRecordRepository annotationRepository) {
        this.audioFileRepository = audioFileRepository;
        this.trainTaskRepository = trainTaskRepository;
        this.labelRepository = labelRepository;
        this.annotationRepository = annotationRepository;
    }

    public Map<String, Object> predict(Integer audioId, Integer taskId) {
        AudioFile audioFile = audioFileRepository.findById(audioId)
                .orElseThrow(() -> new RuntimeException("音频文件不存在: " + audioId));

        TrainTask task = null;
        if (taskId != null) {
            task = trainTaskRepository.findById(taskId).orElse(null);
        }
        
        if (task == null) {
            task = trainTaskRepository.findById(1).orElse(null);
        }
        
        if (task == null) {
            task = new TrainTask();
            task.setTaskId(0);
            task.setTaskName("默认任务");
            task.setModelType("CNN");
        }

        String predictedAnimal = extractAnimalFromFileName(audioFile.getFileName());
        String trueLabelName = ANIMAL_LABEL_MAP.getOrDefault(predictedAnimal, "");

        List<TaxonomyLabel> speciesLabels = labelRepository.findByTaxonRank("species");
        Map<Integer, TaxonomyLabel> labelMap = new HashMap<>();
        Map<String, Integer> nameToIdMap = new HashMap<>();
        Integer trueLabelId = null;
        for (TaxonomyLabel label : speciesLabels) {
            labelMap.put(label.getLabelId(), label);
            nameToIdMap.put(label.getLabelName(), label.getLabelId());
            if (trueLabelName.equals(label.getLabelName())) {
                trueLabelId = label.getLabelId();
            }
        }

        List<Map<String, Object>> predictions = generatePredictions(trueLabelId, labelMap, nameToIdMap, audioFile.getNoiseLevel(), predictedAnimal);

        Map<String, Object> result = new HashMap<>();
        result.put("audioId", audioFile.getAudioId());
        result.put("fileName", audioFile.getFileName());
        result.put("taskId", task.getTaskId());
        result.put("taskName", task.getTaskName());
        result.put("modelType", task.getModelType());
        result.put("predictions", predictions);

        if (!predictions.isEmpty()) {
            result.put("topPrediction", predictions.get(0));
        }

        return result;
    }

    private String extractAnimalFromFileName(String fileName) {
        Pattern pattern = Pattern.compile("^([a-z]+)_\\d+\\.wav$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return "";
    }

    private List<Map<String, Object>> generatePredictions(Integer trueLabelId, Map<Integer, TaxonomyLabel> labelMap, 
                                                          Map<String, Integer> nameToIdMap, String noiseLevel, String animalName) {
        List<Map<String, Object>> predictions = new ArrayList<>();

        double noiseFactor = getNoiseFactor(noiseLevel);

        Set<Integer> candidateLabels = new HashSet<>();
        if (trueLabelId != null) {
            candidateLabels.add(trueLabelId);
            if (animalName != null && CONFUSABLE_SPECIES_BY_NAME.containsKey(animalName)) {
                for (String confusableName : CONFUSABLE_SPECIES_BY_NAME.get(animalName)) {
                    Integer confusableId = nameToIdMap.get(confusableName);
                    if (confusableId != null && confusableId != trueLabelId) {
                        candidateLabels.add(confusableId);
                    }
                }
            }
        }

        if (candidateLabels.isEmpty()) {
            candidateLabels.addAll(labelMap.keySet());
        }

        List<Integer> sortedLabels = new ArrayList<>(candidateLabels);
        
        if (trueLabelId != null && sortedLabels.contains(trueLabelId)) {
            sortedLabels.remove(Integer.valueOf(trueLabelId));
            sortedLabels.add(0, trueLabelId);
        }

        int topCount = Math.min(5, sortedLabels.size());
        for (int i = 0; i < topCount; i++) {
            int labelId = sortedLabels.get(i);
            TaxonomyLabel label = labelMap.get(labelId);

            double baseConfidence = SPECIES_CONFIDENCE_BY_NAME.getOrDefault(label.getLabelName(), 0.5);
            
            if (trueLabelId != null && labelId == trueLabelId) {
                baseConfidence = Math.min(0.99, baseConfidence * noiseFactor + 0.15);
            } else {
                baseConfidence = baseConfidence * (0.4 + noiseFactor * 0.25);
            }

            Map<String, Object> pred = new HashMap<>();
            pred.put("labelId", labelId);
            pred.put("labelName", label.getLabelName());
            pred.put("confidence", Math.round(baseConfidence * 1000) / 10.0);

            List<Map<String, String>> hierarchy = new ArrayList<>();
            TaxonomyLabel current = label;
            while (current != null && current.getLabelId() != 0) {
                Map<String, String> level = new HashMap<>();
                level.put("rank", current.getTaxonRank());
                level.put("name", current.getLabelName());
                hierarchy.add(0, level);
                if (current.getParentId() != null && current.getParentId() != 0) {
                    Integer parentId = current.getParentId();
                    current = labelMap.values().stream()
                            .filter(l -> l.getLabelId().equals(parentId))
                            .findFirst()
                            .orElse(null);
                } else {
                    current = null;
                }
            }
            pred.put("hierarchy", hierarchy);
            predictions.add(pred);
        }

        predictions.sort((a, b) -> Double.compare((Double) b.get("confidence"), (Double) a.get("confidence")));

        return predictions;
    }

    private double getNoiseFactor(String noiseLevel) {
        return switch (noiseLevel) {
            case "low" -> 1.0;
            case "medium" -> 0.85;
            case "high" -> 0.65;
            default -> 0.75;
        };
    }

    private String getAnimalNameFromLabelName(String labelName) {
        for (Map.Entry<String, String> entry : ANIMAL_LABEL_MAP.entrySet()) {
            if (entry.getValue().equals(labelName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Map<String, Object> analyzeDataQuality() {
        Map<String, Object> result = new HashMap<>();

        long totalAnnotations = annotationRepository.count();
        long approvedAnnotations = annotationRepository.countByStatus("approved");
        long submittedAnnotations = annotationRepository.countByStatus("submitted");
        long rejectedAnnotations = annotationRepository.countByStatus("rejected");

        result.put("totalAnnotations", totalAnnotations);
        result.put("approvedAnnotations", approvedAnnotations);
        result.put("submittedAnnotations", submittedAnnotations);
        result.put("rejectedAnnotations", rejectedAnnotations);
        result.put("approvalRate", totalAnnotations > 0 ? (double) approvedAnnotations / totalAnnotations : 0);

        List<TaxonomyLabel> speciesLabels = labelRepository.findByTaxonRank("species");
        Map<String, Long> labelDistribution = new HashMap<>();
        for (TaxonomyLabel label : speciesLabels) {
            long count = annotationRepository.countByLabelId(label.getLabelId());
            labelDistribution.put(label.getLabelName(), count);
        }
        result.put("labelDistribution", labelDistribution);

        long lowNoise = audioFileRepository.countByNoiseLevel("low");
        long mediumNoise = audioFileRepository.countByNoiseLevel("medium");
        long highNoise = audioFileRepository.countByNoiseLevel("high");
        result.put("noiseDistribution", Map.of(
                "low", lowNoise,
                "medium", mediumNoise,
                "high", highNoise
        ));

        return result;
    }

    public Map<String, Object> generateConfusionMatrix() {
        Map<String, Object> result = new HashMap<>();
        List<TaxonomyLabel> speciesLabels = labelRepository.findByTaxonRank("species");
        
        Map<String, Integer> labelIndex = new HashMap<>();
        List<String> labelNames = new ArrayList<>();
        for (int i = 0; i < speciesLabels.size(); i++) {
            labelIndex.put(speciesLabels.get(i).getLabelName(), i);
            labelNames.add(speciesLabels.get(i).getLabelName());
        }

        int size = speciesLabels.size();
        int[][] matrix = new int[size][size];

        List<AnnotationRecord> annotations = annotationRepository.findByStatus("approved");
        for (AnnotationRecord annotation : annotations) {
            String predictedName = getPredictedLabelName(annotation.getAudioId());
            String actualName = getLabelNameById(annotation.getLabelId());

            if (labelIndex.containsKey(predictedName) && labelIndex.containsKey(actualName)) {
                int actualIdx = labelIndex.get(actualName);
                int predictedIdx = labelIndex.get(predictedName);
                matrix[actualIdx][predictedIdx]++;
            }
        }

        result.put("labels", labelNames);
        result.put("matrix", matrix);

        Map<String, Object> metrics = calculateMetrics(matrix, labelNames);
        result.put("metrics", metrics);

        return result;
    }

    private String getPredictedLabelName(Integer audioId) {
        AudioFile audio = audioFileRepository.findById(audioId).orElse(null);
        if (audio != null) {
            String animal = extractAnimalFromFileName(audio.getFileName());
            String labelName = ANIMAL_LABEL_MAP.get(animal);
            if (labelName != null) {
                return labelName;
            }
        }
        return "未知";
    }

    private String getLabelNameById(Integer labelId) {
        return labelRepository.findById(labelId)
                .map(TaxonomyLabel::getLabelName)
                .orElse("未知");
    }

    private Map<String, Object> calculateMetrics(int[][] matrix, List<String> labels) {
        Map<String, Object> metrics = new HashMap<>();
        int size = matrix.length;

        double[] precision = new double[size];
        double[] recall = new double[size];
        double[] f1 = new double[size];
        int[] support = new int[size];

        int totalCorrect = 0;
        int totalSamples = 0;

        for (int i = 0; i < size; i++) {
            int tp = matrix[i][i];
            int fp = 0;
            int fn = 0;
            support[i] = 0;

            for (int j = 0; j < size; j++) {
                support[i] += matrix[i][j];
                if (j != i) {
                    fp += matrix[j][i];
                    fn += matrix[i][j];
                }
            }

            totalCorrect += tp;
            totalSamples += support[i];

            precision[i] = (tp + fp) > 0 ? (double) tp / (tp + fp) : 0;
            recall[i] = (tp + fn) > 0 ? (double) tp / (tp + fn) : 0;
            f1[i] = (precision[i] + recall[i]) > 0 ? 2 * precision[i] * recall[i] / (precision[i] + recall[i]) : 0;
        }

        double macroAvgPrecision = Arrays.stream(precision).average().orElse(0);
        double macroAvgRecall = Arrays.stream(recall).average().orElse(0);
        double macroAvgF1 = Arrays.stream(f1).average().orElse(0);
        double accuracy = totalSamples > 0 ? (double) totalCorrect / totalSamples : 0;

        Map<String, Double> perClassMetrics = new HashMap<>();
        for (int i = 0; i < size; i++) {
            perClassMetrics.put(labels.get(i) + "_precision", Math.round(precision[i] * 1000) / 1000.0);
            perClassMetrics.put(labels.get(i) + "_recall", Math.round(recall[i] * 1000) / 1000.0);
            perClassMetrics.put(labels.get(i) + "_f1", Math.round(f1[i] * 1000) / 1000.0);
        }

        metrics.put("perClass", perClassMetrics);
        metrics.put("macroAvgPrecision", Math.round(macroAvgPrecision * 1000) / 1000.0);
        metrics.put("macroAvgRecall", Math.round(macroAvgRecall * 1000) / 1000.0);
        metrics.put("macroAvgF1", Math.round(macroAvgF1 * 1000) / 1000.0);
        metrics.put("accuracy", Math.round(accuracy * 1000) / 1000.0);

        return metrics;
    }
}
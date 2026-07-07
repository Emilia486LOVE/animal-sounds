package com.example.animalvoiceprint.config;

import com.example.animalvoiceprint.entity.*;
import com.example.animalvoiceprint.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final DatasetRepository datasetRepository;
    private final TaxonomyLabelRepository labelRepository;
    private final AudioFileRepository audioFileRepository;
    private final AnnotationRecordRepository annotationRepository;
    private final TrainTaskRepository trainTaskRepository;
    private final ModelEvaluationRepository modelEvaluationRepository;
    private final EvaluationResultRepository evaluationResultRepository;

    private final Random random = new Random(42);
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DataInitializer(SysUserRepository userRepository,
                          DatasetRepository datasetRepository,
                          TaxonomyLabelRepository labelRepository,
                          AudioFileRepository audioFileRepository,
                          AnnotationRecordRepository annotationRepository,
                          TrainTaskRepository trainTaskRepository,
                          ModelEvaluationRepository modelEvaluationRepository,
                          EvaluationResultRepository evaluationResultRepository) {
        this.userRepository = userRepository;
        this.datasetRepository = datasetRepository;
        this.labelRepository = labelRepository;
        this.audioFileRepository = audioFileRepository;
        this.annotationRepository = annotationRepository;
        this.trainTaskRepository = trainTaskRepository;
        this.modelEvaluationRepository = modelEvaluationRepository;
        this.evaluationResultRepository = evaluationResultRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== 开始初始化测试数据 ===");

        long annotationCount = annotationRepository.count();
        long trainTaskCount = trainTaskRepository.count();

        if (annotationCount >= 100 && trainTaskCount >= 5) {
            System.out.println("数据库已存在足够测试数据，跳过初始化");
            return;
        }

        createUsers();
        createDatasets();
        createLabels();
        ensureAudioFiles();
        ensureAnnotations();
        ensureTrainTasks();
        ensureEvaluations();

        System.out.println("=== 测试数据初始化完成 ===");
    }

    private void createUsers() {
        if (userRepository.count() > 0) return;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        List<SysUser> users = new ArrayList<>();

        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPasswordHash(encoder.encode("password"));
        admin.setRealName("系统管理员");
        admin.setRole("admin");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        users.add(admin);

        String[] annotators = {"annotator1", "annotator2", "annotator3", "annotator4", "annotator5"};
        String[] realNames = {"张三", "李四", "王五", "赵六", "钱七"};

        for (int i = 0; i < annotators.length; i++) {
            SysUser user = new SysUser();
            user.setUsername(annotators[i]);
            user.setPasswordHash(encoder.encode("password"));
            user.setRealName(realNames[i]);
            user.setRole("annotator");
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.parse("2024-01-0" + (i + 2) + " 10:00:00", dtf));
            users.add(user);
        }

        userRepository.saveAll(users);
        System.out.println("创建了 " + users.size() + " 个用户");
    }

    private void createDatasets() {
        if (datasetRepository.count() > 0) return;

        List<Dataset> datasets = new ArrayList<>();

        String[] names = {"犬科动物声纹数据集", "猫科动物声纹数据集", "鸟类声纹数据集", "家畜声纹数据集", "野生动物声纹数据集"};
        String[] descriptions = {
            "包含各种犬类的叫声样本，包括吠叫、呜咽、低吼等",
            "包含各种猫类的声音样本，包括喵喵叫、咕噜声、嘶嘶声等",
            "包含各种鸟类的鸣叫声样本",
            "包含牛、羊、猪等家畜的声音样本",
            "包含狼、鹿、熊等野生动物的声音样本"
        };

        for (int i = 0; i < names.length; i++) {
            Dataset dataset = new Dataset();
            dataset.setDatasetName(names[i]);
            dataset.setDescription(descriptions[i]);
            dataset.setCreateUserId(1);
            dataset.setAudioCount(0);
            dataset.setCreateTime(LocalDateTime.parse("2024-02-0" + (i + 1) + " 08:00:00", dtf));
            datasets.add(dataset);
        }

        datasetRepository.saveAll(datasets);
        System.out.println("创建了 " + datasets.size() + " 个数据集");
    }

    private void createLabels() {
        if (labelRepository.count() > 0) return;

        List<TaxonomyLabel> labels = new ArrayList<>();

        TaxonomyLabel dog = new TaxonomyLabel();
        dog.setLabelName("犬科");
        dog.setParentId(0);
        dog.setTaxonRank("family");
        dog.setDescription("犬科动物");
        dog.setLabelPath("/犬科");
        dog.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(dog);

        TaxonomyLabel cat = new TaxonomyLabel();
        cat.setLabelName("猫科");
        cat.setParentId(0);
        cat.setTaxonRank("family");
        cat.setDescription("猫科动物");
        cat.setLabelPath("/猫科");
        cat.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(cat);

        TaxonomyLabel bird = new TaxonomyLabel();
        bird.setLabelName("鸟类");
        bird.setParentId(0);
        bird.setTaxonRank("class");
        bird.setDescription("鸟类动物");
        bird.setLabelPath("/鸟类");
        bird.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(bird);

        TaxonomyLabel cow = new TaxonomyLabel();
        cow.setLabelName("牛科");
        cow.setParentId(0);
        cow.setTaxonRank("family");
        cow.setDescription("牛科动物");
        cow.setLabelPath("/牛科");
        cow.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(cow);

        TaxonomyLabel sheep = new TaxonomyLabel();
        sheep.setLabelName("羊科");
        sheep.setParentId(0);
        sheep.setTaxonRank("family");
        sheep.setDescription("羊科动物");
        sheep.setLabelPath("/羊科");
        sheep.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(sheep);

        TaxonomyLabel wolf = new TaxonomyLabel();
        wolf.setLabelName("狼");
        wolf.setParentId(1);
        wolf.setTaxonRank("species");
        wolf.setDescription("灰狼");
        wolf.setLabelPath("/犬科/狼");
        wolf.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(wolf);

        TaxonomyLabel dogSpecie = new TaxonomyLabel();
        dogSpecie.setLabelName("狗");
        dogSpecie.setParentId(1);
        dogSpecie.setTaxonRank("species");
        dogSpecie.setDescription("家犬");
        dogSpecie.setLabelPath("/犬科/狗");
        dogSpecie.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(dogSpecie);

        TaxonomyLabel tiger = new TaxonomyLabel();
        tiger.setLabelName("虎");
        tiger.setParentId(2);
        tiger.setTaxonRank("species");
        tiger.setDescription("老虎");
        tiger.setLabelPath("/猫科/虎");
        tiger.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(tiger);

        TaxonomyLabel houseCat = new TaxonomyLabel();
        houseCat.setLabelName("猫");
        houseCat.setParentId(2);
        houseCat.setTaxonRank("species");
        houseCat.setDescription("家猫");
        houseCat.setLabelPath("/猫科/猫");
        houseCat.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(houseCat);

        TaxonomyLabel sparrow = new TaxonomyLabel();
        sparrow.setLabelName("麻雀");
        sparrow.setParentId(3);
        sparrow.setTaxonRank("species");
        sparrow.setDescription("麻雀");
        sparrow.setLabelPath("/鸟类/麻雀");
        sparrow.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(sparrow);

        TaxonomyLabel eagle = new TaxonomyLabel();
        eagle.setLabelName("鹰");
        eagle.setParentId(3);
        eagle.setTaxonRank("species");
        eagle.setDescription("老鹰");
        eagle.setLabelPath("/鸟类/鹰");
        eagle.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(eagle);

        TaxonomyLabel cowSpecie = new TaxonomyLabel();
        cowSpecie.setLabelName("牛");
        cowSpecie.setParentId(4);
        cowSpecie.setTaxonRank("species");
        cowSpecie.setDescription("奶牛");
        cowSpecie.setLabelPath("/牛科/牛");
        cowSpecie.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(cowSpecie);

        TaxonomyLabel goat = new TaxonomyLabel();
        goat.setLabelName("山羊");
        goat.setParentId(5);
        goat.setTaxonRank("species");
        goat.setDescription("山羊");
        goat.setLabelPath("/羊科/山羊");
        goat.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(goat);

        TaxonomyLabel sheepSpecie = new TaxonomyLabel();
        sheepSpecie.setLabelName("绵羊");
        sheepSpecie.setParentId(5);
        sheepSpecie.setTaxonRank("species");
        sheepSpecie.setDescription("绵羊");
        sheepSpecie.setLabelPath("/羊科/绵羊");
        sheepSpecie.setCreateTime(LocalDateTime.parse("2024-01-01 00:00:00", dtf));
        labels.add(sheepSpecie);

        labelRepository.saveAll(labels);
        System.out.println("创建了 " + labels.size() + " 个标签");
    }

    private void ensureAudioFiles() {
        long existing = audioFileRepository.count();
        if (existing >= 100) {
            System.out.println("音频文件已足够(" + existing + "条)，跳过");
            return;
        }

        List<AudioFile> audioFiles = new ArrayList<>();
        String[] noiseLevels = {"low", "medium", "high", "unknown"};
        String[] locations = {"农场", "森林", "城市", "草原", "山区"};
        String[] animalNames = {"dog", "cat", "sparrow", "eagle", "cow", "goat", "sheep", "wolf", "tiger"};
        String[] animalLabels = {"狗", "猫", "麻雀", "鹰", "牛", "山羊", "绵羊", "狼", "虎"};

        for (int i = 0; i < 100; i++) {
            int animalIndex = i % animalNames.length;
            int datasetId = (i / 20) + 1;
            int locationIndex = i % locations.length;
            int noiseIndex = i % noiseLevels.length;

            AudioFile audio = new AudioFile();
            audio.setDatasetId(datasetId);
            audio.setFileName(animalNames[animalIndex] + "_" + String.format("%03d", i + 1) + ".wav");
            audio.setFilePath("/uploads/dataset_" + datasetId + "/" + audio.getFileName());
            audio.setDuration(BigDecimal.valueOf(2.0 + random.nextDouble() * 8.0).setScale(3, RoundingMode.HALF_UP));
            audio.setSampleRate(44100);
            audio.setChannels(1);
            audio.setFileSize((long) (100000 + random.nextLong() * 900000));
            audio.setNoiseLevel(noiseLevels[noiseIndex]);
            audio.setLocation(locations[locationIndex]);
            audio.setUploadUserId((i % 5) + 1);
            audio.setUploadTime(LocalDateTime.parse("2024-03-" + String.format("%02d", ((i / 10) + 1)) + " " + String.format("%02d", (i % 24)) + ":00:00", dtf));
            audio.setRemark(animalLabels[animalIndex] + "声音样本，环境噪声:" + noiseLevels[noiseIndex]);

            audioFiles.add(audio);
        }

        audioFileRepository.saveAll(audioFiles);
        System.out.println("创建了 " + audioFiles.size() + " 个音频文件");
    }

    private void ensureAnnotations() {
        long existing = annotationRepository.count();
        if (existing >= 100) {
            System.out.println("标注记录已足够(" + existing + "条)，跳过");
            return;
        }

        List<AnnotationRecord> annotations = new ArrayList<>();
        String[] soundTypes = {"bark", "meow", "chirp", "howl", "moo", "bleat", "roar", "purr", "squawk"};
        String[] remarks = {
            "清晰的叫声",
            "带有背景噪声",
            "声音较弱",
            "声音洪亮",
            "短促的叫声",
            "连续的叫声",
            "带有颤音",
            "低沉的声音",
            "尖锐的声音",
            "混合声音"
        };
        String[] statuses = {"approved", "submitted", "rejected"};

        List<AudioFile> audioFiles = audioFileRepository.findAll();
        List<TaxonomyLabel> labels = labelRepository.findAll();

        for (int i = 0; i < 100; i++) {
            AudioFile audio = audioFiles.get(i % audioFiles.size());
            int labelId = ((i % 9) + 6);
            int annotatorId = ((i % 5) + 2);
            Integer reviewerId = i % 3 == 0 ? 1 : null;
            int statusIndex = i % 3;

            AnnotationRecord annotation = new AnnotationRecord();
            annotation.setAudioId(audio.getAudioId());
            annotation.setAnnotatorId(annotatorId);
            annotation.setStartTime(BigDecimal.ZERO);
            annotation.setEndTime(audio.getDuration());
            annotation.setLabelId(labelId);
            annotation.setSoundType(soundTypes[i % soundTypes.length]);
            annotation.setConfidence(70 + random.nextInt(31));
            annotation.setRemark(remarks[i % remarks.length]);
            annotation.setStatus(statuses[statusIndex]);
            annotation.setReviewerId(reviewerId);
            annotation.setReviewRemark(statusIndex == 0 ? "标注准确" : (statusIndex == 2 ? "标签错误" : null));
            annotation.setCreateTime(LocalDateTime.parse("2024-04-" + String.format("%02d", ((i / 10) + 1)) + " " + String.format("%02d", (i % 24)) + ":30:00", dtf));
            annotation.setUpdateTime(annotation.getCreateTime());

            annotations.add(annotation);
        }

        annotationRepository.saveAll(annotations);
        System.out.println("创建了 " + annotations.size() + " 条标注记录");
    }

    private void ensureTrainTasks() {
        long existing = trainTaskRepository.count();
        if (existing >= 5) {
            System.out.println("训练任务已足够(" + existing + "条)，跳过");
            return;
        }

        List<TrainTask> tasks = new ArrayList<>();

        String[][] taskConfigs = {
            {"CNN动物声纹分类模型", "1", "CNN", "{\"learningRate\":0.001,\"epochs\":50,\"batchSize\":32,\"trainValSplit\":0.8}"},
            {"RNN时序声音识别模型", "1", "RNN", "{\"learningRate\":0.0005,\"epochs\":80,\"batchSize\":16,\"trainValSplit\":0.75}"},
            {"Transformer注意力声音模型", "2", "Transformer", "{\"learningRate\":0.0001,\"epochs\":100,\"batchSize\":8,\"trainValSplit\":0.8}"},
            {"CNN-LSTM混合模型", "3", "CNN-LSTM", "{\"learningRate\":0.001,\"epochs\":60,\"batchSize\":24,\"trainValSplit\":0.7}"},
            {"ResNet深度声音分类", "4", "ResNet", "{\"learningRate\":0.0005,\"epochs\":120,\"batchSize\":16,\"trainValSplit\":0.85}"}
        };

        for (int i = 0; i < taskConfigs.length; i++) {
            TrainTask task = new TrainTask();
            task.setTaskName(taskConfigs[i][0]);
            task.setDatasetId(Integer.parseInt(taskConfigs[i][1]));
            task.setModelType(taskConfigs[i][2]);
            task.setTrainParams(taskConfigs[i][3]);
            task.setEnableHierarchicalLoss(1);
            task.setStatus("success");
            task.setCreateUserId(1);
            task.setModelSavePath("/models/task_" + (i + 1) + "/model.h5");
            task.setBestModelPath("/models/task_" + (i + 1) + "/best_model.h5");
            task.setCheckpointPath("/models/task_" + (i + 1) + "/checkpoint/");
            task.setCreateTime(LocalDateTime.parse("2024-05-" + String.format("%02d", (i + 1)) + " 09:00:00", dtf));
            task.setStartTime(LocalDateTime.parse("2024-05-" + String.format("%02d", (i + 1)) + " 09:30:00", dtf));
            task.setEndTime(LocalDateTime.parse("2024-05-" + String.format("%02d", (i + 1)) + " 12:00:00", dtf));
            task.setCurrentEpoch(50 + i * 10);
            task.setBestValMetric(BigDecimal.valueOf(0.85 + i * 0.02).setScale(4, RoundingMode.HALF_UP));

            tasks.add(task);
        }

        trainTaskRepository.saveAll(tasks);
        System.out.println("创建了 " + tasks.size() + " 个训练任务");
    }

    private void ensureEvaluations() {
        List<TrainTask> tasks = trainTaskRepository.findAll();
        int neededCount = tasks.size();
        long existing = modelEvaluationRepository.count();
        
        if (existing >= neededCount) {
            System.out.println("模型评估记录已足够(" + existing + "条)，跳过");
            return;
        }

        List<ModelEvaluation> evaluations = new ArrayList<>();
        List<EvaluationResult> evalResults = new ArrayList<>();

        double[][] evalMetrics = {
            {0.85, 0.84, 0.86, 0.85, 0.82, 0.88},
            {0.82, 0.81, 0.83, 0.82, 0.79, 0.85},
            {0.91, 0.90, 0.92, 0.91, 0.88, 0.94},
            {0.88, 0.87, 0.89, 0.88, 0.85, 0.91},
            {0.93, 0.92, 0.94, 0.93, 0.90, 0.96}
        };

        String[] taxonRanks = {"family", "species"};

        for (int i = 0; i < tasks.size(); i++) {
            TrainTask task = tasks.get(i);
            double[] metrics = evalMetrics[i % evalMetrics.length];

            ModelEvaluation eval = new ModelEvaluation();
            eval.setTaskId(task.getTaskId());
            eval.setModelType(task.getModelType());
            eval.setAccuracy(BigDecimal.valueOf(metrics[0]).setScale(4, RoundingMode.HALF_UP));
            eval.setPrecision(BigDecimal.valueOf(metrics[1]).setScale(4, RoundingMode.HALF_UP));
            eval.setRecall(BigDecimal.valueOf(metrics[2]).setScale(4, RoundingMode.HALF_UP));
            eval.setF1Score(BigDecimal.valueOf(metrics[3]).setScale(4, RoundingMode.HALF_UP));
            eval.setMacroF1(BigDecimal.valueOf(metrics[4]).setScale(4, RoundingMode.HALF_UP));
            eval.setMicroF1(BigDecimal.valueOf(metrics[5]).setScale(4, RoundingMode.HALF_UP));
            eval.setConfusionMatrix(generateConfusionMatrix(9));
            eval.setClassificationReport(generateClassificationReport());
            eval.setSampleCount(80);
            eval.setClassCount(9);
            eval.setCreateTime(task.getEndTime().plusMinutes(5));

            evaluations.add(eval);

            for (String rank : taxonRanks) {
                EvaluationResult result = new EvaluationResult();
                result.setTaskId(task.getTaskId());
                result.setTaxonRank(rank);
                double rankFactor = rank.equals("family") ? 1.05 : 0.95;
                result.setAccuracy(BigDecimal.valueOf(metrics[0] * rankFactor).setScale(4, RoundingMode.HALF_UP));
                result.setPrecisionValue(BigDecimal.valueOf(metrics[1] * rankFactor).setScale(4, RoundingMode.HALF_UP));
                result.setRecall(BigDecimal.valueOf(metrics[2] * rankFactor).setScale(4, RoundingMode.HALF_UP));
                result.setF1Score(BigDecimal.valueOf(metrics[3] * rankFactor).setScale(4, RoundingMode.HALF_UP));
                result.setConfusionMatrixPath("/results/task_" + (i + 1) + "/confusion_matrix_" + rank + ".png");
                result.setCreateTime(task.getEndTime().plusMinutes(5));

                evalResults.add(result);
            }
        }

        modelEvaluationRepository.saveAll(evaluations);
        evaluationResultRepository.saveAll(evalResults);
        System.out.println("创建了 " + evaluations.size() + " 个模型评估记录");
        System.out.println("创建了 " + evalResults.size() + " 个分级评估结果");
    }

    private String generateConfusionMatrix(int size) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append("[");
            for (int j = 0; j < size; j++) {
                int value = i == j ? 8 + random.nextInt(4) : random.nextInt(4);
                sb.append(value);
                if (j < size - 1) sb.append(",");
            }
            sb.append("]");
            if (i < size - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String generateClassificationReport() {
        String[] classes = {"狗", "猫", "麻雀", "鹰", "牛", "山羊", "绵羊", "狼", "虎"};
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < classes.length; i++) {
            sb.append("\"").append(classes[i]).append("\": {");
            sb.append("\"precision\":").append(BigDecimal.valueOf(0.75 + random.nextDouble() * 0.2).setScale(4, RoundingMode.HALF_UP)).append(",");
            sb.append("\"recall\":").append(BigDecimal.valueOf(0.75 + random.nextDouble() * 0.2).setScale(4, RoundingMode.HALF_UP)).append(",");
            sb.append("\"f1-score\":").append(BigDecimal.valueOf(0.75 + random.nextDouble() * 0.2).setScale(4, RoundingMode.HALF_UP)).append(",");
            sb.append("\"support\":").append(8 + random.nextInt(4));
            sb.append("}");
            if (i < classes.length - 1) sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }
}
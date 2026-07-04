package com.example.animalvoiceprint.config;

import com.example.animalvoiceprint.entity.AudioFile;
import com.example.animalvoiceprint.entity.Dataset;
import com.example.animalvoiceprint.entity.SysUser;
import com.example.animalvoiceprint.entity.TaxonomyLabel;
import com.example.animalvoiceprint.repository.AudioFileRepository;
import com.example.animalvoiceprint.repository.DatasetRepository;
import com.example.animalvoiceprint.repository.SysUserRepository;
import com.example.animalvoiceprint.repository.TaxonomyLabelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final SysUserRepository userRepository;
    private final DatasetRepository datasetRepository;
    private final TaxonomyLabelRepository labelRepository;
    private final AudioFileRepository audioFileRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final String AUDIO_DIR = "./audio_files";
    
    public DataInitializer(SysUserRepository userRepository, 
                          DatasetRepository datasetRepository,
                          TaxonomyLabelRepository labelRepository,
                          AudioFileRepository audioFileRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.datasetRepository = datasetRepository;
        this.labelRepository = labelRepository;
        this.audioFileRepository = audioFileRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) {
        initializeUsers();
        initializeLabels();
        initializeDatasets();
        initializeAudioFiles();
    }
    
    private void initializeUsers() {
        if (!userRepository.existsByUsername("admin")) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("password"));
            admin.setRealName("系统管理员");
            admin.setRole("admin");
            admin.setStatus(1);
            userRepository.save(admin);
        }
        
        if (!userRepository.existsByUsername("annotator")) {
            SysUser annotator = new SysUser();
            annotator.setUsername("annotator");
            annotator.setPasswordHash(passwordEncoder.encode("password"));
            annotator.setRealName("标注员");
            annotator.setRole("annotator");
            annotator.setStatus(1);
            userRepository.save(annotator);
        }
        
        if (!userRepository.existsByUsername("algorithm")) {
            SysUser algorithm = new SysUser();
            algorithm.setUsername("algorithm");
            algorithm.setPasswordHash(passwordEncoder.encode("password"));
            algorithm.setRealName("算法工程师");
            algorithm.setRole("algorithm");
            algorithm.setStatus(1);
            userRepository.save(algorithm);
        }
        
        if (!userRepository.existsByUsername("guest")) {
            SysUser guest = new SysUser();
            guest.setUsername("guest");
            guest.setPasswordHash(passwordEncoder.encode("password"));
            guest.setRealName("访客");
            guest.setRole("guest");
            guest.setStatus(1);
            userRepository.save(guest);
        }
    }
    
    private void initializeLabels() {
        if (labelRepository.count() > 0) {
            return;
        }
        
        List<TaxonomyLabel> labels = new ArrayList<>();
        
        labels.add(createLabel("动物界", 0, "kingdom", "动物界", "0/1"));
        labels.add(createLabel("脊索动物门", 1, "phylum", "脊索动物门", "0/1/2"));
        labels.add(createLabel("节肢动物门", 1, "phylum", "节肢动物门", "0/1/3"));
        labels.add(createLabel("哺乳纲", 2, "class", "哺乳纲", "0/1/2/4"));
        labels.add(createLabel("鸟纲", 2, "class", "鸟纲", "0/1/2/5"));
        labels.add(createLabel("两栖纲", 2, "class", "两栖纲", "0/1/2/6"));
        labels.add(createLabel("昆虫纲", 3, "class", "昆虫纲", "0/1/3/7"));
        labels.add(createLabel("食肉目", 4, "order", "食肉目", "0/1/2/4/8"));
        labels.add(createLabel("灵长目", 4, "order", "灵长目", "0/1/2/4/9"));
        labels.add(createLabel("偶蹄目", 4, "order", "偶蹄目", "0/1/2/4/10"));
        labels.add(createLabel("雀形目", 5, "order", "雀形目", "0/1/2/5/11"));
        labels.add(createLabel("隼形目", 5, "order", "隼形目", "0/1/2/5/12"));
        labels.add(createLabel("鸮形目", 5, "order", "鸮形目", "0/1/2/5/13"));
        labels.add(createLabel("无尾目", 6, "order", "无尾目", "0/1/2/6/14"));
        labels.add(createLabel("半翅目", 7, "order", "半翅目", "0/1/3/7/15"));
        labels.add(createLabel("膜翅目", 7, "order", "膜翅目", "0/1/3/7/16"));
        labels.add(createLabel("直翅目", 7, "order", "直翅目", "0/1/3/7/17"));
        labels.add(createLabel("猫科", 8, "family", "猫科", "0/1/2/4/8/18"));
        labels.add(createLabel("犬科", 8, "family", "犬科", "0/1/2/4/8/19"));
        labels.add(createLabel("象科", 10, "family", "象科", "0/1/2/4/10/20"));
        labels.add(createLabel("猴科", 9, "family", "猴科", "0/1/2/4/9/21"));
        labels.add(createLabel("熊科", 8, "family", "熊科", "0/1/2/4/8/22"));
        labels.add(createLabel("麻雀科", 11, "family", "麻雀科", "0/1/2/5/11/23"));
        labels.add(createLabel("鸦科", 11, "family", "鸦科", "0/1/2/5/11/24"));
        labels.add(createLabel("鸠鸽科", 11, "family", "鸠鸽科", "0/1/2/5/11/25"));
        labels.add(createLabel("鹰科", 12, "family", "鹰科", "0/1/2/5/12/26"));
        labels.add(createLabel("鸱鸮科", 13, "family", "鸱鸮科", "0/1/2/5/13/27"));
        labels.add(createLabel("蝉科", 15, "family", "蝉科", "0/1/3/7/15/28"));
        labels.add(createLabel("蜜蜂科", 16, "family", "蜜蜂科", "0/1/3/7/16/29"));
        labels.add(createLabel("蟋蟀科", 17, "family", "蟋蟀科", "0/1/3/7/17/30"));
        labels.add(createLabel("蛙科", 14, "family", "蛙科", "0/1/2/6/14/31"));
        labels.add(createLabel("蟾蜍科", 14, "family", "蟾蜍科", "0/1/2/6/14/32"));
        labels.add(createLabel("猫属", 18, "genus", "猫属", "0/1/2/4/8/18/33"));
        labels.add(createLabel("豹属", 18, "genus", "豹属", "0/1/2/4/8/18/34"));
        labels.add(createLabel("犬属", 19, "genus", "犬属", "0/1/2/4/8/19/35"));
        labels.add(createLabel("象属", 20, "genus", "象属", "0/1/2/4/10/20/36"));
        labels.add(createLabel("猕猴属", 21, "genus", "猕猴属", "0/1/2/4/9/21/37"));
        labels.add(createLabel("熊属", 22, "genus", "熊属", "0/1/2/4/8/22/38"));
        labels.add(createLabel("麻雀属", 23, "genus", "麻雀属", "0/1/2/5/11/23/39"));
        labels.add(createLabel("喜鹊属", 24, "genus", "喜鹊属", "0/1/2/5/11/24/40"));
        labels.add(createLabel("乌鸦属", 24, "genus", "乌鸦属", "0/1/2/5/11/24/41"));
        labels.add(createLabel("鸽属", 25, "genus", "鸽属", "0/1/2/5/11/25/42"));
        labels.add(createLabel("鹰属", 26, "genus", "鹰属", "0/1/2/5/12/26/43"));
        labels.add(createLabel("鸮属", 27, "genus", "鸮属", "0/1/2/5/13/27/44"));
        labels.add(createLabel("蝉属", 28, "genus", "蝉属", "0/1/3/7/15/28/45"));
        labels.add(createLabel("蜜蜂属", 29, "genus", "蜜蜂属", "0/1/3/7/16/29/46"));
        labels.add(createLabel("蟋蟀属", 30, "genus", "蟋蟀属", "0/1/3/7/17/30/47"));
        labels.add(createLabel("蛙属", 31, "genus", "蛙属", "0/1/2/6/14/31/48"));
        labels.add(createLabel("蟾蜍属", 32, "genus", "蟾蜍属", "0/1/2/6/14/32/49"));
        
        labels.add(createLabel("家猫", 33, "species", "家猫", "0/1/2/4/8/18/33/50"));
        labels.add(createLabel("老虎", 34, "species", "老虎", "0/1/2/4/8/18/34/51"));
        labels.add(createLabel("狮子", 34, "species", "狮子", "0/1/2/4/8/18/34/52"));
        labels.add(createLabel("狗", 35, "species", "狗", "0/1/2/4/8/19/35/53"));
        labels.add(createLabel("狼", 35, "species", "狼", "0/1/2/4/8/19/35/54"));
        labels.add(createLabel("大象", 36, "species", "大象", "0/1/2/4/10/20/36/55"));
        labels.add(createLabel("猴子", 37, "species", "猴子", "0/1/2/4/9/21/37/56"));
        labels.add(createLabel("熊", 38, "species", "熊", "0/1/2/4/8/22/38/57"));
        labels.add(createLabel("麻雀", 39, "species", "麻雀", "0/1/2/5/11/23/39/58"));
        labels.add(createLabel("喜鹊", 40, "species", "喜鹊", "0/1/2/5/11/24/40/59"));
        labels.add(createLabel("乌鸦", 41, "species", "乌鸦", "0/1/2/5/11/24/41/60"));
        labels.add(createLabel("鸽子", 42, "species", "鸽子", "0/1/2/5/11/25/42/61"));
        labels.add(createLabel("鹰", 43, "species", "鹰", "0/1/2/5/12/26/43/62"));
        labels.add(createLabel("猫头鹰", 44, "species", "猫头鹰", "0/1/2/5/13/27/44/63"));
        labels.add(createLabel("蝉", 45, "species", "蝉", "0/1/3/7/15/28/45/64"));
        labels.add(createLabel("蜜蜂", 46, "species", "蜜蜂", "0/1/3/7/16/29/46/65"));
        labels.add(createLabel("蟋蟀", 47, "species", "蟋蟀", "0/1/3/7/17/30/47/66"));
        labels.add(createLabel("青蛙", 48, "species", "青蛙", "0/1/2/6/14/31/48/67"));
        labels.add(createLabel("蟾蜍", 49, "species", "蟾蜍", "0/1/2/6/14/32/49/68"));
        
        labelRepository.saveAll(labels);
    }
    
    private TaxonomyLabel createLabel(String name, int parentId, String rank, String description, String path) {
        TaxonomyLabel label = new TaxonomyLabel();
        label.setLabelName(name);
        label.setParentId(parentId);
        label.setTaxonRank(rank);
        label.setDescription(description);
        label.setLabelPath(path);
        return label;
    }
    
    private void initializeDatasets() {
        if (datasetRepository.count() > 0) {
            return;
        }
        
        List<Dataset> datasets = new ArrayList<>();
        
        Dataset mainDataset = new Dataset();
        mainDataset.setDatasetName("动物叫声数据集");
        mainDataset.setDescription("包含多种动物的叫声录音，用于声纹识别和分类训练");
        mainDataset.setCreateUserId(1);
        mainDataset.setAudioCount(20);
        datasets.add(mainDataset);
        
        Dataset birdsDataset = new Dataset();
        birdsDataset.setDatasetName("鸟类数据集");
        birdsDataset.setDescription("鸟类声纹数据");
        birdsDataset.setCreateUserId(1);
        birdsDataset.setAudioCount(6);
        datasets.add(birdsDataset);
        
        Dataset mammalsDataset = new Dataset();
        mammalsDataset.setDatasetName("哺乳动物数据集");
        mammalsDataset.setDescription("哺乳动物声纹数据");
        mammalsDataset.setCreateUserId(1);
        mammalsDataset.setAudioCount(8);
        datasets.add(mammalsDataset);
        
        Dataset insectsDataset = new Dataset();
        insectsDataset.setDatasetName("昆虫数据集");
        insectsDataset.setDescription("昆虫声纹数据");
        insectsDataset.setCreateUserId(1);
        insectsDataset.setAudioCount(4);
        datasets.add(insectsDataset);
        
        Dataset amphibiansDataset = new Dataset();
        amphibiansDataset.setDatasetName("两栖动物数据集");
        amphibiansDataset.setDescription("两栖动物声纹数据");
        amphibiansDataset.setCreateUserId(1);
        amphibiansDataset.setAudioCount(2);
        datasets.add(amphibiansDataset);
        
        datasetRepository.saveAll(datasets);
    }
    
    private void initializeAudioFiles() {
        if (audioFileRepository.count() > 0) {
            return;
        }
        
        try {
            Files.createDirectories(Paths.get(AUDIO_DIR));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        SysUser admin = userRepository.findById(1).orElse(null);
        if (admin == null) return;
        
        int[][] animalConfigs = {
            {50, 100, 1500, 2},
            {80, 150, 2000, 1},
            {100, 200, 1500, 1},
            {120, 150, 800, 2},
            {150, 400, 1200, 3},
            {400, 600, 1000, 6},
            {600, 1200, 1500, 4},
            {800, 1000, 1250, 7},
            {3000, 1500, 150, 8},
            {2500, 1000, 200, 5},
            {800, 200, 400, 3},
            {400, 100, 500, 4},
            {600, 1500, 1000, 1},
            {300, 50, 800, 3},
            {4000, 500, 50, 40},
            {200, 50, 20, 100},
            {3000, 200, 100, 20},
            {300, 200, 300, 6},
            {150, 100, 500, 4},
            {450, 50, 30, 80},
        };
        
        String[] animalNames = {
            "大象", "狮子", "老虎", "熊", "狼", "狗", "猫", "猴子",
            "麻雀", "喜鹊", "乌鸦", "鸽子", "鹰", "猫头鹰",
            "蝉", "蜜蜂", "蟋蟀", "青蛙", "蟾蜍", "蚊子"
        };
        
        int[] labelIds = {55, 52, 51, 57, 54, 53, 50, 56,
                          58, 59, 60, 61, 62, 63,
                          64, 65, 66, 67, 68, 69};
        
        int[] datasetIds = {1, 1, 1, 1, 1, 1, 1, 1,
                           2, 2, 2, 2, 2, 2,
                           4, 4, 4, 5, 5, 4};
        
        List<AudioFile> audioFiles = new ArrayList<>();
        
        for (int i = 0; i < animalConfigs.length; i++) {
            String fileName = animalNames[i] + ".wav";
            String filePath = AUDIO_DIR + "/" + fileName;
            
            byte[] wavData = generateWavData(
                animalConfigs[i][0], 
                animalConfigs[i][1], 
                animalConfigs[i][2],
                animalConfigs[i][3]
            );
            
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(wavData);
            } catch (IOException e) {
                continue;
            }
            
            AudioFile audioFile = new AudioFile();
            audioFile.setDatasetId(datasetIds[i]);
            audioFile.setFileName(fileName);
            audioFile.setFilePath(filePath);
            audioFile.setDuration(BigDecimal.valueOf(animalConfigs[i][3] * animalConfigs[i][2] / 1000.0 + 0.5));
            audioFile.setSampleRate(16000);
            audioFile.setChannels(1);
            audioFile.setFileSize((long) wavData.length);
            audioFile.setNoiseLevel("low");
            audioFile.setLocation("人工合成");
            audioFile.setUploadUserId(1);
            audioFile.setRemark("合成" + animalNames[i] + "叫声");
            
            audioFiles.add(audioFile);
        }
        
        audioFileRepository.saveAll(audioFiles);
    }
    
    private byte[] generateWavData(int baseFreq, int variation, int durationMs, int repeats) {
        try {
            int sampleRate = 16000;
            int bytesPerSample = 2;
            int channels = 1;
            int durationPerRepeat = durationMs * sampleRate / 1000;
            int totalSamples = durationPerRepeat * repeats + sampleRate / 2;
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeBytes("RIFF");
            dos.writeInt(36 + totalSamples * channels * bytesPerSample);
            dos.writeBytes("WAVE");
            dos.writeBytes("fmt ");
            dos.writeInt(16);
            dos.writeShort(1);
            dos.writeShort(channels);
            dos.writeInt(sampleRate);
            dos.writeInt(sampleRate * channels * bytesPerSample);
            dos.writeShort(channels * bytesPerSample);
            dos.writeShort(8 * bytesPerSample);
            dos.writeBytes("data");
            dos.writeInt(totalSamples * channels * bytesPerSample);
            
            for (int r = 0; r < repeats; r++) {
                for (int i = 0; i < durationPerRepeat; i++) {
                    double t = (double) i / sampleRate;
                    double freq = baseFreq + variation * Math.sin(2 * Math.PI * 5 * t);
                    double envelope = Math.sin(Math.PI * t / (durationMs / 1000.0));
                    double sample = Math.sin(2 * Math.PI * freq * t) * envelope * 0.3;
                    
                    short sampleInt = (short) (sample * 32767);
                    dos.writeShort(sampleInt);
                }
                
                for (int i = 0; i < sampleRate / 4; i++) {
                    dos.writeShort((short) 0);
                }
            }
            
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }
}

<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">模型预测</h1>
      </div>
    </div>

    <div class="card-container">
      <el-card class="chart-card">
        <template #header>
          <span class="card-title">选择模型和音频</span>
        </template>
        
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="训练任务（模型）" prop="taskId">
                <el-select v-model="form.taskId" placeholder="选择已完成的训练任务">
                  <el-option 
                    v-for="task in completedTasks" 
                    :key="task.taskId" 
                    :label="`${task.taskName} (${task.modelType})`" 
                    :value="task.taskId" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="音频文件" prop="audioId">
                <el-select v-model="form.audioId" placeholder="选择音频文件">
                  <el-option 
                    v-for="audio in audioFiles" 
                    :key="audio.audioId" 
                    :label="audio.fileName" 
                    :value="audio.audioId" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item>
            <el-button type="primary" :loading="predicting" @click="handlePredict">开始预测</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div v-if="predictionResult" class="card-container">
      <el-card class="chart-card">
        <template #header>
          <span class="card-title">预测结果</span>
        </template>
        
        <div class="result-header">
          <div class="result-info">
            <span class="result-label">音频文件：</span>
            <span class="result-value">{{ predictionResult.fileName }}</span>
          </div>
          <div class="result-info">
            <span class="result-label">模型类型：</span>
            <span class="result-value">{{ predictionResult.modelType }}</span>
          </div>
        </div>

        <div v-if="predictionResult.topPrediction" class="top-prediction">
          <div class="top-prediction-header">
            <span class="top-label">最佳预测</span>
          </div>
          <div class="top-prediction-content">
            <div class="top-name">{{ predictionResult.topPrediction.labelName }}</div>
            <div class="top-confidence">置信度：{{ predictionResult.topPrediction.confidence }}%</div>
            <div class="top-hierarchy">
              <span class="hierarchy-label">分类层级：</span>
              <span v-for="(level, index) in predictionResult.topPrediction.hierarchy" :key="index">
                {{ level.name }}
                <span v-if="index < predictionResult.topPrediction.hierarchy.length - 1"> → </span>
              </span>
            </div>
          </div>
        </div>

        <div class="prediction-list">
          <h4>候选预测列表（Top 5）</h4>
          <el-table 
            :data="predictionResult.predictions" 
            row-key="labelId"
            size="small"
          >
            <el-table-column prop="labelName" label="动物名称" width="120" />
            <el-table-column prop="confidence" label="置信度" width="100">
              <template #default="scope">
                <span class="number-font">{{ scope.row.confidence }}%</span>
              </template>
            </el-table-column>
            <el-table-column label="分类层级">
              <template #default="scope">
                {{ scope.row.hierarchy?.map(h => h.name).join(' → ') || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>

    <div v-if="!predictionResult && !predicting" class="card-container">
      <el-card class="chart-card">
        <template #header>
          <span class="card-title">预测说明</span>
        </template>
        <div class="info-content">
          <p>1. 请先创建并完成训练任务（在训练管理页面）</p>
          <p>2. 选择已完成的训练任务作为预测模型</p>
          <p>3. 选择要预测的音频文件</p>
          <p>4. 点击"开始预测"按钮获取分类结果</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { predict } from '../api/prediction'
import { getAllAudioFiles } from '../api/audio'
import { getAllTasks } from '../api/train'

const form = reactive({
  taskId: '',
  audioId: ''
})

const rules = {
  taskId: [{ required: true, message: '请选择训练任务', trigger: 'change' }],
  audioId: [{ required: true, message: '请选择音频文件', trigger: 'change' }]
}

const formRef = ref(null)
const loading = ref(false)
const predicting = ref(false)
const audioFiles = ref([])
const tasks = ref([])
const predictionResult = ref(null)

const completedTasks = computed(() => {
  return tasks.value.filter(t => t.status === 'success')
})

const loadAudioFiles = async () => {
  const res = await getAllAudioFiles()
  audioFiles.value = res.data.data
}

const loadTasks = async () => {
  const res = await getAllTasks()
  tasks.value = res.data.data
}

const handlePredict = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  predicting.value = true
  try {
    const res = await predict({
      audioId: form.audioId,
      taskId: form.taskId
    })
    predictionResult.value = res.data.data
    ElMessage.success('预测完成')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '预测失败')
  } finally {
    predicting.value = false
  }
}

const resetForm = () => {
  form.taskId = ''
  form.audioId = ''
  predictionResult.value = null
}

onMounted(() => {
  loadAudioFiles()
  loadTasks()
})
</script>

<style scoped>
.result-header {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--color-border);
}

.result-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-label {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.result-value {
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 500;
}

.top-prediction {
  background: linear-gradient(135deg, #165DFF15 0%, #00B42A15 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #165DFF30;
}

.top-prediction-header {
  margin-bottom: 10px;
}

.top-label {
  color: #165DFF;
  font-size: 12px;
  font-weight: 600;
  background: #165DFF15;
  padding: 4px 12px;
  border-radius: 4px;
}

.top-prediction-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.top-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.top-confidence {
  font-size: 16px;
  color: #00B42A;
  font-weight: 600;
}

.top-hierarchy {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.hierarchy-label {
  margin-right: 8px;
}

.prediction-list {
  h4 {
    margin-bottom: 12px;
    color: var(--color-text-primary);
    font-size: 14px;
    font-weight: 600;
  }
}

.info-content {
  p {
    margin-bottom: 8px;
    color: var(--color-text-secondary);
    font-size: 14px;
    line-height: 1.6;
  }
}
</style>

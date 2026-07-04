<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">标注工作台</h1>
      </div>
      <div class="page-header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd">创建标注</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索标注ID或备注..."
        prefix-icon="Search"
        style="width: 320px"
      />
    </div>

    <div class="table-container">
      <el-table
        :data="filteredAnnotations"
        row-key="annotationId"
        :loading="loading"
        size="small"
      >
        <el-table-column prop="annotationId" label="标注ID" width="80" />
        <el-table-column prop="audioId" label="音频文件" width="150">
          <template #default="scope">
            {{ audioFiles.find(a => a.audioId === scope.row.audioId)?.fileName || scope.row.audioId }}
          </template>
        </el-table-column>
        <el-table-column prop="labelId" label="标签" width="120">
          <template #default="scope">
            {{ labels.find(l => l.labelId === scope.row.labelId)?.labelName || scope.row.labelId }}
          </template>
        </el-table-column>
        <el-table-column prop="soundType" label="声音类型" width="80">
          <template #default="scope">
            <el-tag type="primary">{{ soundTypeLabels[scope.row.soundType] || scope.row.soundType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="100">
          <template #default="scope">
            <span class="number-font">{{ scope.row.startTime?.toFixed(2) }}s</span>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="100">
          <template #default="scope">
            <span class="number-font">{{ scope.row.endTime?.toFixed(2) }}s</span>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="80">
          <template #default="scope">
            <span class="number-font">{{ scope.row.confidence != null ? `${(scope.row.confidence * 100).toFixed(0)}%` : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="statusColors[scope.row.status] || 'primary'">{{ statusLabels[scope.row.status] || scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="scope">
            {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString('zh-CN') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 'draft'" type="text" @click="handleSubmit(scope.row.annotationId)">提交审核</el-button>
            <el-button v-if="scope.row.status === 'submitted'" type="text" @click="handleReview(scope.row)">审核</el-button>
            <el-button type="text" @click="handleDelete(scope.row.annotationId)" style="color: #F53F3F">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      :title="selectedAnnotation ? '编辑标注' : '创建标注'"
      v-model="modalVisible"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="音频文件" prop="audioId">
          <el-select v-model="form.audioId" placeholder="选择音频文件">
            <el-option v-for="a in audioFiles" :key="a.audioId" :label="a.fileName" :value="a.audioId" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间(秒)" prop="startTime">
          <el-input v-model.number="form.startTime" type="number" placeholder="请输入开始时间" />
        </el-form-item>
        <el-form-item label="结束时间(秒)" prop="endTime">
          <el-input v-model.number="form.endTime" type="number" placeholder="请输入结束时间" />
        </el-form-item>
        <el-form-item label="标签" prop="labelId">
          <el-select v-model="form.labelId" placeholder="选择标签">
            <el-option v-for="l in labels" :key="l.labelId" :label="l.labelName" :value="l.labelId" />
          </el-select>
        </el-form-item>
        <el-form-item label="声音类型" prop="soundType">
          <el-select v-model="form.soundType" placeholder="选择声音类型">
            <el-option label="鸣叫" value="call" />
            <el-option label="歌唱" value="song" />
            <el-option label="警报" value="alarm" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="置信度(0-100)" prop="confidence">
          <el-input v-model.number="form.confidence" type="number" :min="0" :max="100" placeholder="请输入置信度" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" @click="handleSubmit">提交</el-button>
          <el-button @click="closeModal" style="marginLeft: 8">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog
      :title="selectedAudio?.fileName || '音频标注'"
      v-model="playModalVisible"
      width="800px"
    >
      <div style="display: 'flex'; alignItems: 'center'; gap: 12; marginBottom: 16">
        <el-button
          icon="ArrowLeft"
          @click="prevAudio"
          :disabled="audioFiles.length <= 1"
        />
        <span class="number-font" style="color: '#86909C'">
          {{ currentAudioIndex + 1 }} / {{ audioFiles.length }}
        </span>
        <el-button
          icon="ArrowRight"
          @click="nextAudio"
          :disabled="audioFiles.length <= 1"
        />
      </div>

      <div style="marginBottom: 16">
        <div ref="waveformRef" id="waveform" style="backgroundColor: '#121826'; borderRadius: 8; height: 150" />
      </div>

      <div v-if="wavesurfer" style="display: 'flex'; alignItems: 'center'; gap: 12">
        <el-button type="primary" icon="VideoPlay" @click="wavesurfer.playPause()">
          {{ wavesurfer.isPlaying() ? '暂停' : '播放' }}
        </el-button>
        <span class="number-font" style="color: '#86909C'">
          {{ wavesurfer.getCurrentTime().toFixed(2) }} / {{ wavesurfer.getDuration().toFixed(2) }} s
        </span>
      </div>

      <div v-if="selectedAudio" style="marginTop: 16; paddingTop: 16; borderTop: '1px solid #2A3344'">
        <div style="display: 'grid'; gridTemplateColumns: 'repeat(3, 1fr)'; gap: 12">
          <div>
            <span style="color: '#86909C'; fontSize: 12">采样率</span>
            <div class="number-font" style="color: '#E5E6EB'; marginTop: 4">{{ selectedAudio.sampleRate }} Hz</div>
          </div>
          <div>
            <span style="color: '#86909C'; fontSize: 12">时长</span>
            <div class="number-font" style="color: '#E5E6EB'; marginTop: 4">{{ selectedAudio.duration?.toFixed(2) }} s</div>
          </div>
          <div>
            <span style="color: '#86909C'; fontSize: 12">噪声等级</span>
            <div style="color: '#E5E6EB'; marginTop: 4">
              <el-tag :type="noiseLevelType(selectedAudio.noiseLevel)">
                {{ noiseLevelLabel(selectedAudio.noiseLevel) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import WaveSurfer from 'wavesurfer.js'
import {
  getAllAnnotations,
  createAnnotation,
  updateAnnotation,
  deleteAnnotation,
  submitAnnotation,
  reviewAnnotation
} from '../api/annotation'
import { getAllAudioFiles } from '../api/audio'
import { getAllLabels } from '../api/label'

const annotations = ref([])
const audioFiles = ref([])
const labels = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const playModalVisible = ref(false)
const formRef = ref(null)
const selectedAnnotation = ref(null)
const selectedAudio = ref(null)
const searchText = ref('')
const currentAudioIndex = ref(0)
const waveformRef = ref(null)
let wavesurfer = null

const form = reactive({
  audioId: '',
  startTime: null,
  endTime: null,
  labelId: '',
  soundType: '',
  confidence: null,
  remark: ''
})

const rules = {
  audioId: [{ required: true, message: '请选择音频文件', trigger: 'change' }],
  startTime: [{ required: true, message: '请输入开始时间', trigger: 'blur' }],
  endTime: [{ required: true, message: '请输入结束时间', trigger: 'blur' }],
  labelId: [{ required: true, message: '请选择标签', trigger: 'change' }]
}

const statusConfig = {
  submitted: { color: 'warning', text: '待审核' },
  approved: { color: 'success', text: '已通过' },
  rejected: { color: 'danger', text: '已拒绝' },
  draft: { color: 'primary', text: '草稿' }
}

const soundTypeLabels = {
  call: '鸣叫',
  song: '歌唱',
  alarm: '警报',
  other: '其他'
}

const statusColors = {
  submitted: 'warning',
  approved: 'success',
  rejected: 'danger',
  draft: 'primary'
}

const statusLabels = {
  submitted: '待审核',
  approved: '已通过',
  rejected: '已拒绝',
  draft: '草稿'
}

const noiseLevelType = (level) => {
  const types = { high: 'danger', medium: 'warning', low: 'success' }
  return types[level] || 'primary'
}

const noiseLevelLabel = (level) => {
  const labels = { high: '高', medium: '中', low: '低' }
  return labels[level] || '未知'
}

const filteredAnnotations = computed(() => {
  return annotations.value.filter(item => {
    return searchText.value === '' ||
      item.annotationId.toString().includes(searchText.value) ||
      (item.remark && item.remark.toLowerCase().includes(searchText.value.toLowerCase()))
  })
})

const initWaveSurfer = () => {
  if (wavesurfer) {
    wavesurfer.destroy()
  }
  nextTick(() => {
    wavesurfer = WaveSurfer.create({
      container: '#waveform',
      waveColor: '#2A3344',
      progressColor: '#165DFF',
      cursorColor: '#165DFF',
      barWidth: 2,
      barGap: 3,
      barRadius: 3,
      responsive: true,
      height: 150
    })
    if (selectedAudio.value) {
      wavesurfer.load(`/api/audio/download/${selectedAudio.value.filePath}`)
    }
  })
}

const destroyWaveSurfer = () => {
  if (wavesurfer) {
    wavesurfer.destroy()
    wavesurfer = null
  }
}

const loadAnnotations = async () => {
  loading.value = true
  try {
    const res = await getAllAnnotations()
    annotations.value = res.data.data
  } finally {
    loading.value = false
  }
}

const loadAudioFiles = async () => {
  const res = await getAllAudioFiles()
  audioFiles.value = res.data.data
}

const loadLabels = async () => {
  const res = await getAllLabels()
  labels.value = res.data.data
}

const handleAdd = () => {
  form.audioId = ''
  form.startTime = null
  form.endTime = null
  form.labelId = ''
  form.soundType = ''
  form.confidence = null
  form.remark = ''
  selectedAnnotation.value = null
  modalVisible.value = true
}

const handleEdit = (record) => {
  form.audioId = record.audioId
  form.startTime = record.startTime
  form.endTime = record.endTime
  form.labelId = record.labelId
  form.soundType = record.soundType || ''
  form.confidence = record.confidence
  form.remark = record.remark || ''
  selectedAnnotation.value = record
  modalVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAnnotation(id)
    ElMessage.success('删除成功')
    loadAnnotations()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '删除失败')
    }
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  try {
    if (selectedAnnotation.value) {
      await updateAnnotation(selectedAnnotation.value.annotationId, form)
      ElMessage.success('更新成功')
    } else {
      await createAnnotation(form)
      ElMessage.success('创建成功')
    }
    closeModal()
    loadAnnotations()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || (selectedAnnotation.value ? '更新失败' : '创建失败'))
  }
}

const handleReview = async (id, approved) => {
  try {
    await reviewAnnotation(id, approved)
    ElMessage.success(approved ? '审核通过' : '审核拒绝')
    loadAnnotations()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '审核失败')
  }
}

const handlePlay = (audioId) => {
  const audio = audioFiles.value.find(a => a.audioId === audioId)
  if (audio) {
    const index = audioFiles.value.findIndex(a => a.audioId === audioId)
    currentAudioIndex.value = index
    selectedAudio.value = audio
    playModalVisible.value = true
  }
}

const prevAudio = () => {
  const prevIndex = currentAudioIndex.value > 0 ? currentAudioIndex.value - 1 : audioFiles.value.length - 1
  currentAudioIndex.value = prevIndex
  selectedAudio.value = audioFiles.value[prevIndex]
}

const nextAudio = () => {
  const nextIndex = currentAudioIndex.value < audioFiles.value.length - 1 ? currentAudioIndex.value + 1 : 0
  currentAudioIndex.value = nextIndex
  selectedAudio.value = audioFiles.value[nextIndex]
}

const closeModal = () => {
  modalVisible.value = false
  selectedAnnotation.value = null
}

watch(playModalVisible, (val) => {
  if (val && selectedAudio.value) {
    initWaveSurfer()
  } else {
    destroyWaveSurfer()
  }
})

watch(selectedAudio, () => {
  if (playModalVisible.value && selectedAudio.value) {
    initWaveSurfer()
  }
})

onMounted(() => {
  loadAnnotations()
  loadAudioFiles()
  loadLabels()
})
</script>
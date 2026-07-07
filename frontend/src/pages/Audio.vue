<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">音频管理</h1>
      </div>
      <div class="page-header-right">
        <el-select
          v-model="selectedDataset"
          placeholder="选择数据集"
          style="width: 180px"
        >
          <el-option label="全部" value="" />
          <el-option v-for="d in datasets" :key="d.datasetId" :label="d.datasetName" :value="d.datasetId" />
        </el-select>
        <el-upload
          class="upload-dragger"
          action="#"
          :auto-upload="false"
          :on-change="handleUpload"
          accept=".wav,.mp3,.flac,.ogg"
        >
          <el-icon :size="24"><Upload /></el-icon>
          <div class="el-upload__text">点击或拖拽上传</div>
        </el-upload>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索文件名或采集地点..."
        prefix-icon="Search"
        style="width: 320px"
      />
    </div>

    <div class="table-container">
      <el-table
        :data="filteredAudioFiles"
        row-key="audioId"
        :loading="loading"
        size="small"
      >
        <el-table-column prop="fileName" label="文件名" show-overflow-tooltip width="200" />
        <el-table-column prop="datasetId" label="数据集" width="120">
          <template #default="scope">
            {{ datasets.find(d => d.datasetId === scope.row.datasetId)?.datasetName || scope.row.datasetId }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="80">
          <template #default="scope">
            <span class="number-font">{{ scope.row.duration != null ? `${scope.row.duration.toFixed(2)}s` : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sampleRate" label="采样率" width="100">
          <template #default="scope">
            <span class="number-font">{{ scope.row.sampleRate != null ? `${scope.row.sampleRate} Hz` : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="noiseLevel" label="噪声等级" width="80">
          <template #default="scope">
            <el-tag :type="noiseLevelColors[scope.row.noiseLevel] || 'primary'">{{ noiseLevelLabels[scope.row.noiseLevel] || scope.row.noiseLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="采集地点" show-overflow-tooltip width="120" />
        <el-table-column prop="fileSize" label="文件大小" width="100">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="120">
          <template #default="scope">
            {{ scope.row.uploadTime ? new Date(scope.row.uploadTime).toLocaleDateString('zh-CN') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="scope">
            <el-button type="text" @click="handlePlay(scope.row)"><el-icon><VideoPlay /></el-icon></el-button>
            <el-button type="text" @click="handleEdit(scope.row)"><el-icon><Edit /></el-icon></el-button>
            <el-button type="text" @click="handleDelete(scope.row.audioId)" style="color: #F53F3F"><el-icon><Delete /></el-icon></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      :title="selectedAudio ? '编辑音频信息' : '音频信息'"
      v-model="modalVisible"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="噪声等级" prop="noiseLevel">
          <el-select v-model="form.noiseLevel" placeholder="选择噪声等级">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="未知" value="unknown" />
          </el-select>
        </el-form-item>
        <el-form-item label="采集地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入采集地点" />
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
      :title="selectedAudio?.fileName || '音频播放'"
      v-model="playModalVisible"
      width="600px"
    >
      <div style="marginBottom: 16">
        <div ref="waveformRef" id="waveform" style="backgroundColor: '#121826'; borderRadius: 8; height: 120" />
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
        <div style="display: 'grid'; gridTemplateColumns: '1fr 1fr'; gap: 12">
          <div>
            <span style="color: '#86909C'; fontSize: 12">采样率</span>
            <div class="number-font" style="color: '#E5E6EB'; marginTop: 4">{{ selectedAudio.sampleRate }} Hz</div>
          </div>
          <div>
            <span style="color: '#86909C'; fontSize: 12">时长</span>
            <div class="number-font" style="color: '#E5E6EB'; marginTop: 4">{{ selectedAudio.duration?.toFixed(2) }} s</div>
          </div>
          <div>
            <span style="color: '#86909C'; fontSize: 12">通道数</span>
            <div class="number-font" style="color: '#E5E6EB'; marginTop: 4">{{ selectedAudio.channels }} 通道</div>
          </div>
          <div>
            <span style="color: '#86909C'; fontSize: 12">文件大小</span>
            <div class="number-font" style="color: '#E5E6EB'; marginTop: 4">
              {{ selectedAudio.fileSize ? formatFileSize(selectedAudio.fileSize) : '-' }}
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import WaveSurfer from 'wavesurfer.js'
import { Upload } from '@element-plus/icons-vue'
import { getAllAudioFiles, uploadAudioFiles, updateAudioFile, deleteAudioFile } from '../api/audio'
import { getAllDatasets } from '../api/dataset'

const audioFiles = ref([])
const loading = ref(false)
const datasets = ref([])
const modalVisible = ref(false)
const playModalVisible = ref(false)
const formRef = ref(null)
const selectedAudio = ref(null)
const searchText = ref('')
const selectedDataset = ref('')
const waveformRef = ref(null)
let wavesurfer = null

const form = reactive({
  noiseLevel: '',
  location: '',
  remark: ''
})

const rules = {}

const noiseLevelColors = {
  low: 'success',
  medium: 'warning',
  high: 'danger',
  unknown: 'primary'
}

const noiseLevelLabels = {
  low: '低',
  medium: '中',
  high: '高',
  unknown: '未知'
}

const formatFileSize = (size) => {
  if (!size) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(2)} MB`
}

const filteredAudioFiles = computed(() => {
  return audioFiles.value.filter(item => {
    const matchesSearch = searchText.value === '' || 
      item.fileName.toLowerCase().includes(searchText.value.toLowerCase()) ||
      (item.location && item.location.toLowerCase().includes(searchText.value.toLowerCase()))
    const matchesDataset = selectedDataset.value === '' || item.datasetId === selectedDataset.value
    return matchesSearch && matchesDataset
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
      height: 120
    })
    const fileName = selectedAudio.value.fileName
    wavesurfer.load(`/api/audio/download/${selectedAudio.value.datasetId}/${fileName}`)
  })
}

const destroyWaveSurfer = () => {
  if (wavesurfer) {
    wavesurfer.destroy()
    wavesurfer = null
  }
}

const loadAudioFiles = async () => {
  loading.value = true
  try {
    const res = await getAllAudioFiles()
    if (res.data && res.data.data) {
      audioFiles.value = res.data.data
    } else {
      ElMessage.error('获取数据失败')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '获取音频文件失败')
  } finally {
    loading.value = false
  }
}

const loadDatasets = async () => {
  try {
    const res = await getAllDatasets()
    if (res.data && res.data.data) {
      datasets.value = res.data.data
    } else {
      ElMessage.error('获取数据集失败')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '获取数据集失败')
  }
}

const handleAdd = () => {
  form.noiseLevel = ''
  form.location = ''
  form.remark = ''
  selectedAudio.value = null
  modalVisible.value = true
}

const handleEdit = (record) => {
  form.noiseLevel = record.noiseLevel || ''
  form.location = record.location || ''
  form.remark = record.remark || ''
  selectedAudio.value = record
  modalVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await deleteAudioFile(id)
    ElMessage.success('删除成功')
    loadAudioFiles()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

const handleSubmit = async () => {
  if (!selectedAudio.value) {
    ElMessage.error('请选择音频文件')
    return
  }
  try {
    await updateAudioFile(selectedAudio.value.audioId, form)
    ElMessage.success('更新成功')
    closeModal()
    loadAudioFiles()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '更新失败')
  }
}

const handleUpload = ({ file }) => {
  if (!selectedDataset.value) {
    ElMessage.error('请先选择数据集')
    return false
  }
  
  const formData = new FormData()
  formData.append('files', file.raw)
  formData.append('datasetId', selectedDataset.value)
  
  uploadAudioFiles(formData)
    .then(() => {
      ElMessage.success('上传成功')
      loadAudioFiles()
    })
    .catch(err => {
      ElMessage.error(err.response?.data?.message || '上传失败')
    })
  
  return false
}

const handlePlay = (record) => {
  selectedAudio.value = record
  playModalVisible.value = true
}

const closeModal = () => {
  modalVisible.value = false
  selectedAudio.value = null
}

watch(playModalVisible, (val) => {
  if (val && selectedAudio.value) {
    initWaveSurfer()
  } else {
    destroyWaveSurfer()
  }
})

onMounted(() => {
  loadAudioFiles()
  loadDatasets()
})
</script>

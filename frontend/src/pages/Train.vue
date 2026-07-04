<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">训练任务管理</h1>
      </div>
      <div class="page-header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd">创建训练任务</el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table
        :data="tasks"
        row-key="taskId"
        :loading="loading"
        size="small"
      >
        <el-table-column prop="taskName" label="任务名称" show-overflow-tooltip />
        <el-table-column prop="datasetId" label="数据集" width="120">
          <template #default="scope">
            {{ datasets.find(d => d.datasetId === scope.row.datasetId)?.datasetName || scope.row.datasetId }}
          </template>
        </el-table-column>
        <el-table-column prop="modelType" label="模型类型" width="120" />
        <el-table-column prop="enableHierarchicalLoss" label="层级损失" width="100">
          <template #default="scope">
            {{ scope.row.enableHierarchicalLoss ? '启用' : '禁用' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="statusConfig[scope.row.status]?.color || 'primary'">{{ statusConfig[scope.row.status]?.text || scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentEpoch" label="当前轮次" width="100">
          <template #default="scope">
            <span class="number-font">{{ scope.row.currentEpoch != null ? `${scope.row.currentEpoch}轮` : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bestValMetric" label="最佳指标" width="100">
          <template #default="scope">
            <span class="number-font">{{ scope.row.bestValMetric != null ? `${(scope.row.bestValMetric * 100).toFixed(1)}%` : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="scope">
            {{ dayjs(scope.row.createTime).format('YYYY-MM-DD HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150">
          <template #default="scope">
            {{ scope.row.startTime ? dayjs(scope.row.startTime).format('YYYY-MM-DD HH:mm') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="150">
          <template #default="scope">
            {{ scope.row.endTime ? dayjs(scope.row.endTime).format('YYYY-MM-DD HH:mm') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'pending'" type="text" @click="handleStart(scope.row.taskId)">启动</el-button>
            <el-button v-if="scope.row.status === 'running'" type="text" loading>训练中</el-button>
            <el-button v-if="scope.row.status !== 'running'" type="text" @click="handleDelete(scope.row.taskId)" style="color: #F53F3F">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      title="创建训练任务"
      v-model="modalVisible"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="数据集" prop="datasetId">
          <el-select v-model="form.datasetId" placeholder="选择数据集">
            <el-option v-for="d in datasets" :key="d.datasetId" :label="d.datasetName" :value="d.datasetId" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型类型" prop="modelType">
          <el-select v-model="form.modelType" placeholder="选择模型类型">
            <el-option label="随机森林" value="RandomForest" />
            <el-option label="支持向量机" value="SVM" />
            <el-option label="卷积神经网络" value="CNN" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用层级损失" prop="enableHierarchicalLoss">
          <el-select v-model="form.enableHierarchicalLoss" placeholder="是否启用层级损失">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="训练参数" prop="trainParams">
          <el-input v-model="form.trainParams" type="textarea" :rows="3" placeholder='{"n_estimators": 100, "max_depth": 10}' />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" @click="handleSubmit">提交</el-button>
          <el-button @click="modalVisible = false" style="marginLeft: 8">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllTasks, createTask, startTask, deleteTask } from '../api/train'
import { getAllDatasets } from '../api/dataset'
import dayjs from 'dayjs'

const tasks = ref([])
const loading = ref(false)
const datasets = ref([])
const modalVisible = ref(false)
const formRef = ref(null)
let intervalId = null

const form = reactive({
  taskName: '',
  datasetId: '',
  modelType: '',
  enableHierarchicalLoss: false,
  trainParams: ''
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  datasetId: [{ required: true, message: '请选择数据集', trigger: 'change' }],
  modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }]
}

const statusConfig = {
  pending: { color: 'primary', text: '等待中' },
  running: { color: 'warning', text: '训练中' },
  success: { color: 'success', text: '已完成' },
  failed: { color: 'danger', text: '失败' }
}

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await getAllTasks()
    tasks.value = res.data.data
  } finally {
    loading.value = false
  }
}

const loadDatasets = async () => {
  const res = await getAllDatasets()
  datasets.value = res.data.data
}

const handleAdd = () => {
  form.taskName = ''
  form.datasetId = ''
  form.modelType = ''
  form.enableHierarchicalLoss = false
  form.trainParams = ''
  modalVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  try {
    await createTask(form)
    ElMessage.success('训练任务创建成功')
    modalVisible.value = false
    loadTasks()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '创建失败')
  }
}

const handleStart = async (id) => {
  try {
    await startTask(id)
    ElMessage.success('训练任务已启动')
    loadTasks()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '启动失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTask(id)
    ElMessage.success('删除成功')
    loadTasks()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadTasks()
  loadDatasets()

  intervalId = setInterval(() => {
    const runningTasks = tasks.value.filter(t => t.status === 'running')
    if (runningTasks.length > 0) {
      loadTasks()
    }
  }, 3000)
})

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId)
  }
})
</script>
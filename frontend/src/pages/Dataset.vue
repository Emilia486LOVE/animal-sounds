<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">数据集管理</h1>
      </div>
      <div class="page-header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索数据集"
          style="width: 280px"
          prefix-icon="Search"
        />
        <el-button type="primary" icon="Plus" @click="handleAdd">创建数据集</el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table
        :data="filteredDatasets"
        row-key="datasetId"
        :loading="loading"
        size="small"
      >
        <el-table-column prop="datasetName" label="数据集名称" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="audioCount" label="音频数量" width="100">
          <template #default="scope">
            <span class="number-font">{{ scope.row.audioCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="scope">
            {{ dayjs(scope.row.createTime).format('YYYY-MM-DD HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(scope.row.datasetId)" style="color: #F53F3F">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      :title="isEdit ? '编辑数据集' : '创建数据集'"
      v-model="modalVisible"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="数据集名称" prop="datasetName">
          <el-input v-model="form.datasetName" placeholder="请输入数据集名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入数据集描述" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllDatasets, createDataset, updateDataset, deleteDataset } from '../api/dataset'
import dayjs from 'dayjs'

const datasets = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editingId = ref(null)
const searchKeyword = ref('')

const form = reactive({
  datasetName: '',
  description: ''
})

const rules = {
  datasetName: [{ required: true, message: '请输入数据集名称', trigger: 'blur' }]
}

const filteredDatasets = computed(() => {
  return datasets.value.filter(d => 
    d.datasetName.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    (d.description && d.description.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  )
})

const loadDatasets = async () => {
  loading.value = true
  try {
    const res = await getAllDatasets()
    if (res.data && res.data.data) {
      datasets.value = res.data.data
    } else {
      ElMessage.error('获取数据失败')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '获取数据集失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  form.datasetName = ''
  form.description = ''
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  editingId.value = record.datasetId
  form.datasetName = record.datasetName
  form.description = record.description || ''
  modalVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await deleteDataset(id)
    ElMessage.success('删除成功')
    loadDatasets()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  
  try {
    if (isEdit.value) {
      await updateDataset(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createDataset(form)
      ElMessage.success('创建成功')
    }
    modalVisible.value = false
    loadDatasets()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败'))
  }
}

onMounted(() => {
  loadDatasets()
})
</script>

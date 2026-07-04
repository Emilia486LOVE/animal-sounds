<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">标签管理</h1>
      </div>
      <div class="page-header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd">创建标签</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索标签名称、描述或层级..."
        prefix-icon="Search"
        style="width: 320px"
      />
    </div>

    <div class="grid-2col">
      <div class="card-container">
        <div class="card-header">
          <span class="card-title">标签树</span>
        </div>
        <div class="card-body" style="height: 400px; overflow: auto">
          <el-tree
            :data="treeData"
            :props="treeProps"
            default-expand-all
            show-line
          />
        </div>
      </div>

      <div class="card-container">
        <div class="card-header">
          <span class="card-title">标签列表</span>
        </div>
        <div style="height: 400px; overflow: auto">
          <el-table
            :data="filteredLabels"
            row-key="labelId"
            :loading="loading"
            size="small"
          >
            <el-table-column prop="labelName" label="标签名称" show-overflow-tooltip />
            <el-table-column prop="taxonRank" label="层级" width="100" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="labelPath" label="路径" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="150">
              <template #default="scope">
                {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString('zh-CN') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="text" @click="handleDelete(scope.row.labelId)" style="color: #F53F3F">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <div class="card-container mt-lg">
      <div class="card-header">
        <span class="card-title">统计信息</span>
      </div>
      <div class="card-body">
        <div class="grid-3col">
          <div class="stat-item">
            <div class="text-sm text-secondary">标签总数</div>
            <div class="number-font text-title font-bold text-primary">{{ labels.length }}</div>
          </div>
          <div class="stat-item">
            <div class="text-sm text-secondary">叶子节点数</div>
            <div class="number-font text-title font-bold text-primary">{{ leafLabels.length }}</div>
          </div>
          <div class="stat-item">
            <div class="text-sm text-secondary">层级数</div>
            <div class="number-font text-title font-bold text-primary">{{ rankOrder.length }}</div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      :title="selectedLabel ? '编辑标签' : '创建标签'"
      v-model="modalVisible"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="标签名称" prop="labelName">
          <el-input v-model="form.labelName" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="父标签" prop="parentId">
          <el-select v-model="form.parentId" placeholder="选择父标签（根节点留空）">
            <el-option :label="'根节点'" :value="0" />
            <el-option v-for="l in labels" :key="l.labelId" :label="l.labelName" :value="l.labelId" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类层级" prop="taxonRank">
          <el-select v-model="form.taxonRank" placeholder="选择分类层级">
            <el-option v-for="rank in rankOrder" :key="rank" :label="rankNames[rank]" :value="rank" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入标签描述" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" @click="handleSubmit">提交</el-button>
          <el-button @click="closeModal" style="marginLeft: 8">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllLabels, createLabel, updateLabel, deleteLabel } from '../api/label'

const labels = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const formRef = ref(null)
const selectedLabel = ref(null)
const searchText = ref('')

const form = reactive({
  labelName: '',
  parentId: 0,
  taxonRank: '',
  description: ''
})

const rules = {
  labelName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  taxonRank: [{ required: true, message: '请选择分类层级', trigger: 'change' }]
}

const rankOrder = ['kingdom', 'phylum', 'class', 'order', 'family', 'genus', 'species']
const rankNames = {
  kingdom: '界',
  phylum: '门',
  class: '纲',
  order: '目',
  family: '科',
  genus: '属',
  species: '种'
}

const leafLabels = computed(() => {
  return labels.value.filter(l => !labels.value.some(child => child.parentId === l.labelId))
})

const buildTreeData = (items, parentId = 0) => {
  return items
    .filter(item => item.parentId === parentId)
    .map(item => ({
      label: item.labelName,
      key: item.labelId,
      children: buildTreeData(items, item.labelId),
      rank: item.taxonRank
    }))
}

const treeData = computed(() => buildTreeData(labels.value))
const treeProps = {
  children: 'children',
  label: 'label'
}

const filteredLabels = computed(() => {
  return labels.value.filter(item => {
    return searchText.value === '' ||
      item.labelName.toLowerCase().includes(searchText.value.toLowerCase()) ||
      (item.description && item.description.toLowerCase().includes(searchText.value.toLowerCase())) ||
      item.taxonRank.toLowerCase().includes(searchText.value.toLowerCase())
  })
})

const loadLabels = async () => {
  loading.value = true
  try {
    const res = await getAllLabels()
    if (res.data && res.data.data) {
      labels.value = res.data.data
    } else {
      ElMessage.error('获取数据失败')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '获取标签失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.labelName = ''
  form.parentId = 0
  form.taxonRank = ''
  form.description = ''
  selectedLabel.value = null
  modalVisible.value = true
}

const handleEdit = (record) => {
  form.labelName = record.labelName
  form.parentId = record.parentId || 0
  form.taxonRank = record.taxonRank
  form.description = record.description || ''
  selectedLabel.value = record
  modalVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteLabel(id)
    ElMessage.success('删除成功')
    loadLabels()
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
    if (selectedLabel.value) {
      await updateLabel(selectedLabel.value.labelId, form)
      ElMessage.success('更新成功')
    } else {
      await createLabel(form)
      ElMessage.success('创建成功')
    }
    closeModal()
    loadLabels()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || (selectedLabel.value ? '更新失败' : '创建失败'))
  }
}

const closeModal = () => {
  modalVisible.value = false
  selectedLabel.value = null
}

onMounted(() => {
  loadLabels()
})
</script>
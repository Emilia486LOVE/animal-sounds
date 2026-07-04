<template>
  <div>
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">用户管理</h1>
      </div>
      <div class="page-header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd">创建用户</el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table
        :data="users"
        row-key="userId"
        :loading="loading"
        size="small"
      >
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag type="primary">{{ roleOptions.find(r => r.value === scope.row.role)?.label || scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="scope">
            {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString('zh-CN') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleOpenPasswordModal(scope.row.userId, scope.row.username)">修改密码</el-button>
            <el-button v-if="scope.row.status === 1" type="text" @click="handleToggleStatus(scope.row.userId, 1)">禁用</el-button>
            <el-button v-else type="text" @click="handleToggleStatus(scope.row.userId, 0)">启用</el-button>
            <el-button v-if="scope.row.role !== 'admin'" type="text" @click="handleDelete(scope.row.userId)" style="color: #F53F3F">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      :title="editingId ? '编辑用户' : '创建用户'"
      v-model="modalVisible"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="editingId !== null" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="选择角色">
            <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" @click="handleSubmit">提交</el-button>
          <el-button @click="modalVisible = false" style="marginLeft: 8">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog
      title="修改密码"
      v-model="passwordModalVisible"
      width="400px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="passwordForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" @click="handleChangePassword">确认修改</el-button>
          <el-button @click="passwordModalVisible = false" style="marginLeft: 8">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllUsers, createUser, updateUser, deleteUser } from '../api/user'

const users = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const passwordModalVisible = ref(false)
const formRef = ref(null)
const passwordFormRef = ref(null)
const editingId = ref(null)
const changingUserId = ref(null)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  role: '',
  status: 1
})

const passwordForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const passwordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const roleOptions = [
  { value: 'admin', label: '管理员' },
  { value: 'annotator', label: '标注员' },
  { value: 'algorithm', label: '算法工程师' },
  { value: 'guest', label: '访客' }
]

const roleColors = {
  admin: 'danger',
  annotator: 'primary',
  algorithm: 'warning',
  guest: 'info'
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAllUsers()
    users.value = res.data.data
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingId.value = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.role = ''
  form.status = 1
  modalVisible.value = true
}

const handleEdit = (record) => {
  editingId.value = record.userId
  form.username = record.username
  form.password = ''
  form.realName = record.realName
  form.role = record.role
  form.status = record.status
  modalVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadUsers()
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
    if (editingId.value) {
      await updateUser(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    modalVisible.value = false
    loadUsers()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || (editingId.value ? '更新失败' : '创建失败'))
  }
}

const handleOpenPasswordModal = (userId, username) => {
  changingUserId.value = userId
  passwordForm.username = username
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordModalVisible.value = true
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value.validate()
  if (!valid) return

  try {
    await updateUser(changingUserId.value, { password: passwordForm.newPassword })
    ElMessage.success('密码修改成功')
    passwordModalVisible.value = false
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '密码修改失败')
  }
}

const handleToggleStatus = async (userId, currentStatus) => {
  const newStatus = currentStatus === 1 ? 0 : 1
  try {
    await updateUser(userId, { status: newStatus })
    ElMessage.success(newStatus === 1 ? '用户已启用' : '用户已禁用')
    loadUsers()
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadUsers()
})
</script>
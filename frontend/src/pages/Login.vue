<template>
  <div
    style="
      minHeight: '100vh';
      display: 'flex';
      alignItems: 'center';
      justifyContent: 'center';
      backgroundColor: '#121826';
      backgroundImage: `
        linear-gradient(rgba(42, 51, 68, 0.3) 1px, transparent 1px),
        linear-gradient(90deg, rgba(42, 51, 68, 0.3) 1px, transparent 1px)
      `;
      backgroundSize: '20px 20px';
    "
  >
    <el-card
      style="
        width: 420px;
        backgroundColor: '#1A2233';
        border: '1px solid #2A3344';
        borderRadius: 12;
        boxShadow: '0 8px 32px rgba(0, 0, 0, 0.3)';
      "
    >
      <template #header>
        <div style="textAlign: 'center'">
          <div
            style="
              width: 64px;
              height: 64px;
              backgroundColor: '#165DFF';
              borderRadius: 16;
              display: 'flex';
              alignItems: 'center';
              justifyContent: 'center';
              margin: '0 auto 16px';
            "
          >
            <el-icon :size="32" style="color: '#fff'">
            <Mic />
          </el-icon>
          </div>
          <h1
            style="
              fontSize: 24;
              fontWeight: 600;
              color: '#E5E6EB';
              marginBottom: 8;
            "
          >
            动物声纹系统
          </h1>
          <p style="fontSize: 14; color: '#86909C'">
            数据标注与多级分类训练平台
          </p>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            size="large"
            style="width: '100%'; height: 44"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div style="textAlign: 'center'; marginTop: 20">
        <p style="fontSize: 12; color: '#646D7A'; marginBottom: 8">
          默认账号：admin / password
        </p>
        <p style="fontSize: 12; color: '#646D7A'">
          还没有账号？
          <a href="/register" style="color: '#165DFF'">立即注册</a>
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { Mic, User, Lock } from '@element-plus/icons-vue'

const loading = ref(false)
const formRef = ref(null)
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  
  loading.value = true
  try {
    const res = await login(form)
    const { token, user } = res.data.data
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(user))
    ElMessage.success('登录成功')
    window.location.href = '/dashboard'
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

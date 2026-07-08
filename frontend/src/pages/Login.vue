<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <div class="login-icon">
            <el-icon :size="32" class="login-icon-inner">
              <Mic />
            </el-icon>
          </div>
          <h1 class="login-title">动物声纹系统</h1>
          <p class="login-subtitle">数据标注与多级分类训练平台</p>
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
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p class="login-hint">默认账号：admin / password</p>
        <p class="login-register">
          还没有账号？
          <a href="/register" class="login-register-link">立即注册</a>
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

<style scoped>
.login-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-bg-global);
  background-image: 
    linear-gradient(rgba(201, 205, 212, 0.45) 1px, transparent 1px),
    linear-gradient(90deg, rgba(201, 205, 212, 0.45) 1px, transparent 1px);
  background-size: 20px 20px;
  padding: 16px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background-color: var(--color-bg-module);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-modal);
}

.login-header {
  text-align: center;
  padding: 8px 0;
}

.login-icon {
  width: 64px;
  height: 64px;
  background-color: var(--color-brand-primary);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.login-icon-inner {
  color: #fff;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.login-btn {
  width: 100%;
  height: 44px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
}

.login-hint {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}

.login-register {
  font-size: 12px;
  color: var(--color-text-muted);
}

.login-register-link {
  color: var(--color-brand-primary);
  text-decoration: none;
}

.login-register-link:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-card {
    border-radius: var(--radius-md);
    box-shadow: var(--shadow-card);
  }
  
  .login-icon {
    width: 56px;
    height: 56px;
    margin-bottom: 12px;
  }
  
  .login-title {
    font-size: 20px;
  }
}

@media (min-height: 800px) {
  .login-container {
    padding: 40px 16px;
  }
}
</style>

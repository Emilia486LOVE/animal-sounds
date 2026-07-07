<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="register-header">
          <div class="register-icon">
            <el-icon :size="32" class="register-icon-inner">
              <Mic />
            </el-icon>
          </div>
          <h1 class="register-title">注册账号</h1>
          <p class="register-subtitle">创建您的动物声纹系统账号</p>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名（字母、数字、下划线）"
            size="large"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="form.realName"
            placeholder="请输入真实姓名（选填）"
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
            placeholder="请输入密码（至少6位，包含字母和数字）"
            size="large"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              placeholder="请输入图片中的字母"
              size="large"
              class="captcha-input"
            />
            <div class="captcha-action">
              <el-button
                type="text"
                icon="Refresh"
                @click="refreshCaptcha"
                :loading="captchaLoading"
                class="captcha-refresh"
              />
              <img
                :src="captchaImage"
                alt="验证码"
                @click="refreshCaptcha"
                class="captcha-image"
              />
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            size="large"
            class="register-btn"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <p class="register-hint">已有账号？<a href="/login" class="register-link">立即登录</a></p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { register, getCaptcha } from '../api/auth'
import { Mic, User, Lock } from '@element-plus/icons-vue'

const loading = ref(false)
const captchaLoading = ref(false)
const captchaImage = ref('')
const captchaId = ref('')
const formRef = ref(null)
const form = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  captchaCode: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50之间', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const validateUsername = (rule, value, callback) => {
  if (!value) return callback()
  if (!/^[a-zA-Z0-9_]+$/.test(value)) {
    return callback(new Error('用户名只能包含字母、数字和下划线'))
  }
  callback()
}

const validatePassword = (rule, value, callback) => {
  if (!value) return callback()
  if (value.length < 6) {
    return callback(new Error('密码长度至少为6位'))
  }
  if (!/[a-zA-Z]/.test(value)) {
    return callback(new Error('密码必须包含字母'))
  }
  if (!/[0-9]/.test(value)) {
    return callback(new Error('密码必须包含数字'))
  }
  callback()
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) return callback()
  if (form.password !== value) {
    return callback(new Error('两次输入的密码不一致'))
  }
  callback()
}

const refreshCaptcha = async () => {
  captchaLoading.value = true
  try {
    const res = await getCaptcha()
    const { captchaId: id, imageBase64 } = res.data.data
    captchaId.value = id
    captchaImage.value = imageBase64
  } catch (err) {
    ElMessage.error('获取验证码失败')
  } finally {
    captchaLoading.value = false
  }
}

const handleRegister = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  
  loading.value = true
  try {
    await register({
      ...form,
      captchaId: captchaId.value,
      captchaCode: form.captchaCode
    })
    ElMessage.success('注册成功，请登录')
    setTimeout(() => {
      window.location.href = '/login'
    }, 1500)
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '注册失败')
    refreshCaptcha()
    form.captchaCode = ''
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-bg-global);
  background-image: 
    linear-gradient(rgba(42, 51, 68, 0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(42, 51, 68, 0.3) 1px, transparent 1px);
  background-size: 20px 20px;
  padding: 16px;
}

.register-card {
  width: 100%;
  max-width: 460px;
  background-color: var(--color-bg-module);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-modal);
}

.register-header {
  text-align: center;
  padding: 8px 0;
}

.register-icon {
  width: 64px;
  height: 64px;
  background-color: var(--color-brand-primary);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.register-icon-inner {
  color: #fff;
}

.register-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-action {
  display: flex;
  align-items: center;
}

.captcha-refresh {
  padding: 0;
  margin-right: 8px;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid var(--color-border);
}

.register-btn {
  width: 100%;
  height: 44px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
}

.register-hint {
  font-size: 12px;
  color: var(--color-text-muted);
}

.register-link {
  color: var(--color-brand-primary);
  text-decoration: none;
}

.register-link:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .register-card {
    border-radius: var(--radius-md);
    box-shadow: var(--shadow-card);
  }
  
  .register-icon {
    width: 56px;
    height: 56px;
    margin-bottom: 12px;
  }
  
  .register-title {
    font-size: 20px;
  }
  
  .captcha-image {
    width: 100px;
  }
}
</style>
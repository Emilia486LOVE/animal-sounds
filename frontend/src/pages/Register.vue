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
        width: 460px;
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
              <audio />
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
            注册账号
          </h1>
          <p style="fontSize: 14; color: '#86909C'">
            创建您的动物声纹系统账号
          </p>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名（字母、数字、下划线）"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="form.realName"
            placeholder="请输入真实姓名（选填）"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少6位，包含字母和数字）"
            size="large"
            prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="验证码" prop="captchaCode">
          <div style="display: 'flex'; gap: 12">
            <el-input
              v-model="form.captchaCode"
              placeholder="请输入图片中的字母"
              size="large"
              style="flex: 1"
            />
            <div style="display: 'flex'; alignItems: 'center'">
              <el-button
                type="text"
                icon="Refresh"
                @click="refreshCaptcha"
                :loading="captchaLoading"
                style="padding: 0; marginRight: 8"
              />
              <img
                :src="captchaImage"
                alt="验证码"
                @click="refreshCaptcha"
                style="
                  width: 120;
                  height: 40;
                  cursor: 'pointer';
                  borderRadius: 4;
                  border: '1px solid #2A3344';
                "
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
            style="width: '100%'; height: 44"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div style="textAlign: 'center'; marginTop: 20">
        <p style="fontSize: 12; color: '#646D7A'">
          已有账号？<a href="/login" style="color: '#165DFF'">立即登录</a>
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { register, getCaptcha } from '../api/auth'

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

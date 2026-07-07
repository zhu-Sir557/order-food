<template>
  <div class="login-container">
    <div class="login-box">
      <el-card class="login-card" shadow="always">
        <template #header>
          <div class="login-header">
            <el-icon :size="40" color="#ff6034"><Food /></el-icon>
            <h2>智能点餐系统</h2>
            <p>管理后台</p>
          </div>
        </template>
        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          label-width="0"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              size="large"
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              size="large"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useAuthStore } from '@/store/modules/auth'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

async function handleLogin(): Promise<void> {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login({
        username: loginForm.username,
        password: loginForm.password
      })
      authStore.login(res.token, res.adminInfo)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch {
      // 错误已在拦截器中处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6034 0%, #ff8a65 50%, #ffab91 100%);
}

.login-box {
  width: 420px;
}

.login-card {
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
}

.login-header {
  text-align: center;
}

.login-header h2 {
  margin: 12px 0 4px;
  font-size: 22px;
  color: var(--color-text-primary);
}

.login-header p {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.login-btn {
  width: 100%;
  box-shadow: 0 4px 12px rgba(255, 96, 52, 0.3);
  border-radius: 8px;
}
</style>

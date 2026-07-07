<template>
  <div class="register-page">
    <van-nav-bar title="注册" left-arrow @click-left="router.back()" />

    <div class="register-content">
      <div class="logo">
        <div class="logo-icon-wrap">
          <van-icon name="user-circle-o" size="60" color="#ff6034" />
        </div>
        <div class="title">会员注册</div>
      </div>

      <van-form @submit="handleRegister" class="register-form">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            label="账号"
            placeholder="请输入账号（3-20个字符）"
            :rules="[
              { required: true, message: '请输入账号' },
              { validator: validateUsername, message: '账号长度3-20个字符' }
            ]"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码（6-20个字符）"
            :rules="[
              { required: true, message: '请输入密码' },
              { validator: validatePassword, message: '密码长度6-20个字符' }
            ]"
          />
          <van-field
            v-model="confirmPassword"
            type="password"
            name="confirmPassword"
            label="确认密码"
            left-icon="lock"
            placeholder="请再次输入密码"
            :rules="[
              { required: true, message: '请确认密码' },
              { validator: validateConfirm, message: '两次密码不一致' }
            ]"
          />
        </van-cell-group>

        <div class="register-btn">
          <van-button
            type="primary"
            block
            round
            size="large"
            :loading="loading"
            loading-text="注册中..."
            native-type="submit"
          >
            注册
          </van-button>
        </div>

        <div class="login-link">
          已有账号？
          <span @click="router.push('/login')">立即登录</span>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import router from '@/router'
import { showToast } from 'vant'
import { useMemberStore } from '@/store/modules/member'

const memberStore = useMemberStore()

const form = reactive({
  username: '',
  password: '',
})

const confirmPassword = ref('')
const loading = ref(false)

/** 校验用户名 */
const validateUsername = (val: string): boolean => {
  return val.length >= 3 && val.length <= 20
}

/** 校验密码 */
const validatePassword = (val: string): boolean => {
  return val.length >= 6 && val.length <= 20
}

/** 校验确认密码 */
const validateConfirm = (val: string): boolean => {
  return val === form.password
}

/** 注册 */
const handleRegister = async (): Promise<void> => {
  loading.value = true
  try {
    await memberStore.register({
      username: form.username,
      password: form.password,
    })
    showToast('注册成功')
    router.replace('/me')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff5f0 0%, #f7f8fa 30%, #f7f8fa 100%);
}

.register-content {
  padding: 20px 0;
}

.logo {
  text-align: center;
  padding: 40px 0 30px;
}

.logo-icon-wrap {
  width: 80px;
  height: 80px;
  border-radius: 24px;
  background: linear-gradient(135deg, #fff5f0, #ffe4d7);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(255, 96, 52, 0.15);
}

.title {
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-top: 10px;
}

.register-form {
  margin-top: 20px;
}

.register-form :deep(.van-cell-group--inset) {
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.register-btn {
  padding: 20px 16px;
}

.register-btn :deep(.van-button--primary) {
  box-shadow: var(--shadow-primary);
}

.login-link {
  text-align: center;
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
  padding-bottom: 20px;
}

.login-link span {
  color: var(--color-primary);
  font-weight: 500;
}
</style>

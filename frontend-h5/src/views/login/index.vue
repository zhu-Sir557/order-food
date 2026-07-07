<template>
  <div class="login-page">
    <van-nav-bar title="登录" left-arrow @click-left="router.back()" />

    <div class="login-content">
      <div class="logo">
        <div class="logo-icon-wrap">
          <van-icon name="user-circle-o" size="60" color="#ff6034" />
        </div>
        <div class="title">会员登录</div>
      </div>

      <van-form @submit="onLoginClick" class="login-form">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            label="账号"
            placeholder="请输入账号"
            :rules="[{ required: true, message: '请输入账号' }]"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>

        <div class="login-btn">
          <van-button
            type="primary"
            block
            round
            size="large"
            :loading="loading"
            loading-text="登录中..."
            native-type="submit"
          >
            登录
          </van-button>
        </div>

        <div class="register-link">
          还没有账号？
          <span @click="router.push('/register')">立即注册</span>
        </div>
      </van-form>
    </div>

    <!-- 滑块验证弹窗 -->
    <van-popup
      v-model:show="showCaptcha"
      round
      closeable
      close-icon="cross"
      :style="{ width: '340px', padding: '20px' }"
      @close="onCaptchaClose"
    >
      <div class="captcha-popup-title">滑动验证</div>
      <SliderCaptcha
        v-if="showCaptcha"
        @success="onCaptchaSuccess"
        @fail="onCaptchaFail"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import router from '@/router'
import { showToast } from 'vant'
import { useMemberStore } from '@/store/modules/member'
import SliderCaptcha from '@/components/SliderCaptcha.vue'

const memberStore = useMemberStore()

const form = reactive({
  username: '',
  password: '',
})

const showCaptcha = ref(false)
const captchaToken = ref('')
const loading = ref(false)

/** 点击登录按钮 — 弹出滑块验证 */
const onLoginClick = (): void => {
  captchaToken.value = ''
  showCaptcha.value = true
}

/** 滑块验证成功 — 自动登录 */
const onCaptchaSuccess = async (token: string): Promise<void> => {
  captchaToken.value = token
  showCaptcha.value = false
  loading.value = true
  try {
    await memberStore.login({
      username: form.username,
      password: form.password,
      captchaToken: captchaToken.value,
    })
    showToast('登录成功')
    router.replace('/me')
  } catch (error) {
    console.error('登录失败:', error)
    captchaToken.value = ''
  } finally {
    loading.value = false
  }
}

/** 滑块验证失败 */
const onCaptchaFail = (): void => {
  captchaToken.value = ''
}

/** 弹窗关闭 */
const onCaptchaClose = (): void => {
  captchaToken.value = ''
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff5f0 0%, #f7f8fa 30%, #f7f8fa 100%);
}

.login-content {
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

.login-form {
  margin-top: 20px;
}

.login-form :deep(.van-cell-group--inset) {
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.login-btn {
  padding: 20px 16px;
}

.login-btn :deep(.van-button--primary) {
  box-shadow: var(--shadow-primary);
}

.register-link {
  text-align: center;
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
  padding-bottom: 20px;
}

.register-link span {
  color: var(--color-primary);
  font-weight: 500;
}

.captcha-popup-title {
  text-align: center;
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  margin-bottom: 16px;
}
</style>

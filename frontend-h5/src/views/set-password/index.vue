<template>
  <div class="set-password-page">
    <van-nav-bar title="设置密码" left-arrow @click-left="router.back()" />

    <div class="set-tip">
      设置登录密码后，可使用「账号+密码」或「手机号+密码」方式登录。密码至少 8 位。
    </div>

    <van-form @submit="onSubmit" class="set-form">
      <van-cell-group inset>
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码（至少8位）"
          maxlength="20"
          :rules="[
            { required: true, message: '请输入密码' },
            { validator: validatePassword, message: '密码至少8位' }
          ]"
        />
        <van-field
          v-model="confirmPassword"
          type="password"
          name="confirmPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: validateConfirm, message: '两次密码不一致' }
          ]"
        />
      </van-cell-group>

      <div class="set-btn">
        <van-button
          type="primary"
          block
          round
          size="large"
          :loading="loading"
          loading-text="设置中..."
          native-type="submit"
        >
          设置密码
        </van-button>
      </div>
    </van-form>

    <!-- 滑块验证弹窗 -->
    <van-popup
      v-model:show="showCaptcha"
      round
      closeable
      close-icon="cross"
      :style="{ width: '340px', padding: '20px' }"
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
  password: '',
})
const confirmPassword = ref('')
const loading = ref(false)
const showCaptcha = ref(false)
const captchaToken = ref('')

/** 校验密码长度（8-20） */
const validatePassword = (val: string): boolean => val.length >= 8 && val.length <= 20

/** 校验两次密码一致 */
const validateConfirm = (val: string): boolean => val === form.password

/** 提交：校验 → 弹滑块 */
const onSubmit = (): void => {
  if (form.password.length < 8) {
    showToast('密码至少8位')
    return
  }
  if (form.password !== confirmPassword.value) {
    showToast('两次密码不一致')
    return
  }
  captchaToken.value = ''
  showCaptcha.value = true
}

/** 滑块验证成功 → 调用设密码接口 */
const onCaptchaSuccess = async (token: string): Promise<void> => {
  captchaToken.value = token
  showCaptcha.value = false
  loading.value = true
  try {
    await memberStore.setPassword({ password: form.password, captchaToken: token })
    showToast('密码设置成功')
    router.back()
  } catch (error) {
    console.error('设置密码失败:', error)
    captchaToken.value = ''
  } finally {
    loading.value = false
  }
}

/** 滑块验证失败 */
const onCaptchaFail = (): void => {
  captchaToken.value = ''
}
</script>

<style scoped>
.set-password-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff5f0 0%, #f7f8fa 30%, #f7f8fa 100%);
}

.set-tip {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-secondary);
  line-height: 1.6;
  padding: 16px var(--space-page-x) 4px;
}

.set-form {
  margin-top: 12px;
}

.set-form :deep(.van-cell-group--inset) {
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.set-btn {
  padding: 20px 16px;
}

.set-btn :deep(.van-button--primary) {
  box-shadow: var(--shadow-primary);
}

.captcha-popup-title {
  text-align: center;
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  margin-bottom: 16px;
}
</style>

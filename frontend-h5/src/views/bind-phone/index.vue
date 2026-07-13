<template>
  <div class="bind-phone-page">
    <van-nav-bar title="绑定手机" left-arrow @click-left="router.back()" />

    <div class="bind-tip">
      为保障账号安全，请绑定您的手机号。仅支持首次绑定，绑定后不可改绑。
    </div>

    <van-form @submit="onSubmit" class="bind-form">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          type="tel"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          maxlength="11"
          :rules="[
            { required: true, message: '请输入手机号' },
            { pattern: PHONE_REGEX, message: '请输入正确的手机号' }
          ]"
        >
          <template #button>
            <van-button
              size="small"
              type="primary"
              :disabled="countdown > 0"
              @click="onSendCode"
            >
              <van-count-down
                v-if="countdown > 0"
                :time="countdown"
                format="ss"
                @finish="onCountdownFinish"
              />
              <span v-else>获取验证码</span>
            </van-button>
          </template>
        </van-field>
        <van-field
          v-model="form.code"
          type="tel"
          name="code"
          label="验证码"
          placeholder="请输入6位验证码"
          maxlength="6"
          :rules="[{ required: true, message: '请输入验证码' }]"
        />
      </van-cell-group>

      <div class="bind-btn">
        <van-button
          type="primary"
          block
          round
          size="large"
          :loading="loading"
          loading-text="绑定中..."
          native-type="submit"
        >
          绑定手机
        </van-button>
      </div>
    </van-form>

    <!-- 滑块验证弹窗（发码 / 绑定共用） -->
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
import { sendSmsCode } from '@/api/sms'

const memberStore = useMemberStore()

/** 手机号正则（与后端 ^1[3-9]\d{9}$ 一致） */
const PHONE_REGEX = /^1[3-9]\d{9}$/

const form = reactive({
  phone: '',
  code: '',
})

const loading = ref(false)
const countdown = ref(0)
const showCaptcha = ref(false)
const captchaToken = ref('')

/** 滑块用途：send=获取验证码，bind=提交绑定 */
type CaptchaPurpose = 'send' | 'bind'
const captchaPurpose = ref<CaptchaPurpose>('send')

/** 点击获取验证码：校验手机号 → 弹滑块 */
const onSendCode = (): void => {
  if (!PHONE_REGEX.test(form.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  captchaToken.value = ''
  captchaPurpose.value = 'send'
  showCaptcha.value = true
}

/** 点击提交：校验 → 弹滑块 */
const onSubmit = (): void => {
  if (!PHONE_REGEX.test(form.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  if (!form.code) {
    showToast('请输入验证码')
    return
  }
  captchaToken.value = ''
  captchaPurpose.value = 'bind'
  showCaptcha.value = true
}

/** 滑块验证成功 */
const onCaptchaSuccess = async (token: string): Promise<void> => {
  captchaToken.value = token
  showCaptcha.value = false
  if (captchaPurpose.value === 'send') {
    await doSendCode(token)
  } else {
    await doBind(token)
  }
}

/** 滑块验证失败 */
const onCaptchaFail = (): void => {
  captchaToken.value = ''
}

/** 发送短信验证码（滑块通过后） */
const doSendCode = async (token: string): Promise<void> => {
  try {
    await sendSmsCode(form.phone, token)
    showToast('验证码已发送')
    countdown.value = 60 * 1000
  } catch (error) {
    console.error('发送验证码失败:', error)
    captchaToken.value = ''
  }
}

/** 绑定手机（滑块通过后） */
const doBind = async (token: string): Promise<void> => {
  loading.value = true
  try {
    await memberStore.bindPhone({
      phone: form.phone,
      code: form.code,
      captchaToken: token,
    })
    showToast('绑定成功')
    router.back()
  } catch (error) {
    console.error('绑定手机失败:', error)
    captchaToken.value = ''
  } finally {
    loading.value = false
  }
}

/** 倒计时结束 */
const onCountdownFinish = (): void => {
  countdown.value = 0
}
</script>

<style scoped>
.bind-phone-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff5f0 0%, #f7f8fa 30%, #f7f8fa 100%);
}

.bind-tip {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-secondary);
  line-height: 1.6;
  padding: 16px var(--space-page-x) 4px;
}

.bind-form {
  margin-top: 12px;
}

.bind-form :deep(.van-cell-group--inset) {
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.bind-btn {
  padding: 20px 16px;
}

.bind-btn :deep(.van-button--primary) {
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

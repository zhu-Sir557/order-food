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

      <van-tabs v-model:active="activeTab" swipeable>
        <!-- ① 账号密码 -->
        <van-tab title="账号密码">
          <van-form @submit="onPasswordLoginClick" class="login-form">
            <van-cell-group inset>
              <van-field
                v-model="passwordForm.username"
                name="username"
                label="账号"
                placeholder="请输入账号"
                :rules="[{ required: true, message: '请输入账号' }]"
              />
              <van-field
                v-model="passwordForm.password"
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
                :loading="passwordLoading"
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
        </van-tab>

        <!-- ② 手机验证码 -->
        <van-tab title="手机验证码">
          <van-form @submit="onSmsLoginClick" class="login-form">
            <van-cell-group inset>
              <van-field
                v-model="smsForm.phone"
                type="tel"
                name="phone"
                label="手机号"
                placeholder="请输入手机号"
                maxlength="11"
                :rules="[
                  { required: true, message: '请输入手机号' },
                  { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
                ]"
              >
                <template #button>
                  <van-button
                    size="small"
                    type="primary"
                    :disabled="countdownTime > 0"
                    @click="onSendCodeClick"
                  >
                    <van-count-down
                      v-if="countdownTime > 0"
                      :time="countdownTime"
                      format="ss"
                      @finish="onCountdownFinish"
                    />
                    <span v-else>获取验证码</span>
                  </van-button>
                </template>
              </van-field>
              <van-field
                v-model="smsForm.code"
                type="tel"
                name="code"
                label="验证码"
                placeholder="请输入6位验证码"
                maxlength="6"
                :rules="[{ required: true, message: '请输入验证码' }]"
              />
            </van-cell-group>

            <div class="login-btn">
              <van-button
                type="primary"
                block
                round
                size="large"
                :loading="smsLoading"
                loading-text="登录中..."
                native-type="submit"
              >
                登录
              </van-button>
            </div>

            <div class="tip-text">未注册的手机号将自动创建账号</div>
          </van-form>
        </van-tab>
      </van-tabs>
    </div>

    <!-- 滑块验证弹窗（账号密码登录 & 获取验证码共用） -->
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
import { sendSmsCode } from '@/api/sms'

const memberStore = useMemberStore()

/** 当前激活的 Tab：0=账号密码，1=手机验证码 */
const activeTab = ref(0)

const passwordForm = reactive({
  username: '',
  password: '',
})

const smsForm = reactive({
  phone: '',
  code: '',
})

const showCaptcha = ref(false)
/** 滑块用途：password=账号密码登录，sms=获取短信验证码 */
const captchaPurpose = ref<'password' | 'sms'>('password')
const captchaToken = ref('')

const passwordLoading = ref(false)
const smsLoading = ref(false)
/** 获取验证码倒计时（毫秒）；>0 时按钮禁用并显示剩余秒 */
const countdownTime = ref(0)

/** 账号密码登录：点击登录 → 弹滑块 */
const onPasswordLoginClick = (): void => {
  captchaToken.value = ''
  captchaPurpose.value = 'password'
  showCaptcha.value = true
}

/** 获取验证码：校验手机号 → 弹滑块 */
const onSendCodeClick = (): void => {
  if (!/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  captchaToken.value = ''
  captchaPurpose.value = 'sms'
  showCaptcha.value = true
}

/** 滑块验证成功 */
const onCaptchaSuccess = async (token: string): Promise<void> => {
  captchaToken.value = token
  showCaptcha.value = false
  if (captchaPurpose.value === 'password') {
    await doPasswordLogin()
  } else {
    await doSendSmsCode(token)
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

/** 账号密码登录（滑块通过后） */
const doPasswordLogin = async (): Promise<void> => {
  passwordLoading.value = true
  try {
    await memberStore.login({
      username: passwordForm.username,
      password: passwordForm.password,
      captchaToken: captchaToken.value,
    })
    showToast('登录成功')
    router.replace('/me')
  } catch (error) {
    console.error('登录失败:', error)
    captchaToken.value = ''
  } finally {
    passwordLoading.value = false
  }
}

/** 发送短信验证码（滑块通过后） */
const doSendSmsCode = async (token: string): Promise<void> => {
  try {
    await sendSmsCode(smsForm.phone, token)
    showToast('验证码已发送')
    countdownTime.value = 60 * 1000
  } catch (error) {
    console.error('发送验证码失败:', error)
    captchaToken.value = ''
  }
}

/** 倒计时结束 */
const onCountdownFinish = (): void => {
  countdownTime.value = 0
}

/** 短信验证码登录 */
const onSmsLoginClick = async (): Promise<void> => {
  if (!/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  if (!smsForm.code) {
    showToast('请输入验证码')
    return
  }
  smsLoading.value = true
  try {
    await memberStore.smsLogin({ phone: smsForm.phone, code: smsForm.code })
    showToast('登录成功')
    router.replace('/me')
  } catch (error) {
    console.error('短信登录失败:', error)
  } finally {
    smsLoading.value = false
  }
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

.tip-text {
  text-align: center;
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
  padding: 0 16px;
}

.captcha-popup-title {
  text-align: center;
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  margin-bottom: 16px;
}
</style>

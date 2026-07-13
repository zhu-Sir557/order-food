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
        <!-- ① 账号 + 密码 -->
        <van-tab title="账号密码">
          <van-form @submit="onAccountPasswordSubmit" class="login-form">
            <van-cell-group inset>
              <van-field
                v-model="accountPwdForm.account"
                name="account"
                label="账号"
                placeholder="请输入账号"
                :rules="[{ required: true, message: '请输入账号' }]"
              />
              <van-field
                v-model="accountPwdForm.password"
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
        </van-tab>

        <!-- ② 手机号 + 验证码 -->
        <van-tab title="手机验证码">
          <van-form @submit="onPhoneCodeSubmit" class="login-form">
            <van-cell-group inset>
              <van-field
                v-model="phoneCodeForm.phone"
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
                    @click="onSendPhoneCode"
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
                v-model="phoneCodeForm.code"
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
                :loading="loading"
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

    <!-- 滑块验证弹窗（两种登录 / 发码共用） -->
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
import type { UnifiedLoginData } from '@/types'

const memberStore = useMemberStore()

/** 手机号正则（与后端 ^1[3-9]\d{9}$ 一致） */
const PHONE_REGEX = /^1[3-9]\d{9}$/

/** 当前激活的 Tab：0=账号密码 1=手机验证码 */
const activeTab = ref(0)
/** 统一登录按钮 loading（两种方式共用） */
const loading = ref(false)
const showCaptcha = ref(false)
const captchaToken = ref('')

const accountPwdForm = reactive({ account: '', password: '' })
const phoneCodeForm = reactive({ phone: '', code: '' })

/** 获取验证码倒计时（毫秒）；>0 时按钮禁用并显示剩余秒 */
const countdown = ref(0)

/** 滑块验证通过后待执行的操作（携带一次性 captchaToken） */
let afterCaptcha: ((token: string) => void) | null = null

/** 打开滑块验证弹窗，并记录通过后的回调 */
const openCaptcha = (cb: (token: string) => void): void => {
  captchaToken.value = ''
  afterCaptcha = cb
  showCaptcha.value = true
}

/** 滑块验证成功 */
const onCaptchaSuccess = (token: string): void => {
  captchaToken.value = token
  showCaptcha.value = false
  const cb = afterCaptcha
  afterCaptcha = null
  if (cb) cb(token)
}

/** 滑块验证失败 */
const onCaptchaFail = (): void => {
  captchaToken.value = ''
  afterCaptcha = null
}

/** 统一登录调用（两种方式共用） */
const doLogin = async (data: UnifiedLoginData): Promise<void> => {
  loading.value = true
  try {
    await memberStore.login(data)
    showToast('登录成功')
    router.replace('/me')
  } catch (error) {
    console.error('登录失败:', error)
    captchaToken.value = ''
  } finally {
    loading.value = false
  }
}

/** ① 账号密码登录：弹滑块 → 统一登录 */
const onAccountPasswordSubmit = (): void => {
  openCaptcha((token) =>
    doLogin({
      loginType: 'ACCOUNT_PASSWORD',
      account: accountPwdForm.account,
      password: accountPwdForm.password,
      captchaToken: token,
    })
  )
}

/** ② 手机验证码：先发码（滑块校验） */
const onSendPhoneCode = (): void => {
  if (!PHONE_REGEX.test(phoneCodeForm.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  openCaptcha(async (token) => {
    try {
      await sendSmsCode(phoneCodeForm.phone, token)
      showToast('验证码已发送')
      countdown.value = 60 * 1000
    } catch (error) {
      console.error('发送验证码失败:', error)
      captchaToken.value = ''
    }
  })
}

/** ② 手机验证码：提交登录（再次滑块校验，统一登录携带 code） */
const onPhoneCodeSubmit = (): void => {
  if (!PHONE_REGEX.test(phoneCodeForm.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  if (!phoneCodeForm.code) {
    showToast('请输入验证码')
    return
  }
  openCaptcha((token) =>
    doLogin({
      loginType: 'PHONE_CODE',
      account: phoneCodeForm.phone,
      code: phoneCodeForm.code,
      captchaToken: token,
    })
  )
}

const onCountdownFinish = (): void => {
  countdown.value = 0
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

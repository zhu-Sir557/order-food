<template>
  <div class="me-page">
    <van-nav-bar title="我的" />

    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="avatar" @click="showAvatarSheet = true">
        <img v-if="memberStore.avatar" :src="memberStore.avatar" class="avatar-img" alt="头像" />
        <van-icon v-else name="user-circle-o" size="60" color="#fff" />
        <div class="avatar-edit" v-if="!uploading">
          <van-icon name="edit" size="14" color="#fff" />
        </div>
        <van-loading v-else class="avatar-edit" type="spinner" size="14" color="#fff" />
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          class="hidden-file-input"
          @change="onAvatarChange"
        />
      </div>
      <div class="user-info" v-if="memberStore.isLoggedIn">
        <div class="user-name-row">
          <span class="user-name">{{ memberStore.displayName }}</span>
          <van-icon name="edit" class="edit-icon" @click="showNickPopup = true" />
        </div>
        <div class="user-desc">余额: ¥{{ memberStore.balance.toFixed(2) }}</div>
      </div>
      <div class="user-info" v-else>
        <div class="user-name">食客</div>
        <div class="user-desc">欢迎光临</div>
      </div>
    </div>

    <!-- 会员资料菜单（登录后可见） -->
    <van-cell-group inset class="menu-group" v-if="memberStore.isLoggedIn">
      <van-cell
        v-if="!memberStore.isPhoneBound"
        title="绑定手机"
        icon="phone-o"
        is-link
        @click="router.push('/bind-phone')"
      />
      <van-cell
        v-else
        title="查看绑定手机号"
        icon="phone-o"
        is-link
        @click="showPhoneDialog = true"
      />
      <van-cell title="设置密码" icon="lock" is-link @click="router.push('/set-password')" />
    </van-cell-group>

    <!-- 会员功能菜单 -->
    <van-cell-group inset class="menu-group" v-if="memberStore.isLoggedIn">
      <van-cell title="余额记录" icon="balance-o" is-link @click="router.push('/balance')" />
      <van-cell title="兑换点卡" icon="gift-o" is-link @click="router.push('/recharge')" />
    </van-cell-group>

    <!-- 通用功能菜单 -->
    <van-cell-group inset class="menu-group">
      <van-cell title="我的订单" icon="orders-o" is-link @click="router.push('/order/list')" />
      <van-cell title="桌号信息" icon="location-o" :value="userStore.tableName || '未选择'" @click="router.push('/cart')" />
    </van-cell-group>

    <!-- 会员操作 -->
    <van-cell-group inset class="menu-group" v-if="memberStore.isLoggedIn">
      <van-cell title="退出登录" icon="cross" is-link @click="handleLogout" />
    </van-cell-group>

    <!-- 登录/注册入口 -->
    <van-cell-group inset class="menu-group" v-if="!memberStore.isLoggedIn">
      <van-cell title="登录" icon="user-o" is-link @click="router.push('/login')" />
      <van-cell title="注册" icon="add-o" is-link @click="router.push('/register')" />
    </van-cell-group>

    <van-cell-group inset class="menu-group">
      <van-cell title="关于我们" icon="info-o" is-link @click="goAboutUs" />
      <van-cell
        v-if="contactPhone"
        title="联系商家"
        icon="phone-o"
        is-link
        @click="onContactClick"
      />
    </van-cell-group>

    <!-- 联系商家操作面板 -->
    <van-action-sheet
      v-model:show="showContactSheet"
      :actions="contactActions"
      cancel-text="取消"
      description="联系商家"
      @select="onContactAction"
    />

    <!-- 昵称编辑弹窗 -->
    <EditNicknamePopup
      v-model:show="showNickPopup"
      :current-nickname="memberStore.nickname"
    />

    <!-- 头像操作动作面板 -->
    <van-action-sheet
      v-model:show="showAvatarSheet"
      :actions="avatarActions"
      cancel-text="取消"
      description="头像操作"
      @select="onAvatarAction"
    />

    <!-- 查看绑定手机号弹窗 -->
    <van-dialog v-model:show="showPhoneDialog" title="绑定手机号">
      <div class="phone-dialog-content">您的手机号：{{ memberStore.phoneMasked }}</div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import router from '@/router'
import { showToast, showImagePreview } from 'vant'
import { useUserStore } from '@/store/modules/user'
import { useMemberStore } from '@/store/modules/member'
import { getMerchantConfig } from '@/api/merchant-config'
import EditNicknamePopup from '@/components/EditNicknamePopup.vue'

const userStore = useUserStore()
const memberStore = useMemberStore()

/** 商家联系电话（为空时隐藏「联系商家」入口） */
const contactPhone = ref('')

/** 联系商家操作面板显隐 */
const showContactSheet = ref(false)

/** 联系商家动作面板选项 */
const contactActions: { name: string; value: string }[] = [
  { name: '拨打电话', value: 'call' },
  { name: '复制号码', value: 'copy' },
]

/** 昵称编辑弹窗显隐 */
const showNickPopup = ref(false)

/** 头像操作动作面板显隐 */
const showAvatarSheet = ref(false)

/** 查看绑定手机号弹窗显隐 */
const showPhoneDialog = ref(false)

/** 头像动作面板选项 */
const avatarActions: { name: string }[] = [
  { name: '查看头像' },
  { name: '更换头像' },
]

/** 头像上传 */
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

/** 触发隐藏文件选择 */
const triggerAvatarUpload = (): void => {
  fileInput.value?.click()
}

/** 头像动作面板选择回调 */
const onAvatarAction = (action: { name?: string }): void => {
  const name = action.name ?? ''
  if (name === '查看头像') {
    if (!memberStore.avatar) {
      showToast('尚未设置头像')
      return
    }
    showImagePreview([memberStore.avatar])
  } else if (name === '更换头像') {
    triggerAvatarUpload()
  }
}

/** 选择图片后上传头像 */
const onAvatarChange = async (e: Event): Promise<void> => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const remaining = await memberStore.uploadAvatar(file)
    showToast(`头像已更新，今日还可修改 ${remaining} 次`)
  } catch (error) {
    console.error('上传头像失败:', error)
  } finally {
    uploading.value = false
    // 复位，保证下次选择同一文件也能触发 change
    target.value = ''
  }
}

/** 退出登录 */
const handleLogout = async (): Promise<void> => {
  memberStore.clearMemberInfo()
  // 重新创建临时用户
  await userStore.initUser()
  showToast('已退出登录')
}

/** 跳转到「关于我们」 */
const goAboutUs = (): void => {
  router.push('/about-us')
}

/** 点击「联系商家」兜底：号码为空时不弹出面板 */
const onContactClick = (): void => {
  if (!contactPhone.value) return
  showContactSheet.value = true
}

/** 联系商家动作面板选择回调 */
const onContactAction = async (action: { name?: string; value?: string }): Promise<void> => {
  const value = action.value
  if (value === 'call') {
    window.location.href = `tel:${contactPhone.value}`
  } else if (value === 'copy') {
    await copyPhone(contactPhone.value)
  }
}

/** 复制联系电话到剪贴板（带降级方案） */
const copyPhone = async (phone: string): Promise<void> => {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(phone)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = phone
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      const ok = document.execCommand('copy')
      document.body.removeChild(textarea)
      if (!ok) throw new Error('copy failed')
    }
    showToast(`已复制：${phone}`)
  } catch {
    showToast('复制失败，请手动复制')
  }
}

/** 拉取商家公开配置（用于「联系商家」显隐与号码） */
const fetchMerchantConfig = async (): Promise<void> => {
  try {
    const res = await getMerchantConfig()
    contactPhone.value = res.contactPhone || ''
  } catch {
    // 业务错误已由请求拦截器统一提示
  }
}

onMounted(() => {
  // 刷新会员信息（含昵称/头像/脱敏手机号）
  if (memberStore.isLoggedIn) {
    memberStore.refreshInfo()
  }
  // 拉取商家配置（联系电话 / 关于我们入口）
  fetchMerchantConfig()
})
</script>

<style scoped>
.me-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.user-card {
  display: flex;
  align-items: center;
  padding: 28px 20px;
  background: linear-gradient(135deg, #ff6034 0%, #ff8a65 50%, #ffab91 100%);
  color: #fff;
  margin-bottom: -20px;
}

.avatar {
  position: relative;
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
  cursor: pointer;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-edit {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hidden-file-input {
  display: none;
}

.user-info {
  margin-left: 16px;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-name {
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-bold);
  color: #fff;
}

.edit-icon {
  font-size: 16px;
  color: #fff;
  opacity: 0.9;
}

.user-desc {
  font-size: var(--font-size-body-sm);
  color: rgba(255, 255, 255, 0.85);
  margin-top: 4px;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 10px;
  border-radius: 12px;
  display: inline-block;
  margin-top: 6px;
}

.menu-group {
  margin-top: 12px;
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.phone-dialog-content {
  padding: 20px 16px;
  text-align: center;
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
}
</style>

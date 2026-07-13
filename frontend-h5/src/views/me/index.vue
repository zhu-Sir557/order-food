<template>
  <div class="me-page">
    <van-nav-bar title="我的" />

    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="avatar">
        <img v-if="memberStore.avatar" :src="memberStore.avatar" class="avatar-img" alt="头像" />
        <van-icon v-else name="user-circle-o" size="60" color="#fff" />
      </div>
      <div class="user-info" v-if="memberStore.isLoggedIn">
        <div class="user-name">{{ memberStore.displayName }}</div>
        <div class="user-desc">余额: ¥{{ memberStore.balance.toFixed(2) }}</div>
      </div>
      <div class="user-info" v-else>
        <div class="user-name">食客</div>
        <div class="user-desc">欢迎光临</div>
      </div>
    </div>

    <!-- 会员资料菜单（登录后可见） -->
    <van-cell-group inset class="menu-group" v-if="memberStore.isLoggedIn">
      <van-cell title="更换头像" icon="smile-o" is-link @click="router.push('/avatar')" />
      <van-cell
        title="修改昵称"
        icon="edit"
        is-link
        :value="memberStore.nickname || ''"
        @click="showNickPopup = true"
      />
      <van-cell title="绑定手机" icon="phone-o" is-link @click="router.push('/bind-phone')" />
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
      <van-cell title="关于我们" icon="info-o" />
      <van-cell title="联系商家" icon="phone-o" />
    </van-cell-group>

    <!-- 昵称编辑弹窗 -->
    <EditNicknamePopup
      v-model:show="showNickPopup"
      :current-nickname="memberStore.nickname"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import router from '@/router'
import { showToast } from 'vant'
import { useUserStore } from '@/store/modules/user'
import { useMemberStore } from '@/store/modules/member'
import EditNicknamePopup from '@/components/EditNicknamePopup.vue'

const userStore = useUserStore()
const memberStore = useMemberStore()

/** 昵称编辑弹窗显隐 */
const showNickPopup = ref(false)

/** 退出登录 */
const handleLogout = async (): Promise<void> => {
  memberStore.clearMemberInfo()
  // 重新创建临时用户
  await userStore.initUser()
  showToast('已退出登录')
}

onMounted(() => {
  // 刷新会员信息（含昵称/头像）
  if (memberStore.isLoggedIn) {
    memberStore.refreshInfo()
  }
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
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  margin-left: 16px;
}

.user-name {
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-bold);
  color: #fff;
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
</style>

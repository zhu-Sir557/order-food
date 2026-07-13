<template>
  <div class="avatar-page">
    <van-nav-bar title="更换头像" left-arrow @click-left="router.back()" />

    <!-- 昵称区域（内联编辑入口） -->
    <van-cell-group inset class="nick-group">
      <van-cell
        title="昵称"
        :value="memberStore.displayName"
        is-link
        @click="showNickPopup = true"
      />
    </van-cell-group>

    <div class="section-title">选择头像</div>

    <div class="avatar-grid" v-if="avatars.length">
      <div
        v-for="item in avatars"
        :key="item.id"
        class="avatar-item"
        :class="{ active: selectedId === item.id }"
        @click="onSelect(item)"
      >
        <img :src="item.ossUrl" :alt="`头像${item.id}`" class="avatar-img" />
        <van-icon v-if="selectedId === item.id" name="success" class="avatar-check" />
      </div>
    </div>

    <van-loading v-if="loading" class="grid-loading" type="spinner" />

    <van-empty v-else-if="!avatars.length" description="暂无头像可选" />

    <div class="avatar-tip" v-if="memberStore.avatarRemaining !== null">
      今日还可修改头像 {{ memberStore.avatarRemaining }} 次
    </div>

    <!-- 昵称编辑弹窗（内联编辑昵称） -->
    <EditNicknamePopup
      v-model:show="showNickPopup"
      :current-nickname="memberStore.nickname"
      @success="onNickSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import router from '@/router'
import { showToast } from 'vant'
import { useMemberStore } from '@/store/modules/member'
import type { AvatarVO } from '@/types'
import EditNicknamePopup from '@/components/EditNicknamePopup.vue'

const memberStore = useMemberStore()

const avatars = ref<AvatarVO[]>([])
const loading = ref(false)
/** 当前选中的头像 id（页面内选中态，便于视觉反馈） */
const selectedId = ref<number | null>(null)
const showNickPopup = ref(false)

/** 拉取头像列表 */
const loadAvatars = async (): Promise<void> => {
  loading.value = true
  try {
    avatars.value = await memberStore.getAvatars()
  } catch (error) {
    console.error('加载头像列表失败:', error)
  } finally {
    loading.value = false
  }
}

/** 选择一个头像并保存 */
const onSelect = async (item: AvatarVO): Promise<void> => {
  selectedId.value = item.id
  try {
    await memberStore.updateAvatar({ avatarId: item.id }, item.ossUrl)
    showToast('头像已更新')
  } catch (error) {
    console.error('更新头像失败:', error)
    selectedId.value = null
  }
}

/** 昵称修改成功（store 已更新昵称） */
const onNickSuccess = (): void => {
  // 昵称回写由 store 处理，此处无需额外逻辑
}

onMounted(async () => {
  await loadAvatars()
  // 预选当前已使用的头像
  if (memberStore.avatar) {
    selectedId.value = avatars.value.find((a) => a.ossUrl === memberStore.avatar)?.id ?? null
  }
})
</script>

<style scoped>
.avatar-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.nick-group {
  margin-top: 12px;
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.section-title {
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  padding: 16px 16px 8px;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 4px 16px 8px;
}

.avatar-item {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 50%;
  overflow: hidden;
  background: var(--color-bg-section);
  border: 2px solid transparent;
  cursor: pointer;
  transition: border-color 0.2s;
}

.avatar-item.active {
  border-color: var(--color-primary);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-check {
  position: absolute;
  right: 2px;
  bottom: 2px;
  color: #fff;
  background: var(--color-primary);
  border-radius: 50%;
  font-size: 14px;
  padding: 1px;
}

.grid-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.avatar-tip {
  text-align: center;
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
  padding: 12px 16px 24px;
}
</style>

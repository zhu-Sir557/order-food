<template>
  <div class="detail-page">
    <van-nav-bar title="消息详情" left-text="返回" left-arrow @click-left="onClickLeft" />

    <div v-if="msg" class="detail-body">
      <h2 class="detail-title">{{ msg.title }}</h2>
      <div class="detail-time">{{ msg.createTime }}</div>

      <img v-if="msg.imageUrl" :src="msg.imageUrl" class="detail-img" alt="图片" />

      <p class="detail-content">{{ msg.content }}</p>

      <van-button
        v-if="msg.linkUrl"
        type="primary"
        block
        round
        class="link-btn"
        @click="openLink"
      >
        查看详情
      </van-button>

      <van-tag v-if="msg.status === 'REVOKED'" type="warning" class="revoked-tag">该消息已撤回</van-tag>
    </div>

    <van-loading v-if="loading" class="center-loading" vertical>加载中...</van-loading>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import router from '@/router'
import { getMessageDetail, readMessage } from '@/api/message'
import type { MessageVO } from '@/api/message'
import { useMessageStore } from '@/store/modules/message'

const route = useRoute()
const messageStore = useMessageStore()
const msg = ref<MessageVO | null>(null)
const loading = ref(false)
const id = Number(route.params.id)

/** 加载详情并标记已读 */
const load = async (): Promise<void> => {
  loading.value = true
  try {
    msg.value = await getMessageDetail(id)
    if (msg.value && !msg.value.isRead) {
      await readMessage(id)
      msg.value.isRead = true
      await messageStore.refreshUnread()
    }
  } catch {
    // 业务错误已由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}

/** 打开跳转链接（基础合法性校验，避免 javascript: 注入） */
const openLink = (): void => {
  const url = msg.value?.linkUrl
  if (!url) return
  if (url.startsWith('/') || url.startsWith('http://') || url.startsWith('https://')) {
    window.location.href = url
  } else {
    showToast('链接不合法')
  }
}

const onClickLeft = (): void => {
  router.back()
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.detail-body {
  padding: 16px;
}

.detail-title {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--color-text-primary);
}

.detail-time {
  color: var(--color-text-secondary);
  font-size: 13px;
  margin-bottom: 12px;
}

.detail-img {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 12px;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: var(--color-text-primary);
}

.link-btn {
  margin-top: 16px;
}

.revoked-tag {
  margin-top: 16px;
}

.center-loading {
  margin-top: 60px;
}
</style>

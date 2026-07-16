<template>
  <div class="message-page">
    <van-nav-bar title="消息中心" />

    <van-empty v-if="!loading && messages.length === 0" description="暂无消息" />

    <van-cell-group inset class="msg-group">
      <van-cell
        v-for="m in messages"
        :key="m.id"
        :title="m.title"
        :label="preview(m)"
        is-link
        @click="openDetail(m)"
      >
        <template #icon>
          <van-badge :dot="!m.isRead" class="msg-dot">
            <van-icon name="comment-o" size="20" class="msg-icon" />
          </van-badge>
        </template>
        <template #value>
          <span class="msg-time">{{ formatTime(m.createTime) }}</span>
        </template>
      </van-cell>
    </van-cell-group>

    <van-loading v-if="loading" class="center-loading" vertical>加载中...</van-loading>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import router from '@/router'
import { getMessageList } from '@/api/message'
import type { MessageVO } from '@/api/message'
import { useMessageStore } from '@/store/modules/message'

const messageStore = useMessageStore()
const messages = ref<MessageVO[]>([])
const loading = ref(false)

/** 加载消息列表，并批量标记可见未读项为已读 */
const loadMessages = async (): Promise<void> => {
  loading.value = true
  try {
    const res = await getMessageList({ page: 1, size: 50 })
    messages.value = res.records
    const unreadIds = messages.value.filter((m) => !m.isRead).map((m) => m.id)
    if (unreadIds.length) {
      await messageStore.markBatchRead(unreadIds)
      messages.value.forEach((m) => {
        if (unreadIds.includes(m.id)) m.isRead = true
      })
    }
  } catch {
    // 业务错误已由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}

/** 列表项摘要 */
const preview = (m: MessageVO): string => {
  const prefix = m.type === 'BROADCAST' ? '[全员] ' : m.type === 'SPECIFIED' ? '[指定] ' : ''
  return prefix + (m.content || '')
}

/** 时间格式化（取日期部分） */
const formatTime = (t?: string): string => (t ? t.slice(0, 10) : '')

const openDetail = (m: MessageVO): void => {
  router.push(`/message/${m.id}`)
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.message-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.msg-group {
  margin-top: 12px;
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.msg-dot {
  margin-right: 10px;
  display: flex;
  align-items: center;
}

.msg-icon {
  color: #ff6034;
}

.msg-time {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.center-loading {
  margin-top: 40px;
}
</style>

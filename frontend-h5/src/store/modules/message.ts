import { defineStore } from 'pinia'
import { messageSocket, type MessagePushPayload } from '@/utils/websocket'
import { getUnreadCount, readMessage, readBatch } from '@/api/message'
import { showToast } from 'vant'

interface MessageState {
  /** 未读消息总数（红点） */
  unreadCount: number
  /** WebSocket 是否已连接 */
  connected: boolean
}

/**
 * 消息 Store
 * 持有 WebSocket 单例、未读计数、最新推送（供红点 / Toast）。
 */
export const useMessageStore = defineStore('message', {
  state: (): MessageState => ({
    unreadCount: 0,
    connected: false,
  }),
  getters: {
    /** 是否有未读 */
    hasUnread: (state): boolean => state.unreadCount > 0,
  },
  actions: {
    /**
     * 初始化 WebSocket 连接（幂等，应在应用挂载后调用一次）。
     */
    init(): void {
      if (this.connected) return
      const token = localStorage.getItem('token')
      if (!token) return
      this.connected = true
      messageSocket.connect(token, {
        onMessage: (payload) => this.onPush(payload),
        onConnected: () => this.refreshUnread(),
        onDisconnected: () => {
          this.connected = false
        },
      })
      this.refreshUnread()
    },

    /**
     * 收到推送消息：更新未读与 Toast。
     * @param payload 推送内容
     */
    onPush(payload: MessagePushPayload): void {
      if (payload.status === 'REVOKED') {
        showToast('一条消息已撤回')
        this.refreshUnread()
        return
      }
      this.unreadCount += 1
      showToast(`收到新消息：${payload.title}`)
    },

    /** 从服务端刷新未读计数 */
    async refreshUnread(): Promise<void> {
      try {
        this.unreadCount = await getUnreadCount()
      } catch {
        // 401 等已由请求拦截器统一处理
      }
    },

    /** 标记单条已读 */
    async markRead(id: number): Promise<void> {
      try {
        await readMessage(id)
      } catch {
        // 忽略
      }
    },

    /** 批量标记已读并刷新红点 */
    async markBatchRead(ids: number[]): Promise<void> {
      if (!ids.length) return
      try {
        await readBatch(ids)
      } catch {
        // 忽略
      }
      await this.refreshUnread()
    },

    /** 断开 WebSocket */
    disconnect(): void {
      messageSocket.disconnect()
      this.connected = false
    },
  },
})

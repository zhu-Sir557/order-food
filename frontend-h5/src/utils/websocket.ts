import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import router from '@/router'

/** WebSocket 端点（启用 SockJS） */
const WS_ENDPOINT = '/ws'
/** 个人队列（Spring user destination） */
const USER_QUEUE = '/user/queue/messages'
/** 广播主题 */
const BROADCAST_TOPIC = '/topic/messages/broadcast'

/** 重连基础间隔（ms） */
const RECONNECT_BASE_MS = 1000
/** 重连最大间隔（ms） */
const RECONNECT_MAX_MS = 30000
/** 心跳间隔（ms） */
const HEARTBEAT_MS = 10000

/** 推送消息载荷（与后端 MessagePushVO 对齐） */
export interface MessagePushPayload {
  id: number
  type: string
  title: string
  content?: string
  imageUrl?: string
  linkUrl?: string
  status: string
  createTime?: string
}

/** WebSocket 事件回调 */
export interface MessageSocketHandlers {
  /** 收到推送消息 */
  onMessage: (payload: MessagePushPayload) => void
  /** 连接成功 */
  onConnected?: () => void
  /** 连接断开 */
  onDisconnected?: () => void
}

/**
 * 消息 WebSocket 客户端（单例）
 *
 * <p>基于 @stomp/stompjs + sockjs-client：
 * <ul>
 *   <li>令牌通过查询参数 ?token= 传递（SockJS 不转发自定义请求头，握手拦截器同时支持 Authorization 头与 ?token=）；</li>
 *   <li>订阅个人队列 /user/queue/messages 与广播主题 /topic/messages/broadcast；</li>
   *   <li>断线后指数退避重连（1s → 30s 上限），心跳 10s；</li>
   *   <li>认证失败（握手被拒 / STOMP 401）：从未连上时经有限次（3 次）重试后清除登录态并跳转登录页，避免无限重连。</li>
   * </ul>
 * </p>
 */
class MessageSocket {
  private client: Client | null = null
  private token = ''
  private handlers: MessageSocketHandlers | null = null
  private reconnectAttempts = 0
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private manualClose = false
  /** 是否曾成功建立 STOMP 会话（用于区分「初始建连失败」与「已连接后掉线」） */
  private hasConnected = false
  /** 握手鉴权失败（从未连上）时的最大重试次数，超过则跳登录 */
  private readonly AUTH_RETRY_LIMIT = 3
  /** 是否已触发跳登录，防止重复弹窗/重复重定向 */
  private redirectingToLogin = false

  /** 建立连接 */
  connect(token: string, handlers: MessageSocketHandlers): void {
    this.token = token
    this.handlers = handlers
    this.manualClose = false
    // 重置鉴权失败重试与连接状态：支持重新登录后再次连接（store.init 会再次调用）
    this.hasConnected = false
    this.redirectingToLogin = false
    this.reconnectAttempts = 0
    this.start()
  }

  private start(): void {
    if (!this.token) return
    const client = new Client({
      // SockJS 不转发自定义请求头，令牌放查询参数
      webSocketFactory: () => new SockJS(`${WS_ENDPOINT}?token=${encodeURIComponent(this.token)}`),
      reconnectDelay: 0,
      heartbeatIncoming: HEARTBEAT_MS,
      heartbeatOutgoing: HEARTBEAT_MS,
      connectHeaders: { Authorization: `Bearer ${this.token}` },
      onConnect: () => {
        this.reconnectAttempts = 0
        // 标记已成功建立连接：此后断线走正常指数退避重连，不再触发跳登录
        this.hasConnected = true
        client.subscribe(USER_QUEUE, (m: IMessage) => this.handleFrame(m))
        client.subscribe(BROADCAST_TOPIC, (m: IMessage) => this.handleFrame(m))
        this.handlers?.onConnected?.()
      },
      onStompError: (frame) => {
        const msg = frame.headers['message'] || ''
        if (msg.includes('401') || msg.toLowerCase().includes('unauthorized')) {
          this.redirectLogin()
        }
      },
      onWebSocketClose: () => {
        this.handlers?.onDisconnected?.()
        if (this.manualClose) return
        // 从未成功连接过（如握手阶段令牌无效/过期被拒）：有限次重试后跳登录，避免无限重连
        if (!this.hasConnected && this.reconnectAttempts >= this.AUTH_RETRY_LIMIT) {
          this.redirectLogin()
          return
        }
        this.scheduleReconnect()
      },
      onWebSocketError: () => {
        if (this.manualClose) return
        if (!this.hasConnected && this.reconnectAttempts >= this.AUTH_RETRY_LIMIT) {
          this.redirectLogin()
          return
        }
        this.scheduleReconnect()
      },
    })
    this.client = client
    client.activate()
  }

  private handleFrame(m: IMessage): void {
    try {
      const payload = JSON.parse(m.body) as MessagePushPayload
      this.handlers?.onMessage(payload)
    } catch (e) {
      console.error('解析 WS 消息失败', e)
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimer != null) return
    const delay = Math.min(RECONNECT_BASE_MS * 2 ** this.reconnectAttempts, RECONNECT_MAX_MS)
    this.reconnectAttempts++
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.start()
    }, delay)
  }

  private redirectLogin(): void {
    // 防止重复弹窗 / 重复重定向（与 request.ts 的 isRedirectingToLogin 防重逻辑一致）
    if (this.redirectingToLogin) return
    this.redirectingToLogin = true
    localStorage.removeItem('token')
    localStorage.removeItem('memberId')
    localStorage.removeItem('memberUsername')
    localStorage.removeItem('memberBalance')
    localStorage.removeItem('tempUserId')
    this.manualClose = true
    this.disconnect()
    if (router.currentRoute.value.path !== '/login') {
      router.push('/login')
    }
  }

  /** 主动断开 */
  disconnect(): void {
    this.manualClose = true
    if (this.reconnectTimer != null) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.client?.deactivate()
    this.client = null
  }
}

/** 全局单例 */
export const messageSocket = new MessageSocket()

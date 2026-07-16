import request from './request'
import type { PageResult } from '@/types'

/** 后台消息视图 */
export interface MessageVO {
  id: number
  type: string
  receiverScope: string
  senderId?: number
  title: string
  content?: string
  imageUrl?: string
  linkUrl?: string
  status: string
  /** 是否可撤回（后台视角） */
  revocable?: boolean
  createTime?: string
  isRead?: boolean
}

/** 后台发送消息请求 */
export interface MessageSendDTO {
  type: 'BROADCAST' | 'SPECIFIED'
  title: string
  content: string
  imageUrl?: string
  linkUrl?: string
  /** 指定用户时必填（会员ID列表） */
  receiverIds?: number[]
}

/** 未读计数 */
export interface MessageUnreadVO {
  unreadCount: number
}

/** 消息列表查询参数 */
export interface MessageQuery {
  page?: number
  size?: number
  type?: string
  scope?: string
  status?: string
  keyword?: string
}

/** 发送消息 */
export function sendMessage(data: MessageSendDTO): Promise<MessageVO> {
  return request.post('/api/admin/message/send', data)
}

/** 消息发送记录（分页） */
export function getMessageList(params: MessageQuery): Promise<PageResult<MessageVO>> {
  return request.get('/api/admin/message/list', { params })
}

/** 消息详情 */
export function getMessageDetail(id: number): Promise<MessageVO> {
  return request.get(`/api/admin/message/${id}`)
}

/** 撤回消息 */
export function revokeMessage(id: number): Promise<void> {
  return request.post(`/api/admin/message/${id}/revoke`)
}

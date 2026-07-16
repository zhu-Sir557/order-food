import request from './request'
import type { PageResult } from '@/types'

/** H5 消息视图（与后端 MessageVO 对齐） */
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
  createTime?: string
  isRead?: boolean
}

/** 未读计数响应 */
export interface MessageUnreadVO {
  unreadCount: number
}

/** 消息中心列表 */
export function getMessageList(params: { page?: number; size?: number }): Promise<PageResult<MessageVO>> {
  return request.get('/api/h5/message/list', { params })
}

/** 消息详情 */
export function getMessageDetail(id: number): Promise<MessageVO> {
  return request.get(`/api/h5/message/${id}`)
}

/** 标记单条已读 */
export function readMessage(id: number): Promise<void> {
  return request.put(`/api/h5/message/${id}/read`)
}

/** 批量标记已读 */
export function readBatch(ids: number[]): Promise<void> {
  return request.put('/api/h5/message/read-batch', ids)
}

/** 未读计数 */
export async function getUnreadCount(): Promise<number> {
  // 响应拦截器已解包返回业务体（res.data），故运行时 data 即为 MessageUnreadVO。
  // axios 泛型将返回类型推断为 AxiosResponse<MessageUnreadVO>，此处转型以对齐运行时行为。
  const data = await request.get<MessageUnreadVO>('/api/h5/message/unread-count')
  return (data as unknown as MessageUnreadVO).unreadCount
}

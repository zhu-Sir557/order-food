import dayjs from 'dayjs'

/**
 * 格式化价格，显示两位小数并加上人民币符号
 * @param price - 价格数值
 * @returns 格式化后的价格字符串
 */
export const formatPrice = (price: number): string => '¥' + price.toFixed(2)

/**
 * 格式化完整日期时间
 * @param date - 日期字符串
 * @returns 格式化后的日期字符串 YYYY-MM-DD HH:mm:ss
 */
export const formatDate = (date: string): string => dayjs(date).format('YYYY-MM-DD HH:mm:ss')

/**
 * 格式化短日期时间
 * @param date - 日期字符串
 * @returns 格式化后的短日期字符串 MM-DD HH:mm
 */
export const formatShortDate = (date: string): string => dayjs(date).format('MM-DD HH:mm')

/**
 * 根据订单状态码获取状态文本
 * @param status - 订单状态码
 * @returns 状态文本
 */
export const orderStatusText = (status: number): string => {
  const map: Record<number, string> = {
    0: '待支付',
    1: '待接单',
    2: '制作中',
    3: '已完成',
    4: '已取餐',
    5: '已取消',
  }
  return map[status] || '未知'
}

/**
 * 根据订单状态码获取状态颜色
 * @param status - 订单状态码
 * @returns 颜色值
 */
export const orderStatusColor = (status: number): string => {
  const map: Record<number, string> = {
    0: '#909399',
    1: '#e6a23c',
    2: '#409eff',
    3: '#67c23a',
    4: '#67c23a',
    5: '#f56c6c',
  }
  return map[status] || '#909399'
}

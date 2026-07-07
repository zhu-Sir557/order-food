import dayjs from 'dayjs'

/** 格式化价格 */
export const formatPrice = (price: number): string => '¥' + price.toFixed(2)

/** 格式化完整日期时间 */
export const formatDate = (date: string): string => dayjs(date).format('YYYY-MM-DD HH:mm:ss')

/** 格式化短日期时间 */
export const formatShortDate = (date: string): string => dayjs(date).format('MM-DD HH:mm')

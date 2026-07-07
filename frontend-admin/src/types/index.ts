/** 菜品 */
export interface Dish {
  id: number
  categoryId: number
  categoryName?: string
  name: string
  price: number
  image: string
  description: string
  stock: number
  status: number
  createTime: string
  tasteConfig?: string
}

/** 分类 */
export interface Category {
  id: number
  name: string
  sort: number
  status: number
  dishCount?: number
}

/** 订单项 */
export interface OrderItem {
  id: number
  orderId: number
  dishId: number
  dishName: string
  dishPrice: number
  dishImage: string
  quantity: number
  subtotal: number
}

/** 订单 */
export interface Order {
  id: number
  orderNo: string
  tableId: number
  tableCode: string
  totalAmount: number
  status: number
  statusText?: string
  remark: string
  createTime: string
  items?: OrderItem[]
}

/** 桌台 */
export interface DiningTable {
  id: number
  code: string
  name: string
  capacity: number
  status: number
}

/** 轮播图 */
export interface Banner {
  id: number
  title: string
  image: string
  link: string
  sort: number
  status: number
  createTime: string
}

/** 仪表盘统计 */
export interface Dashboard {
  todayOrderCount: number
  todayRevenue: number
  tableUsage: number
  totalDishCount: number
  revenueTrend: { date: string; revenue: number }[]
  topDishes: { dishName: string; orderCount: number }[]
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/** 充值点卡 */
export interface Card {
  id: number
  cardNo: string
  cardPassword: string
  amount: number
  status: number
  statusText: string
  memberId: number | null
  memberName: string | null
  assignedAt: string | null
  usedAt: string | null
  createTime: string
}

/** 后台会员 */
export interface AdminMember {
  id: number
  username: string
  balance: number
  createTime: string
}

/** 余额变动记录 */
export interface BalanceRecord {
  id: number
  type: number
  typeText: string
  amount: number
  balanceAfter: number
  cardNo: string | null
  orderNo: string | null
  remark: string
  createTime: string
}

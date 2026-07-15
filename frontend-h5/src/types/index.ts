export interface Dish {
  id: number
  categoryId: number
  name: string
  price: number
  image: string
  description: string
  tasteConfig?: string
}

/** 轮播图 */
export interface Banner {
  id: number
  title?: string
  image: string
  link?: string
  sort: number
}

export interface Category {
  id: number
  name: string
}

export interface CartItem {
  dishId: number
  name: string
  price: number
  image: string
  quantity: number
  tasteSelection?: string
}

export interface OrderItem {
  id: number
  dishId: number
  dishName: string
  dishPrice: number
  dishImage: string
  quantity: number
  subtotal: number
  tasteSelection?: string
}

export interface DiningTable {
  id: number
  code: string
  name: string
  capacity: number
  status: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/** 会员信息 */
export interface MemberInfo {
  memberId: number
  username: string
  balance: number
  createTime: string
  nickname?: string
  avatar?: string
  /** 脱敏后的绑定手机号（未绑定则不返回该字段） */
  phoneMasked?: string
}

/** 统一登录请求（两种组合） */
export type LoginType = 'ACCOUNT_PASSWORD' | 'PHONE_CODE' | 'PHONE_PASSWORD' | 'ACCOUNT_CODE'

export interface UnifiedLoginData {
  loginType: LoginType
  account: string
  password?: string
  code?: string
  captchaToken: string
}

/** 绑定手机请求 */
export interface BindPhoneData {
  phone: string
  code: string
  captchaToken: string
}

/** 设置密码请求 */
export interface SetPasswordData {
  password: string
  captchaToken: string
}

/** 修改昵称请求 */
export interface UpdateNicknameData {
  nickname: string
}

/** 上传头像响应 */
export interface AvatarUpdateResult {
  /** 新头像地址 */
  avatar: string
  /** 当日剩余可修改次数 */
  remaining: number
}

/** 修改次数回执 */
export interface ChangeLimitVO {
  remaining: number
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

/** 我的点卡（已发放待兑换） */
export interface MyCard {
  id: number
  cardNo: string
  cardPassword: string
  amount: number
  status: number
  statusText: string
  assignedAt: string
  createTime: string
}

/** 订单（扩展 memberId, payMethod 字段） */
export interface Order {
  id: number
  orderNo: string
  tableId: number
  tableCode: string
  totalAmount: number
  status: number
  payMethod?: number
  payMethodText?: string
  remark: string
  createTime: string
  items: OrderItem[]
}

/** 商家公开配置（H5 关于我们 / 联系商家） */
export interface MerchantConfigPublic {
  aboutUsContent?: string
  contactPhone?: string
}

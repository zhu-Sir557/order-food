import { defineStore } from 'pinia'
import type { CartItem, Dish } from '@/types'

const STORAGE_KEY = 'cart_items'
const REMARK_KEY = 'cart_remark'

/**
 * 生成购物车项的唯一标识 key
 * 同一菜品不同口味是不同的购物车项
 * @param dishId - 菜品ID
 * @param tasteSelection - 口味选择（可选）
 * @returns 唯一标识字符串
 */
function cartItemKey(dishId: number, tasteSelection?: string): string {
  return `${dishId}__${tasteSelection || ''}`
}

/**
 * 购物车Store
 * 管理购物车商品列表，支持添加、删除、修改数量、清空
 * 支持口味选择：同一菜品不同口味是不同的购物车项
 * 数据持久化到 localStorage
 */
export const useCartStore = defineStore('cart', {
  state: () => ({
    items: JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]') as CartItem[],
    remark: localStorage.getItem(REMARK_KEY) || '',
  }),
  getters: {
    /** 购物车商品总数量 */
    totalCount: (state): number =>
      state.items.reduce((sum: number, item: CartItem) => sum + item.quantity, 0),
    /** 购物车商品总金额 */
    totalAmount: (state): number =>
      state.items.reduce(
        (sum: number, item: CartItem) => sum + item.price * item.quantity,
        0
      ),
  },
  actions: {
    /** 持久化购物车数据到 localStorage */
    persist(): void {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.items))
    },

    /**
     * 添加菜品到购物车
     * 如果菜品已存在（相同dishId且相同口味）则数量+1，否则新增一条记录
     * 同一菜品不同口味是不同的购物车项
     * @param dish - 菜品对象
     * @param tasteSelection - 口味选择（如"微辣,不要香菜"），可选
     */
    addItem(dish: Dish, tasteSelection?: string): void {
      const key = cartItemKey(dish.id, tasteSelection)
      const existing = this.items.find(
        (item: CartItem) => cartItemKey(item.dishId, item.tasteSelection) === key
      )
      if (existing) {
        existing.quantity += 1
      } else {
        this.items.push({
          dishId: dish.id,
          name: dish.name,
          price: dish.price,
          image: dish.image,
          quantity: 1,
          tasteSelection: tasteSelection || undefined,
        })
      }
      this.persist()
    },

    /**
     * 从购物车移除指定菜品（含口味区分）
     * @param dishId - 菜品ID
     * @param tasteSelection - 口味选择（可选）
     */
    removeItem(dishId: number, tasteSelection?: string): void {
      const key = cartItemKey(dishId, tasteSelection)
      this.items = this.items.filter(
        (item: CartItem) => cartItemKey(item.dishId, item.tasteSelection) !== key
      )
      this.persist()
    },

    /**
     * 更新购物车中指定菜品（含口味区分）的数量
     * 如果数量小于等于0则移除该菜品
     * @param dishId - 菜品ID
     * @param quantity - 新的数量
     * @param tasteSelection - 口味选择（可选）
     */
    updateQuantity(dishId: number, quantity: number, tasteSelection?: string): void {
      if (quantity <= 0) {
        this.removeItem(dishId, tasteSelection)
        return
      }
      const key = cartItemKey(dishId, tasteSelection)
      const item = this.items.find(
        (i: CartItem) => cartItemKey(i.dishId, i.tasteSelection) === key
      )
      if (item) {
        item.quantity = quantity
        this.persist()
      }
    },

    /**
     * 获取购物车中指定菜品（含口味区分）的数量
     * @param dishId - 菜品ID
     * @param tasteSelection - 口味选择（可选）
     * @returns 数量，不存在返回0
     */
    getQuantity(dishId: number, tasteSelection?: string): number {
      const key = cartItemKey(dishId, tasteSelection)
      const item = this.items.find(
        (i: CartItem) => cartItemKey(i.dishId, i.tasteSelection) === key
      )
      return item ? item.quantity : 0
    },

    /** 清空购物车 */
    clearCart(): void {
      this.items = []
      this.remark = ''
      this.persist()
      localStorage.removeItem(REMARK_KEY)
    },

    /**
     * 设置订单备注
     * @param remark - 备注内容
     */
    setRemark(remark: string): void {
      this.remark = remark
      localStorage.setItem(REMARK_KEY, remark)
    },
  },
})

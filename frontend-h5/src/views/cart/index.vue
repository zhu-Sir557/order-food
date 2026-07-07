<template>
  <div class="cart-page">
    <van-nav-bar title="购物车" left-arrow @click-left="router.back()" />

    <!-- 空购物车状态 -->
    <van-empty
      v-if="cartStore.items.length === 0"
      description="购物车空空如也"
    >
      <van-button round type="primary" class="go-order-btn" @click="goMenu">
        去点餐
      </van-button>
    </van-empty>

    <!-- 购物车内容 -->
    <template v-else>
      <!-- 桌号选择 -->
      <van-cell
        class="table-cell"
        title="就餐桌号"
        :value="userStore.tableName || userStore.tableCode || '请选择桌号'"
        is-link
        @click="showTablePicker = true"
      />

      <!-- 菜品列表 -->
      <div class="cart-items">
        <van-swipe-cell
          v-for="item in cartStore.items"
          :key="cartItemKey(item)"
        >
          <div class="cart-item">
            <van-image
              width="60"
              height="60"
              radius="6"
              fit="cover"
              :src="item.image || defaultImage"
            />
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div v-if="item.tasteSelection" class="item-taste">
                <van-icon name="bookmark-o" class="taste-icon" />
                <span>{{ formatTaste(item.tasteSelection) }}</span>
              </div>
              <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
            </div>
            <div class="item-stepper">
              <van-stepper
                :model-value="item.quantity"
                min="0"
                theme="round"
                @change="(val: string) => onQuantityChange(item, val)"
              />
            </div>
          </div>
          <template #right>
            <van-button
              square
              type="danger"
              text="删除"
              class="delete-btn"
              @click="cartStore.removeItem(item.dishId, item.tasteSelection)"
            />
          </template>
        </van-swipe-cell>
      </div>

      <!-- 备注 -->
      <van-cell-group inset class="remark-group">
        <van-field
          v-model="remark"
          label="备注"
          type="textarea"
          placeholder="请输入备注信息（如：少辣、不要香菜等）"
          rows="2"
          autosize
          @blur="onRemarkBlur"
        />
      </van-cell-group>

      <!-- 底部结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <span class="footer-label">合计:</span>
          <span class="footer-amount">¥{{ cartStore.totalAmount.toFixed(2) }}</span>
        </div>
        <van-button
          type="primary"
          round
          class="checkout-btn"
          :disabled="!userStore.tableId"
          @click="goConfirm"
        >
          {{ userStore.tableId ? '去结算' : '请先选桌号' }}
        </van-button>
      </div>
    </template>

    <!-- 桌台选择弹窗 -->
    <van-popup
      v-model:show="showTablePicker"
      position="bottom"
      round
      :style="{ maxHeight: '60%' }"
    >
      <div class="table-picker">
        <div class="picker-title">选择桌号</div>
        <van-empty v-if="tables.length === 0" description="暂无可用桌台" />
        <van-cell
          v-for="table in tables"
          :key="table.id"
          :title="table.name || table.code"
          :label="`可容纳 ${table.capacity} 人`"
          is-link
          @click="selectTable(table)"
        />
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import router from '@/router'
import { showToast } from 'vant'
import { useCartStore } from '@/store/modules/cart'
import { useUserStore } from '@/store/modules/user'
import { getAvailableTables } from '@/api/table'
import type { CartItem, DiningTable } from '@/types'

const cartStore = useCartStore()
const userStore = useUserStore()

const remark = ref(cartStore.remark)
const showTablePicker = ref(false)
const tables = ref<DiningTable[]>([])
const defaultImage = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

/** 生成购物车项的唯一 key（dishId + tasteSelection） */
const cartItemKey = (item: CartItem): string => {
  return `${item.dishId}__${item.tasteSelection || ''}`
}

/** 格式化口味选择显示文本 */
const formatTaste = (tasteSelection: string): string => {
  return tasteSelection.split(',').join(' · ')
}

/** 加载可用桌台 */
const loadTables = async (): Promise<void> => {
  try {
    tables.value = await getAvailableTables()
  } catch (error) {
    console.error('加载桌台失败:', error)
  }
}

/** 选择桌台 */
const selectTable = (table: DiningTable): void => {
  userStore.setTable(table)
  showTablePicker.value = false
  showToast(`已选择桌号: ${table.name || table.code}`)
}

/** 数量变化（需要传入 tasteSelection 以区分同一菜品不同口味） */
const onQuantityChange = (item: CartItem, val: string): void => {
  const quantity = Number(val)
  cartStore.updateQuantity(item.dishId, quantity, item.tasteSelection)
}

/** 备注失焦时保存 */
const onRemarkBlur = (): void => {
  cartStore.setRemark(remark.value)
}

/** 去结算 */
const goConfirm = (): void => {
  if (!userStore.tableId) {
    showToast('请先选择桌号')
    return
  }
  cartStore.setRemark(remark.value)
  router.push('/confirm')
}

/** 去点餐 */
const goMenu = (): void => {
  router.push('/menu')
}

onMounted(() => {
  loadTables()
})
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.go-order-btn {
  width: 160px;
  margin-top: 16px;
}

.table-cell {
  margin: 10px 12px;
  border-radius: var(--radius-card);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.cart-items {
  margin: 10px 12px;
  background: var(--color-bg-card);
  border-radius: var(--radius-card);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.cart-item {
  display: flex;
  align-items: center;
  padding: var(--space-card-inner);
  background: var(--color-bg-card);
}

.item-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.item-name {
  font-size: var(--font-size-h3);
  color: var(--color-text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-taste {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-top: 4px;
  padding: 1px 8px;
  background: var(--color-primary-bg);
  border-radius: var(--radius-tag);
  font-size: var(--font-size-caption-mini);
  color: var(--color-primary);
}

.taste-icon {
  font-size: var(--font-size-caption-mini);
}

.item-price {
  font-size: var(--font-size-h3);
  color: var(--color-primary);
  font-weight: var(--font-weight-semi);
  margin-top: 4px;
}

.item-stepper {
  flex-shrink: 0;
}

.delete-btn {
  height: 100%;
}

.remark-group {
  margin-top: 8px;
}

.cart-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--color-bg-card);
  border-radius: 16px 16px 0 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: var(--shadow-top);
  z-index: 100;
}

.footer-left {
  display: flex;
  align-items: center;
}

.footer-label {
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
}

.footer-amount {
  font-size: 20px;
  color: var(--color-primary);
  font-weight: var(--font-weight-bold);
  margin-left: 4px;
}

.checkout-btn {
  width: 140px;
  --van-button-primary-background: var(--color-primary);
  --van-button-primary-border-color: var(--color-primary);
}

.table-picker {
  padding: 12px 0;
}

.picker-title {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semi);
  text-align: center;
  padding: 8px 0 12px;
  color: var(--color-text-primary);
}
</style>

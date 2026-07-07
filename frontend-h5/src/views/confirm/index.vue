<template>
  <div class="confirm-page">
    <van-nav-bar title="确认订单" left-arrow @click-left="router.back()" />

    <!-- 就餐方式 -->
    <van-cell-group inset class="confirm-section">
      <van-cell title="就餐方式" value="堂食" />
      <van-cell
        title="桌号"
        :value="userStore.tableName || userStore.tableCode || '未选择'"
        is-link
        @click="router.push('/cart')"
      />
    </van-cell-group>

    <!-- 订单明细 -->
    <div class="confirm-section">
      <div class="section-title">订单明细</div>
      <van-cell-group inset>
        <div
          v-for="item in cartStore.items"
          :key="cartItemKey(item)"
          class="order-item"
        >
          <van-image
            width="50"
            height="50"
            radius="8"
            fit="cover"
            :src="item.image || defaultImage"
          />
          <div class="item-detail">
            <div class="item-name">{{ item.name }}</div>
            <div v-if="item.tasteSelection" class="item-taste">
              <van-icon name="bookmark-o" class="taste-icon" />
              <span>{{ formatTaste(item.tasteSelection) }}</span>
            </div>
            <div class="item-meta">
              <span class="item-price">¥{{ item.price.toFixed(2) }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
            </div>
          </div>
          <div class="item-subtotal">
            ¥{{ (item.price * item.quantity).toFixed(2) }}
          </div>
        </div>
      </van-cell-group>
    </div>

    <!-- 备注 -->
    <van-cell-group inset class="confirm-section">
      <van-field
        v-model="remark"
        label="备注"
        type="textarea"
        placeholder="请输入备注信息"
        rows="2"
        autosize
        readonly
      />
    </van-cell-group>

    <!-- 应付金额 -->
    <div class="amount-section">
      <span class="amount-label">应付金额</span>
      <span class="amount-value">¥{{ cartStore.totalAmount.toFixed(2) }}</span>
    </div>

    <!-- 提交按钮 -->
    <div class="submit-bar">
      <van-button
        type="primary"
        block
        round
        :loading="submitting"
        loading-text="提交中..."
        @click="handleSubmit"
      >
        提交订单
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import router from '@/router'
import { showToast } from 'vant'
import { useCartStore } from '@/store/modules/cart'
import { useUserStore } from '@/store/modules/user'
import { submitOrder } from '@/api/order'
import type { CartItem } from '@/types'

const cartStore = useCartStore()
const userStore = useUserStore()

const remark = ref(cartStore.remark)
const submitting = ref(false)
const defaultImage = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

/** 生成购物车项的唯一 key */
const cartItemKey = (item: CartItem): string => {
  return `${item.dishId}__${item.tasteSelection || ''}`
}

/** 格式化口味选择显示文本 */
const formatTaste = (tasteSelection: string): string => {
  return tasteSelection.split(',').join(' · ')
}

/** 提交订单 */
const handleSubmit = async (): Promise<void> => {
  if (!userStore.tableId) {
    showToast('请先选择桌号')
    router.push('/cart')
    return
  }

  if (cartStore.items.length === 0) {
    showToast('购物车为空')
    return
  }

  submitting.value = true
  try {
    const order = await submitOrder({
      tableId: userStore.tableId,
      remark: remark.value,
      items: cartStore.items.map((item) => ({
        dishId: item.dishId,
        quantity: item.quantity,
        tasteSelection: item.tasteSelection,
      })),
    })

    // 清空购物车
    cartStore.clearCart()

    // 跳转到支付页
    router.replace(`/pay/${order.id}`)
  } catch (error) {
    console.error('提交订单失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.confirm-page {
  min-height: 100vh;
  background: var(--color-bg-page);
  padding-bottom: 80px;
}

.confirm-section {
  margin-top: 10px;
}

.confirm-section :deep(.van-cell-group--inset) {
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.section-title {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  padding: 12px 16px 8px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: var(--space-card-inner);
}

.item-detail {
  flex: 1;
  margin-left: 10px;
  min-width: 0;
}

.item-name {
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
  font-weight: 500;
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
  font-size: 11px;
}

.item-meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.item-price {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-secondary);
}

.item-qty {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-secondary);
}

.item-subtotal {
  font-size: var(--font-size-body);
  color: var(--color-primary);
  font-weight: var(--font-weight-semi);
  flex-shrink: 0;
}

.amount-section {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 16px;
  margin: 10px 12px;
  background: var(--color-bg-card);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-sm);
}

.amount-label {
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
}

.amount-value {
  font-size: 24px;
  color: var(--color-primary);
  font-weight: var(--font-weight-bold);
  margin-left: 8px;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: var(--color-bg-card);
  border-radius: 16px 16px 0 0;
  box-shadow: var(--shadow-top);
  z-index: 100;
}

.submit-bar :deep(.van-button--primary) {
  --van-button-primary-background: var(--color-primary);
  --van-button-primary-border-color: var(--color-primary);
}
</style>

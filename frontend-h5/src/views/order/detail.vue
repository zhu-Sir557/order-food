<template>
  <div class="order-detail-page">
    <van-nav-bar title="订单详情" left-arrow @click-left="router.back()" />

    <template v-if="order">
      <!-- 订单状态 -->
      <div class="status-banner" :style="{ '--status-color': statusBgColor }">
        <div class="status-text">{{ statusText }}</div>
        <div class="status-desc">{{ statusDesc }}</div>
      </div>

      <!-- 状态时间轴 -->
      <div class="detail-section" v-if="order.status !== 5">
        <van-steps :active="activeStep" active-color="#ff6034">
          <van-step>下单</van-step>
          <van-step>接单</van-step>
          <van-step>制作中</van-step>
          <van-step>已完成</van-step>
        </van-steps>
      </div>

      <!-- 订单信息 -->
      <van-cell-group inset class="detail-section">
        <van-cell title="订单号" :value="order.orderNo" />
        <van-cell title="桌号" :value="order.tableCode" />
        <van-cell title="下单时间" :value="formatDate(order.createTime)" />
      </van-cell-group>

      <!-- 菜品明细 -->
      <div class="detail-section">
        <div class="section-title">菜品明细</div>
        <van-cell-group inset>
          <div
            v-for="item in order.items"
            :key="item.id"
            class="dish-item"
          >
            <van-image
              width="50"
              height="50"
              radius="6"
              fit="cover"
              :src="item.dishImage || defaultImage"
            />
            <div class="dish-info">
              <div class="dish-name">{{ item.dishName }}</div>
              <div v-if="item.tasteSelection" class="dish-taste">
                <van-icon name="bookmark-o" class="taste-icon" />
                <span>{{ formatTaste(item.tasteSelection) }}</span>
              </div>
              <div class="dish-meta">
                <span class="dish-price">¥{{ item.dishPrice.toFixed(2) }}</span>
                <span class="dish-qty">x{{ item.quantity }}</span>
              </div>
            </div>
            <div class="dish-subtotal">¥{{ item.subtotal.toFixed(2) }}</div>
          </div>
        </van-cell-group>
      </div>

      <!-- 备注 -->
      <van-cell-group inset class="detail-section" v-if="order.remark">
        <van-field label="备注" :model-value="order.remark" readonly />
      </van-cell-group>

      <!-- 总金额 -->
      <div class="total-section">
        <span class="total-label">总金额</span>
        <span class="total-amount">¥{{ order.totalAmount.toFixed(2) }}</span>
      </div>

      <!-- 待支付按钮 -->
      <div class="action-bar" v-if="order.status === 0">
        <van-button
          type="primary"
          block
          round
          @click="goPay"
        >
          去支付
        </van-button>
      </div>
    </template>

    <!-- 加载中 -->
    <van-loading v-else class="page-loading" type="spinner" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import router from '@/router'
import { formatDate, orderStatusText, orderStatusColor } from '@/utils'
import { getOrderDetail } from '@/api/order'
import type { Order } from '@/types'

const route = useRoute()

const order = ref<Order | null>(null)
const defaultImage = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

/** 格式化口味选择显示文本 */
const formatTaste = (tasteSelection: string): string => {
  return tasteSelection.split(',').join(' · ')
}

const statusText = computed(() =>
  order.value ? orderStatusText(order.value.status) : ''
)

const statusBgColor = computed(() =>
  order.value ? orderStatusColor(order.value.status) : '#909399'
)

const statusDesc = computed(() => {
  if (!order.value) return ''
  const descMap: Record<number, string> = {
    0: '请尽快完成支付',
    1: '商家正在接单中，请耐心等待',
    2: '您的菜品正在制作中',
    3: '您的订单已完成，请取餐',
    4: '您已取餐，欢迎下次光临',
    5: '订单已取消',
  }
  return descMap[order.value.status] || ''
})

/** 步骤条激活索引 */
const activeStep = computed(() => {
  if (!order.value) return 0
  const status = order.value.status
  if (status >= 3) return 3
  return status
})

/** 加载订单详情 */
const loadOrder = async (): Promise<void> => {
  const id = Number(route.params.id)
  if (!id) return
  try {
    order.value = await getOrderDetail(id)
  } catch (error) {
    console.error('加载订单详情失败:', error)
  }
}

/** 去支付 */
const goPay = (): void => {
  if (order.value) {
    router.push(`/pay/${order.value.id}`)
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background: var(--color-bg-page);
  padding-bottom: 80px;
}

.status-banner {
  padding: 28px 20px;
  color: #fff;
  margin-bottom: -16px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0)),
    var(--status-color);
}

.status-text {
  font-size: 24px;
  font-weight: 700;
}

.status-desc {
  font-size: 13px;
  margin-top: 4px;
  opacity: 0.9;
}

.detail-section {
  margin-top: 8px;
}

.detail-section :deep(.van-cell-group--inset) {
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.section-title {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  padding: 12px 16px 8px;
}

.dish-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
}

.dish-info {
  flex: 1;
  margin-left: 10px;
  min-width: 0;
}

.dish-name {
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
  font-weight: 500;
}

.dish-taste {
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

.dish-meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.dish-price {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-secondary);
}

.dish-qty {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-secondary);
}

.dish-subtotal {
  font-size: var(--font-size-body);
  color: var(--color-primary);
  font-weight: var(--font-weight-semi);
  flex-shrink: 0;
}

.total-section {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 16px;
  margin-top: 8px;
  background: var(--color-bg-card);
}

.total-label {
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
}

.total-amount {
  font-size: 24px;
  color: var(--color-primary);
  font-weight: var(--font-weight-bold);
  margin-left: 8px;
}

.action-bar {
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

.page-loading {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
</style>

<template>
  <div class="order-card" @click="goDetail">
    <div class="order-header">
      <span class="order-no">订单号: {{ order.orderNo }}</span>
      <span class="order-status" :style="{ color: statusColor }">{{ statusText }}</span>
    </div>
    <div class="order-items-summary">
      {{ itemsSummary }}
    </div>
    <div class="order-footer">
      <span class="order-time">{{ formatShortDate(order.createTime) }}</span>
      <div class="order-footer-right">
        <span class="order-amount">¥{{ order.totalAmount.toFixed(2) }}</span>
        <van-icon name="arrow" class="arrow-icon" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { formatShortDate, orderStatusText, orderStatusColor } from '@/utils'
import type { Order } from '@/types'

const props = defineProps<{
  order: Order
}>()

const router = useRouter()

const statusText = computed(() => orderStatusText(props.order.status))
const statusColor = computed(() => orderStatusColor(props.order.status))

const itemsSummary = computed(() => {
  if (!props.order.items || props.order.items.length === 0) {
    return '暂无菜品'
  }
  const names = props.order.items.slice(0, 2).map((item) => item.dishName)
  if (props.order.items.length > 2) {
    return names.join('、') + ` 等${props.order.items.length}件商品`
  }
  return names.join('、')
})

const goDetail = (): void => {
  router.push(`/order/${props.order.id}`)
}
</script>

<style scoped>
.order-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-card);
  padding: var(--space-card-inner);
  margin-bottom: var(--space-list-item);
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.2s ease;
}

.order-card:active {
  box-shadow: var(--shadow-md);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-no {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-secondary);
}

.order-status {
  font-size: var(--font-size-caption);
  font-weight: 600;
  background: var(--color-primary-bg);
  padding: 2px 8px;
  border-radius: var(--radius-tag);
}

.order-items-summary {
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-time {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
}

.order-footer-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.order-amount {
  font-size: 16px;
  color: var(--color-primary);
  font-weight: var(--font-weight-bold);
}

.arrow-icon {
  color: var(--color-text-disabled);
}
</style>

<template>
  <div class="order-list-page">
    <van-nav-bar title="我的订单" left-arrow @click-left="router.back()" />

    <!-- 状态Tab -->
    <van-tabs v-model:active="activeTab" @change="onTabChange" sticky>
      <van-tab title="全部" />
      <van-tab title="待接单" />
      <van-tab title="制作中" />
      <van-tab title="已完成" />
    </van-tabs>

    <!-- 下拉刷新 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 订单列表 -->
      <div class="order-list" v-if="orders.length > 0">
        <OrderCard
          v-for="order in orders"
          :key="order.id"
          :order="order"
        />
      </div>

      <!-- 空状态 -->
      <van-empty v-else description="暂无订单记录" image-size="120" />

      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMore">
        <van-button plain hairline size="small" @click="loadMore">
          加载更多
        </van-button>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import router from '@/router'
import OrderCard from '@/components/OrderCard.vue'
import { getOrderList } from '@/api/order'
import type { Order } from '@/types'

const activeTab = ref(0)
const orders = ref<Order[]>([])
const refreshing = ref(false)
const loading = ref(false)
const hasMore = ref(false)
const currentPage = ref(1)
const pageSize = 10

/** Tab状态映射：0全部, 1待接单, 2制作中, 3已完成 */
const tabStatusMap: (number | undefined)[] = [undefined, 1, 2, 3]

/** 加载订单列表 */
const loadOrders = async (reset = false): Promise<void> => {
  if (loading.value) return
  loading.value = true

  if (reset) {
    currentPage.value = 1
  }

  try {
    const status = tabStatusMap[activeTab.value]
    const res = await getOrderList({
      status,
      page: currentPage.value,
      size: pageSize,
    })

    const records = res.records || []
    if (reset) {
      orders.value = records
    } else {
      orders.value.push(...records)
    }
    hasMore.value = orders.value.length < (res.total || 0)
  } catch (error) {
    console.error('加载订单列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/** Tab切换 */
const onTabChange = (): void => {
  loadOrders(true)
}

/** 下拉刷新 */
const onRefresh = (): void => {
  loadOrders(true)
}

/** 加载更多 */
const loadMore = (): void => {
  currentPage.value += 1
  loadOrders(false)
}

onMounted(() => {
  loadOrders(true)
})
</script>

<style scoped>
.order-list-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.order-list-page :deep(.van-tabs) {
  --van-tabs-nav-background: var(--color-bg-page);
}

.order-list-page :deep(.van-tab--active) {
  font-weight: 700;
}

.order-list {
  padding: 8px 12px;
}

.load-more {
  text-align: center;
  padding: 12px 0 20px;
}
</style>

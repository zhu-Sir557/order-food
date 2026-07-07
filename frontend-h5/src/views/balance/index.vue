<template>
  <div class="balance-page">
    <van-nav-bar title="余额记录" left-arrow @click-left="router.back()" />

    <!-- 余额展示 -->
    <div class="balance-card">
      <div class="balance-label">当前余额</div>
      <div class="balance-value">¥{{ memberStore.balance.toFixed(2) }}</div>
    </div>

    <!-- 记录列表 -->
    <van-cell-group inset class="record-group" v-if="records.length > 0">
      <van-cell
        v-for="record in records"
        :key="record.id"
        class="record-item"
      >
        <template #title>
          <div class="record-info">
            <div class="record-type">
              <van-tag :type="record.type === 1 ? 'success' : 'warning'" size="medium">
                {{ record.typeText }}
              </van-tag>
              <span class="record-remark">{{ record.remark }}</span>
            </div>
            <div class="record-time">{{ record.createTime }}</div>
            <div class="record-card" v-if="record.cardNo">卡号: {{ record.cardNo }}</div>
            <div class="record-card" v-if="record.orderNo">订单: {{ record.orderNo }}</div>
          </div>
        </template>
        <template #value>
          <div class="record-amount" :class="record.type === 1 ? 'income' : 'expense'">
            {{ record.type === 1 ? '+' : '-' }}¥{{ record.amount.toFixed(2) }}
          </div>
          <div class="record-balance">余额 ¥{{ record.balanceAfter.toFixed(2) }}</div>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 空状态 -->
    <van-empty description="暂无余额记录" v-if="records.length === 0 && !loading" />

    <!-- 加载中 -->
    <van-loading v-if="loading" class="page-loading" type="spinner" />

    <!-- 加载更多 -->
    <div class="load-more" v-if="hasMore" @click="loadMore">加载更多</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import router from '@/router'
import { showToast } from 'vant'
import { useMemberStore } from '@/store/modules/member'
import { getBalanceRecords } from '@/api/member'
import type { BalanceRecord } from '@/types'

const memberStore = useMemberStore()

const records = ref<BalanceRecord[]>([])
const loading = ref(false)
const currentPage = ref(1)
const hasMore = ref(false)

/** 加载余额记录 */
const loadRecords = async (): Promise<void> => {
  loading.value = true
  try {
    const result = await getBalanceRecords(currentPage.value, 20)
    if (currentPage.value === 1) {
      records.value = result.records
    } else {
      records.value.push(...result.records)
    }
    hasMore.value = records.value.length < result.total
  } catch (error) {
    console.error('加载余额记录失败:', error)
  } finally {
    loading.value = false
  }
}

/** 加载更多 */
const loadMore = (): void => {
  currentPage.value++
  loadRecords()
}

onMounted(() => {
  // 刷新余额
  memberStore.refreshInfo()
  loadRecords()
})
</script>

<style scoped>
.balance-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.balance-card {
  background: linear-gradient(135deg, #ff6034, #ff8a65, #ffab91);
  padding: 30px 20px;
  text-align: center;
  color: #fff;
  margin: -20px 16px 0;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(255, 96, 52, 0.2);
}

.balance-label {
  font-size: 14px;
  opacity: 0.9;
}

.balance-value {
  font-size: var(--font-size-display);
  font-weight: var(--font-weight-bold);
  margin-top: 8px;
}

.record-group {
  margin-top: 12px;
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-card);
}

.record-item {
  padding: 12px 16px;
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.record-remark {
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
}

.record-time {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
}

.record-card {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
}

.record-amount {
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-semi);
  text-align: right;
}

.record-amount.income {
  color: #07c160;
}

.record-amount.expense {
  color: #ee0a24;
}

.record-balance {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
  text-align: right;
  margin-top: 4px;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.load-more {
  text-align: center;
  padding: 12px;
  font-size: var(--font-size-body);
  color: var(--color-primary);
  cursor: pointer;
}

.load-more:active {
  opacity: 0.7;
}
</style>

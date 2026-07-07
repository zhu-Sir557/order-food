<template>
  <div class="order-page">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
          >
            <el-option label="待支付" :value="0" />
            <el-option label="待接单" :value="1" />
            <el-option label="制作中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取餐" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="tableCode" label="桌号" width="80" align="center" />
        <el-table-column label="菜品摘要" min-width="200">
          <template #default="{ row }">
            <span class="dish-summary">{{ row.items ? summarizeDishes(row.items) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="110" align="center">
          <template #default="{ row }">
            <span class="amount">{{ formatPrice(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link :icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              type="primary"
              size="small"
              round
              @click="handleStatusUpdate(row, 2)"
            >
              接单
            </el-button>
            <el-button
              v-if="row.status === 2"
              type="success"
              size="small"
              round
              @click="handleStatusUpdate(row, 3)"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status === 3"
              type="warning"
              size="small"
              round
              @click="handleStatusUpdate(row, 4)"
            >
              取餐
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <OrderDetailDialog
      v-model:visible="detailVisible"
      :order-id="currentOrderId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import { getOrderPage, updateOrderStatus } from '@/api/order'
import { formatPrice, formatDate } from '@/utils'
import OrderDetailDialog from '@/components/OrderDetailDialog.vue'
import type { Order, OrderItem } from '@/types'

const loading = ref(false)
const tableData = ref<Order[]>([])
const detailVisible = ref(false)
const currentOrderId = ref(0)
const dateRange = ref<[string, string] | null>(null)
let pollTimer: ReturnType<typeof setInterval> | null = null

const searchForm = reactive({
  status: undefined as number | undefined,
  startTime: undefined as string | undefined,
  endTime: undefined as string | undefined
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

function statusText(status: number): string {
  const map: Record<number, string> = {
    0: '待支付',
    1: '待接单',
    2: '制作中',
    3: '已完成',
    4: '已取餐',
    5: '已取消'
  }
  return map[status] || '未知'
}

function statusTagType(status: number): 'info' | 'warning' | 'success' | 'danger' {
  const map: Record<number, 'info' | 'warning' | 'success' | 'danger'> = {
    0: 'info',
    1: 'warning',
    2: 'warning',
    3: 'success',
    4: 'success',
    5: 'danger'
  }
  return map[status] || 'info'
}

function summarizeDishes(items: OrderItem[]): string {
  const parts = items.slice(0, 3).map((item) => `${item.dishName}×${item.quantity}`)
  let summary = parts.join('，')
  if (items.length > 3) {
    summary += ` 等${items.length}道菜`
  }
  return summary
}

function handleDateChange(val: [string, string] | null): void {
  if (val) {
    searchForm.startTime = val[0]
    searchForm.endTime = val[1]
  } else {
    searchForm.startTime = undefined
    searchForm.endTime = undefined
  }
}

async function loadData(): Promise<void> {
  loading.value = true
  try {
    const res = await getOrderPage({
      current: pagination.current,
      size: pagination.size,
      status: searchForm.status,
      startTime: searchForm.startTime,
      endTime: searchForm.endTime
    })
    tableData.value = res.records
    pagination.total = res.total
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  pagination.current = 1
  loadData()
}

function handleReset(): void {
  searchForm.status = undefined
  searchForm.startTime = undefined
  searchForm.endTime = undefined
  dateRange.value = null
  pagination.current = 1
  loadData()
}

function handleDetail(row: Order): void {
  currentOrderId.value = row.id
  detailVisible.value = true
}

async function handleStatusUpdate(row: Order, status: number): Promise<void> {
  try {
    await updateOrderStatus(row.id, status)
    ElMessage.success(`已${statusText(status)}`)
    loadData()
  } catch {
    // 错误已在拦截器处理
  }
}

onMounted(() => {
  loadData()
  // 每10秒轮询刷新订单列表
  pollTimer = setInterval(() => {
    loadData()
  }, 10000)
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.dish-summary {
  color: var(--color-text-regular);
}

.amount {
  font-weight: 700;
  color: var(--color-danger);
}

.pagination {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-divider);
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  --el-table-border-color: var(--color-border);
}

:deep(.el-table__header-wrapper th) {
  background: #f7f8fa;
  font-weight: 600;
  color: var(--color-text-primary);
}

:deep(.el-table__row--striped td) {
  background: #fafafa;
}

:deep(.el-card) {
  border-radius: 12px;
}
</style>

<template>
  <el-dialog
    :model-value="visible"
    title="订单详情"
    width="680px"
    @close="handleClose"
  >
    <div v-loading="loading" class="order-detail">
      <template v-if="order">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="桌号">{{ order.tableCode }}</el-descriptions-item>
          <el-descriptions-item label="总金额">
            <span class="amount">{{ formatPrice(order.totalAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(order.status)">
              {{ statusText(order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">
            {{ formatDate(order.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 菜品明细 -->
        <h4 class="section-title">菜品明细</h4>
        <el-table :data="order.items || []" stripe border size="small">
          <el-table-column label="图片" width="80" align="center">
            <template #default="{ row }">
              <el-image
                :src="row.dishImage"
                fit="cover"
                style="width: 40px; height: 40px; border-radius: 4px"
              >
                <template #error>
                  <div class="img-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="dishName" label="菜品名称" min-width="120" />
          <el-table-column label="单价" width="90" align="center">
            <template #default="{ row }">
              {{ formatPrice(row.dishPrice) }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" align="center" />
          <el-table-column label="小计" width="90" align="center">
            <template #default="{ row }">
              {{ formatPrice(row.subtotal) }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 状态时间轴 -->
        <h4 class="section-title">订单状态轨迹</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in timeline"
            :key="index"
            :type="item.active ? 'primary' : 'info'"
            :hollow="!item.active"
            :timestamp="item.time"
          >
            {{ item.label }}
          </el-timeline-item>
        </el-timeline>
      </template>
    </div>
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import { getOrderDetail } from '@/api/order'
import { formatPrice, formatDate } from '@/utils'
import type { Order } from '@/types'

const props = defineProps<{
  visible: boolean
  orderId: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
}>()

const loading = ref(false)
const order = ref<Order | null>(null)

const statusLabels = ['待支付', '待接单', '制作中', '已完成', '已取餐', '已取消']

function statusText(status: number): string {
  return statusLabels[status] || '未知'
}

function statusTagType(status: number): 'info' | 'warning' | 'primary' | 'success' | 'danger' {
  const map: Record<number, 'info' | 'warning' | 'primary' | 'success' | 'danger'> = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'success',
    5: 'danger'
  }
  return map[status] || 'info'
}

const timeline = computed(() => {
  if (!order.value) return []
  const currentStatus = order.value.status
  const result: { label: string; active: boolean; time: string }[] = []

  for (let i = 0; i <= 4; i++) {
    result.push({
      label: statusLabels[i],
      active: currentStatus >= i,
      time: currentStatus >= i ? formatDate(order.value.createTime) : ''
    })
  }

  if (currentStatus === 5) {
    result.push({
      label: statusLabels[5],
      active: true,
      time: formatDate(order.value.createTime)
    })
  }

  return result
})

watch(
  () => props.visible,
  (val) => {
    if (val && props.orderId) {
      loadOrderDetail()
    } else {
      order.value = null
    }
  }
)

async function loadOrderDetail(): Promise<void> {
  loading.value = true
  try {
    order.value = await getOrderDetail(props.orderId)
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function handleClose(): void {
  emit('update:visible', false)
}
</script>

<style scoped>
.section-title {
  margin: 20px 0 12px;
  font-size: 15px;
  color: var(--color-text-primary);
}

.amount {
  font-weight: 700;
  color: var(--color-danger);
  font-size: 16px;
}

.img-placeholder {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #c0c4cc;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}
</style>

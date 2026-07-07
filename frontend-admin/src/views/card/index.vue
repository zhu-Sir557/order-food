<template>
  <div class="card-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>点卡管理</span>
          <el-button type="primary" @click="showCreateDialog = true">批量创建点卡</el-button>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="卡号">
          <el-input v-model="query.cardNo" placeholder="请输入卡号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="未使用" :value="0" />
            <el-option label="已发放" :value="1" />
            <el-option label="已使用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadCards">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 点卡列表 -->
      <el-table :data="cards" v-loading="loading" border style="width: 100%">
        <el-table-column prop="cardNo" label="卡号" width="200" />
        <el-table-column prop="cardPassword" label="卡密" width="180" />
        <el-table-column prop="amount" label="额度" width="100">
          <template #default="{ row }">
            ¥{{ row.amount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberName" label="发放对象" width="120">
          <template #default="{ row }">
            {{ row.memberName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="assignedAt" label="发放时间" width="180">
          <template #default="{ row }">{{ row.assignedAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="usedAt" label="使用时间" width="180">
          <template #default="{ row }">{{ row.usedAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :disabled="row.status !== 0"
              @click="openAssignDialog(row)"
            >
              发放
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadCards"
        @current-change="loadCards"
      />
    </el-card>

    <!-- 批量创建对话框 -->
    <el-dialog v-model="showCreateDialog" title="批量创建点卡" width="400px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="额度">
          <el-input-number v-model="createForm.amount" :min="0.01" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="createForm.count" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">确认创建</el-button>
      </template>
    </el-dialog>

    <!-- 发放点卡对话框 -->
    <el-dialog v-model="showAssignDialog" title="发放点卡" width="400px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="卡号">
          <el-input :model-value="currentCard?.cardNo" disabled />
        </el-form-item>
        <el-form-item label="额度">
          <el-input :model-value="'¥' + (currentCard?.amount || 0).toFixed(2)" disabled />
        </el-form-item>
        <el-form-item label="会员ID">
          <el-input-number v-model="assignForm.memberId" :min="1" placeholder="请输入会员ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssignDialog = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleAssign">确认发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { batchCreateCards, getCardList, assignCard } from '@/api/card'
import type { Card } from '@/types'

const loading = ref(false)
const cards = ref<Card[]>([])
const total = ref(0)

const query = reactive({
  cardNo: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10,
})

/** 加载点卡列表 */
const loadCards = async (): Promise<void> => {
  loading.value = true
  try {
    const result = await getCardList(query)
    cards.value = result.records
    total.value = result.total
  } catch (error) {
    console.error('加载点卡列表失败:', error)
  } finally {
    loading.value = false
  }
}

/** 重置查询 */
const resetQuery = (): void => {
  query.cardNo = ''
  query.status = undefined
  query.page = 1
  loadCards()
}

/** 状态标签类型 */
const getStatusType = (status: number): 'info' | 'warning' | 'success' => {
  const map: Record<number, 'info' | 'warning' | 'success'> = {
    0: 'info',
    1: 'warning',
    2: 'success',
  }
  return map[status] || 'info'
}

// 批量创建
const showCreateDialog = ref(false)
const creating = ref(false)
const createForm = reactive({
  amount: 50,
  count: 10,
})

const handleCreate = async (): Promise<void> => {
  creating.value = true
  try {
    const result = await batchCreateCards(createForm)
    ElMessage.success(`成功创建 ${result.length} 张点卡`)
    showCreateDialog.value = false
    loadCards()
  } catch (error) {
    console.error('创建点卡失败:', error)
  } finally {
    creating.value = false
  }
}

// 发放点卡
const showAssignDialog = ref(false)
const assigning = ref(false)
const currentCard = ref<Card | null>(null)
const assignForm = reactive({
  memberId: undefined as number | undefined,
})

const openAssignDialog = (card: Card): void => {
  currentCard.value = card
  assignForm.memberId = undefined
  showAssignDialog.value = true
}

const handleAssign = async (): Promise<void> => {
  if (!currentCard.value || !assignForm.memberId) {
    ElMessage.warning('请输入会员ID')
    return
  }
  assigning.value = true
  try {
    await assignCard(currentCard.value.id, { memberId: assignForm.memberId })
    ElMessage.success('发放成功')
    showAssignDialog.value = false
    loadCards()
  } catch (error) {
    console.error('发放点卡失败:', error)
  } finally {
    assigning.value = false
  }
}

onMounted(() => {
  loadCards()
})
</script>

<style scoped>
.card-page {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.search-form {
  margin-bottom: 16px;
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

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}
</style>

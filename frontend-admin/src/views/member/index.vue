<template>
  <div class="member-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>会员管理</span>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="query.keyword" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadMembers">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 会员列表 -->
      <el-table :data="members" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="200" />
        <el-table-column prop="balance" label="余额" width="120">
          <template #default="{ row }">
            <span class="balance-text">¥{{ row.balance.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="200" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openRecordDialog(row)">余额记录</el-button>
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
        @size-change="loadMembers"
        @current-change="loadMembers"
      />
    </el-card>

    <!-- 余额记录对话框 -->
    <el-dialog v-model="showRecordDialog" title="余额变动记录" width="700px">
      <el-table :data="records" v-loading="recordLoading" border style="width: 100%">
        <el-table-column prop="typeText" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'warning'" size="small">{{ row.typeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            <span :class="row.type === 1 ? 'income' : 'expense'">
              {{ row.type === 1 ? '+' : '-' }}¥{{ row.amount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="变动后余额" width="120">
          <template #default="{ row }">¥{{ row.balanceAfter.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="cardNo" label="卡号" width="160">
          <template #default="{ row }">{{ row.cardNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="160">
          <template #default="{ row }">{{ row.orderNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
      <el-pagination
        class="pagination"
        v-model:current-page="recordQuery.page"
        v-model:page-size="recordQuery.size"
        :total="recordTotal"
        layout="total, prev, pager, next"
        @current-change="loadRecords"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMemberList, getMemberBalanceRecords } from '@/api/member'
import type { AdminMember, BalanceRecord } from '@/types'

const loading = ref(false)
const members = ref<AdminMember[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  page: 1,
  size: 10,
})

/** 加载会员列表 */
const loadMembers = async (): Promise<void> => {
  loading.value = true
  try {
    const result = await getMemberList(query)
    members.value = result.records
    total.value = result.total
  } catch (error) {
    console.error('加载会员列表失败:', error)
  } finally {
    loading.value = false
  }
}

/** 重置查询 */
const resetQuery = (): void => {
  query.keyword = ''
  query.page = 1
  loadMembers()
}

// 余额记录
const showRecordDialog = ref(false)
const recordLoading = ref(false)
const records = ref<BalanceRecord[]>([])
const recordTotal = ref(0)
const currentMember = ref<AdminMember | null>(null)
const recordQuery = reactive({
  page: 1,
  size: 10,
})

const openRecordDialog = (member: any): void => {
  currentMember.value = member
  recordQuery.page = 1
  showRecordDialog.value = true
  loadRecords()
}

const loadRecords = async (): Promise<void> => {
  if (!currentMember.value) return
  recordLoading.value = true
  try {
    const result = await getMemberBalanceRecords(currentMember.value.id, recordQuery.page, recordQuery.size)
    records.value = result.records
    recordTotal.value = result.total
  } catch (error) {
    console.error('加载余额记录失败:', error)
  } finally {
    recordLoading.value = false
  }
}

onMounted(() => {
  loadMembers()
})
</script>

<style scoped>
.member-page {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.balance-text {
  color: var(--el-color-primary);
  font-weight: 600;
}

.income {
  color: #67c23a;
  font-weight: 600;
}

.expense {
  color: var(--color-danger);
  font-weight: 600;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog .el-table) {
  border-radius: 8px;
  overflow: hidden;
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
</style>

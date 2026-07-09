<template>
  <div class="table-page">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="page-title">桌台管理</span>
        <el-button type="success" :icon="Plus" @click="handleAdd">新增桌台</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="code" label="桌号" width="120" align="center" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="capacity" label="容量(人)" width="120" align="center" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="tableStatusType(row.status)">
              {{ tableStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <TableDialog
      v-model:visible="dialogVisible"
      :table-data="currentTable"
      @save="handleSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getTableList, addTable, updateTable, deleteTable } from '@/api/table'
import TableDialog from '@/components/TableDialog.vue'
import type { DiningTable } from '@/types'

const loading = ref(false)
const tableData = ref<DiningTable[]>([])
const dialogVisible = ref(false)
const currentTable = ref<DiningTable | null>(null)

function tableStatusText(status: number): string {
  const map: Record<number, string> = {
    0: '空闲',
    1: '使用中',
    2: '已预约',
    3: '待清理'
  }
  return map[status] || '未知'
}

function tableStatusType(status: number): 'success' | 'warning' | 'info' | 'primary' {
  const map: Record<number, 'success' | 'warning' | 'info' | 'primary'> = {
    0: 'success',
    1: 'warning',
    2: 'primary',
    3: 'info'
  }
  return map[status] || 'info'
}

async function loadData(): Promise<void> {
  loading.value = true
  try {
    tableData.value = await getTableList()
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function handleAdd(): void {
  currentTable.value = null
  dialogVisible.value = true
}

function handleEdit(row: any): void {
  currentTable.value = { ...row }
  dialogVisible.value = true
}

async function handleSave(data: Partial<DiningTable>, id?: number): Promise<void> {
  try {
    if (id) {
      await updateTable(id, data)
      ElMessage.success('修改成功')
    } else {
      await addTable(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // 错误已在拦截器处理
  }
}

async function handleDelete(row: any): Promise<void> {
  ElMessageBox.confirm(`确定要删除桌台"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteTable(row.id)
        ElMessage.success('删除成功')
        loadData()
      } catch {
        // 错误已在拦截器处理
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-divider);
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
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

:deep(.el-tag) {
  border-radius: 4px;
}

:deep(.el-card) {
  border-radius: 12px;
}
</style>

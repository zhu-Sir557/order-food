<template>
  <div class="user-page">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="page-title">用户管理</span>
        <el-button type="success" :icon="Plus" @click="handleAdd">新增用户</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar">{{ row.name?.charAt(0) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'success'">
              {{ row.role === 1 ? '管理员' : '服务员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              :disabled="row.role === 1"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { formatDate } from '@/utils'

interface UserRecord {
  id: number
  username: string
  name: string
  phone: string
  avatar: string
  role: number
  status: number
  createTime: string
}

const loading = ref(false)
const tableData = ref<UserRecord[]>([])

// 用户管理为P1功能，使用模拟数据展示
const mockData: UserRecord[] = [
  {
    id: 1,
    username: 'admin',
    name: '系统管理员',
    phone: '13800138000',
    avatar: '',
    role: 1,
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    username: 'waiter01',
    name: '张服务员',
    phone: '13900139000',
    avatar: '',
    role: 2,
    status: 1,
    createTime: '2024-03-15 09:30:00'
  },
  {
    id: 3,
    username: 'waiter02',
    name: '李服务员',
    phone: '13700137000',
    avatar: '',
    role: 2,
    status: 0,
    createTime: '2024-06-20 14:00:00'
  }
]

async function loadData(): Promise<void> {
  loading.value = true
  try {
    // P1功能：使用模拟数据
    await new Promise((resolve) => setTimeout(resolve, 300))
    tableData.value = mockData
  } finally {
    loading.value = false
  }
}

function handleAdd(): void {
  ElMessage.info('用户管理为P1功能，暂未开放新增')
}

function handleEdit(_row: any): void {
  ElMessage.info('用户管理为P1功能，暂未开放编辑')
}

function handleDelete(row: any): void {
  ElMessageBox.confirm(`确定要删除用户"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      tableData.value = tableData.value.filter((item) => item.id !== row.id)
      ElMessage.success('删除成功')
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

:deep(.el-avatar) {
  background: var(--el-color-primary-light-8);
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

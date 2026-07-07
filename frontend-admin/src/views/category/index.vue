<template>
  <div class="category-page">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="page-title">分类管理</span>
        <el-button type="success" :icon="Plus" @click="handleAdd">新增分类</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="sort" label="排序" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dishCount" label="菜品数量" width="120" align="center" />
        <el-table-column label="排序操作" width="140" align="center">
          <template #default="{ row, $index }">
            <el-button
              type="primary"
              link
              :icon="Top"
              :disabled="$index === 0"
              @click="handleMove($index, -1)"
            >
              上移
            </el-button>
            <el-button
              type="primary"
              link
              :icon="Bottom"
              :disabled="$index === tableData.length - 1"
              @click="handleMove($index, 1)"
            >
              下移
            </el-button>
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

    <CategoryDialog
      v-model:visible="dialogVisible"
      :category-data="currentCategory"
      @save="handleSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Top, Bottom } from '@element-plus/icons-vue'
import {
  getCategoryList,
  addCategory,
  updateCategory,
  deleteCategory,
  updateCategorySort
} from '@/api/category'
import CategoryDialog from '@/components/CategoryDialog.vue'
import type { Category } from '@/types'

const loading = ref(false)
const tableData = ref<Category[]>([])
const dialogVisible = ref(false)
const currentCategory = ref<Category | null>(null)

async function loadData(): Promise<void> {
  loading.value = true
  try {
    tableData.value = await getCategoryList()
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function handleAdd(): void {
  currentCategory.value = null
  dialogVisible.value = true
}

function handleEdit(row: Category): void {
  currentCategory.value = { ...row }
  dialogVisible.value = true
}

async function handleSave(data: Partial<Category>, id?: number): Promise<void> {
  try {
    if (id) {
      await updateCategory(id, data)
      ElMessage.success('修改成功')
    } else {
      await addCategory(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // 错误已在拦截器处理
  }
}

async function handleDelete(row: Category): Promise<void> {
  ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteCategory(row.id)
        ElMessage.success('删除成功')
        loadData()
      } catch {
        // 错误已在拦截器处理
      }
    })
    .catch(() => {})
}

async function handleMove(index: number, direction: number): Promise<void> {
  const targetIndex = index + direction
  if (targetIndex < 0 || targetIndex >= tableData.value.length) return

  const current = tableData.value[index]
  const target = tableData.value[targetIndex]

  const sortItems = [
    { id: current.id, sort: target.sort },
    { id: target.id, sort: current.sort }
  ]

  try {
    await updateCategorySort(sortItems)
    ElMessage.success('排序已更新')
    loadData()
  } catch {
    // 错误已在拦截器处理
  }
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

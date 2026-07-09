<template>
  <div class="dish-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="菜品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入菜品名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="全部分类"
            clearable
            style="width: 160px"
          >
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" :icon="Plus" @click="handleAdd">新增菜品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column label="图片" width="100" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.image"
              fit="cover"
              style="width: 56px; height: 56px; border-radius: 8px"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" align="center" />
        <el-table-column label="价格" width="100" align="center">
          <template #default="{ row }">
            {{ formatPrice(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: any) => handleStatusChange(row, val)"
              active-text="上架"
              inactive-text="下架"
              inline-prompt
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 弹窗 -->
    <DishDialog
      v-model:visible="dialogVisible"
      :dish-data="currentDish"
      :category-list="categoryList"
      @save="handleSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Picture } from '@element-plus/icons-vue'
import { getDishPage, addDish, updateDish, deleteDish, updateDishStatus } from '@/api/dish'
import { getCategoryList } from '@/api/category'
import { formatPrice } from '@/utils'
import DishDialog from '@/components/DishDialog.vue'
import type { Dish, Category } from '@/types'

const loading = ref(false)
const tableData = ref<Dish[]>([])
const categoryList = ref<Category[]>([])
const dialogVisible = ref(false)
const currentDish = ref<Dish | null>(null)

const searchForm = reactive({
  name: '',
  categoryId: undefined as number | undefined
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

async function loadData(): Promise<void> {
  loading.value = true
  try {
    const res = await getDishPage({
      current: pagination.current,
      size: pagination.size,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId
    })
    tableData.value = res.records
    pagination.total = res.total
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

async function loadCategories(): Promise<void> {
  try {
    categoryList.value = await getCategoryList()
  } catch {
    // ignore
  }
}

function handleSearch(): void {
  pagination.current = 1
  loadData()
}

function handleReset(): void {
  searchForm.name = ''
  searchForm.categoryId = undefined
  pagination.current = 1
  loadData()
}

function handleAdd(): void {
  currentDish.value = null
  dialogVisible.value = true
}

function handleEdit(row: any): void {
  currentDish.value = { ...row }
  dialogVisible.value = true
}

async function handleSave(data: Partial<Dish>, id?: number): Promise<void> {
  try {
    if (id) {
      await updateDish(id, data)
      ElMessage.success('修改成功')
    } else {
      await addDish(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // 错误已在拦截器处理
  }
}

async function handleDelete(row: any): Promise<void> {
  ElMessageBox.confirm(`确定要删除菜品"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteDish(row.id)
        ElMessage.success('删除成功')
        loadData()
      } catch {
        // 错误已在拦截器处理
      }
    })
    .catch(() => {})
}

async function handleStatusChange(row: any, val: boolean): Promise<void> {
  const newStatus = val ? 1 : 0
  try {
    await updateDishStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(val ? '已上架' : '已下架')
  } catch {
    // 错误已在拦截器处理
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.pagination {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-divider);
  display: flex;
  justify-content: flex-end;
}

.image-placeholder {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #c0c4cc;
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

:deep(.el-form-item__label) {
  color: var(--color-text-regular);
}

:deep(.el-card) {
  border-radius: 12px;
}
</style>

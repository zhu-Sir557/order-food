<template>
  <div class="banner-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true">
        <el-form-item>
          <el-button type="success" :icon="Plus" @click="handleAdd">新增轮播图</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column label="图片" width="220" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.image"
              fit="cover"
              style="width: 200px; height: 80px; border-radius: 8px"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="120" />
        <el-table-column label="跳转链接" min-width="150">
          <template #default="{ row }">
            <span v-if="row.link" class="link-text">{{ row.link }}</span>
            <span v-else class="no-link">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: any) => handleStatusChange(row, val)"
              active-text="启用"
              inactive-text="禁用"
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

    <BannerDialog
      v-model:visible="dialogVisible"
      :banner-data="currentBanner"
      @save="handleSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Picture } from '@element-plus/icons-vue'
import { getBannerPage, addBanner, updateBanner, deleteBanner, updateBannerStatus } from '@/api/banner'
import BannerDialog from '@/components/BannerDialog.vue'
import type { Banner } from '@/types'

const loading = ref(false)
const tableData = ref<Banner[]>([])
const dialogVisible = ref(false)
const currentBanner = ref<Banner | null>(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

async function loadData(): Promise<void> {
  loading.value = true
  try {
    const res = await getBannerPage({
      current: pagination.current,
      size: pagination.size
    })
    tableData.value = res.records
    pagination.total = res.total
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function handleAdd(): void {
  currentBanner.value = null
  dialogVisible.value = true
}

function handleEdit(row: any): void {
  currentBanner.value = { ...row }
  dialogVisible.value = true
}

async function handleSave(data: Partial<Banner>, id?: number): Promise<void> {
  try {
    if (id) {
      await updateBanner(id, data)
      ElMessage.success('修改成功')
    } else {
      await addBanner(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // 错误已在拦截器处理
  }
}

async function handleDelete(row: any): Promise<void> {
  ElMessageBox.confirm(`确定要删除轮播图"${row.title || '未命名'}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteBanner(row.id)
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
    await updateBannerStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch {
    // 错误已在拦截器处理
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
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
  width: 200px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #c0c4cc;
}

.link-text {
  color: var(--el-color-primary);
  font-size: 13px;
}

.no-link {
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

:deep(.el-card) {
  border-radius: 12px;
}
</style>

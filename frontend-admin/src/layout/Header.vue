<template>
  <el-header class="header">
    <div class="header-right">
      <el-dropdown @command="handleCommand">
        <span class="admin-info">
          <el-avatar :size="32" :src="adminInfo?.avatar || ''">
            {{ adminInfo?.name?.charAt(0) || 'A' }}
          </el-avatar>
          <span class="admin-name">{{ adminInfo?.name || '管理员' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'

const router = useRouter()
const authStore = useAuthStore()

const adminInfo = computed(() => authStore.adminInfo)

function handleCommand(command: string): void {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        authStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background-color: #fff;
  border-bottom: none;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.admin-info:hover {
  background: var(--color-bg-page);
}

.admin-name {
  font-size: 14px;
  color: var(--color-text-primary);
}

:deep(.el-avatar) {
  border: 2px solid var(--el-color-primary-light-7);
}
</style>

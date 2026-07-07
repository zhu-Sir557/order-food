<template>
  <el-container class="layout-wrapper" direction="vertical">
    <el-header class="top-bar" height="48px">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
      <el-dropdown @command="handleCommand">
        <span class="admin-info">
          <el-avatar :size="30" :src="adminInfo?.avatar || ''">
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
    </el-header>
    <el-container class="layout-container">
      <Sidebar />
      <el-container class="main-container">
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import Sidebar from './Sidebar.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentTitle = computed(() => (route.meta.title as string) || '')
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
.layout-wrapper {
  height: 100%;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background-color: #fff;
  border-bottom: 1px solid var(--color-border);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

:deep(.el-breadcrumb) {
  line-height: 48px;
}

:deep(.el-breadcrumb__inner) {
  color: var(--color-text-secondary);
  font-weight: 400;
  font-size: 14px;
}

:deep(.el-breadcrumb__inner.is-link) {
  color: var(--el-color-primary);
  font-weight: 500;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--color-text-primary);
  font-weight: 600;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
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

.layout-container {
  height: 100%;
}

.main-container {
  height: 100%;
  overflow: hidden;
}
</style>

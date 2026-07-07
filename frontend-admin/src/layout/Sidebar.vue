<template>
  <el-aside width="220px" class="sidebar">
    <div class="logo">
      <el-icon :size="28" color="#fff"><Food /></el-icon>
      <span class="logo-text">智能点餐后台</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      router
      background-color="#2b2d3a"
      text-color="#a0a3b1"
      active-text-color="#ff6034"
    >
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>
      <el-menu-item index="/dish">
        <el-icon><Food /></el-icon>
        <span>菜品管理</span>
      </el-menu-item>
      <el-menu-item index="/category">
        <el-icon><Files /></el-icon>
        <span>分类管理</span>
      </el-menu-item>
      <el-menu-item index="/order">
        <el-icon><List /></el-icon>
        <span>订单管理</span>
      </el-menu-item>
      <el-menu-item index="/table">
        <el-icon><Grid /></el-icon>
        <span>桌台管理</span>
      </el-menu-item>
      <el-menu-item index="/banner">
        <el-icon><Picture /></el-icon>
        <span>轮播图管理</span>
      </el-menu-item>
      <el-menu-item index="/card">
        <el-icon><Ticket /></el-icon>
        <span>点卡管理</span>
      </el-menu-item>
      <el-menu-item index="/member">
        <el-icon><UserFilled /></el-icon>
        <span>会员管理</span>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  background-color: #2b2d3a;
  height: 100%;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 60px;
  padding: 0 20px;
  background-color: #232530;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.el-menu {
  border-right: none;
}

:deep(.el-menu-item.is-active) {
  box-shadow: inset 3px 0 0 #ff6034;
}
</style>

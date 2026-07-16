<template>
  <div class="app-container">
    <transition name="fade">
      <router-view />
    </transition>
    <TabBar v-if="showTabBar" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import TabBar from '@/components/TabBar.vue'
import { useMessageStore } from '@/store/modules/message'

const route = useRoute()
const showTabBar = computed(() => {
  return ['/home', '/menu', '/me'].includes(route.path)
})

// 初始化消息 WebSocket（幂等），用于实时接收推送与未读红点
const messageStore = useMessageStore()
onMounted(() => {
  messageStore.init()
})
</script>

<style>
.app-container {
  min-height: 100vh;
  padding-bottom: 56px;
}

.fade-enter-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
}
</style>

<template>
  <div class="app-container">
    <transition name="fade">
      <router-view />
    </transition>
    <TabBar v-if="showTabBar" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TabBar from '@/components/TabBar.vue'

const route = useRoute()
const showTabBar = computed(() => {
  return ['/home', '/menu', '/me'].includes(route.path)
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

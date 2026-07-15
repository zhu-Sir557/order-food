<template>
  <div class="about-us-page">
    <van-nav-bar title="关于我们" left-text="返回" left-arrow @click-left="onClickLeft" />

    <div class="content-wrapper">
      <div v-if="loading" class="loading-wrap">
        <van-loading type="spinner" color="#ff6034" />
      </div>
      <div v-else-if="aboutUsContent" class="rich-content" v-html="aboutUsContent"></div>
      <van-empty v-else description="暂无内容" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getMerchantConfig } from '@/api/merchant-config'

const aboutUsContent = ref('')
const loading = ref(false)

function onClickLeft(): void {
  history.back()
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getMerchantConfig()
    aboutUsContent.value = res.aboutUsContent || ''
    if (!aboutUsContent.value) {
      showToast('暂无内容')
    }
  } catch {
    // 业务错误已由请求拦截器统一提示
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.about-us-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.content-wrapper {
  padding: 16px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.rich-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text-primary);
  word-break: break-word;
}

.rich-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 8px 0;
}

.rich-content :deep(h1),
.rich-content :deep(h2),
.rich-content :deep(h3) {
  font-weight: 600;
  margin: 12px 0 8px;
}

.rich-content :deep(p) {
  margin: 8px 0;
}

.rich-content :deep(ul),
.rich-content :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}

.rich-content :deep(a) {
  color: var(--van-primary-color);
}
</style>

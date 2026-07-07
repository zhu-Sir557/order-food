<template>
  <div class="home-page">
    <van-nav-bar title="在线点餐" />

    <!-- 轮播图 -->
    <van-swipe v-if="banners.length > 0" class="home-swipe" :autoplay="3000" indicator-color="#ff6034">
      <van-swipe-item v-for="banner in banners" :key="banner.id">
        <img
          class="swipe-img"
          :src="banner.image"
          :style="{ cursor: banner.link ? 'pointer' : 'default' }"
          @click="onBannerClick(banner)"
        />
      </van-swipe-item>
    </van-swipe>

    <!-- 分类导航 - 横向滚动标签 -->
    <div class="category-section">
      <div class="section-title-row">
        <span class="section-title">菜品分类</span>
        <span class="section-subtitle">浏览全部</span>
      </div>
      <div class="category-scroll">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-card"
          @click="goMenu(cat.id)"
        >
          <div class="category-icon-wrap" :style="{ background: getCategoryGradient(cat.id) }">
            <span class="category-emoji">{{ getCategoryEmoji(cat.name) }}</span>
          </div>
          <span class="category-label">{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- 热门推荐 -->
    <div class="hot-section">
      <div class="section-title-row">
        <span class="section-title">🔥 热门推荐</span>
        <span class="section-subtitle">精选好菜</span>
      </div>
      <div class="hot-list">
        <DishCard
          v-for="dish in hotDishes"
          :key="dish.id"
          :dish="dish"
          @add="handleAdd"
        />
      </div>
      <van-empty v-if="hotDishes.length === 0 && !loading" description="暂无推荐菜品" />
    </div>

    <!-- 底部购物车栏 -->
    <CartBar />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import router from '@/router'
import { showToast } from 'vant'
import DishCard from '@/components/DishCard.vue'
import CartBar from '@/components/CartBar.vue'
import { getCategories, getDishes, getBanners } from '@/api/dish'
import { useCartStore } from '@/store/modules/cart'
import type { Category, Dish, Banner } from '@/types'

const cartStore = useCartStore()

const categories = ref<Category[]>([])
const hotDishes = ref<Dish[]>([])
const banners = ref<Banner[]>([])
const loading = ref(false)

/** 分类渐变色配置 */
const categoryGradients: Record<number, string> = {
  1: 'linear-gradient(135deg, #ff6b6b, #ee5a24)',
  2: 'linear-gradient(135deg, #55efc4, #00b894)',
  3: 'linear-gradient(135deg, #fdcb6e, #f39c12)',
  4: 'linear-gradient(135deg, #74b9ff, #0984e3)',
  5: 'linear-gradient(135deg, #a29bfe, #6c5ce7)',
}

/** 根据分类名称获取emoji图标 */
const getCategoryEmoji = (name: string): string => {
  const emojiMap: Record<string, string> = {
    '热菜': '🔥',
    '凉菜': '🥗',
    '主食': '🍚',
    '汤': '🍲',
    '羹': '🍲',
    '饮品': '🥤',
    '甜点': '🍰',
    '小吃': '🍟',
  }
  for (const key in emojiMap) {
    if (name.includes(key)) {
      return emojiMap[key]
    }
  }
  return '🍽️'
}

/** 根据分类ID获取渐变背景色 */
const getCategoryGradient = (categoryId: number): string => {
  return categoryGradients[categoryId] || 'linear-gradient(135deg, #ff9a56, #ff6034)'
}

const loadData = async (): Promise<void> => {
  loading.value = true
  try {
    const [cats, dishes, bannerList] = await Promise.all([
      getCategories(),
      getDishes(),
      getBanners(),
    ])
    categories.value = cats || []
    hotDishes.value = (dishes || []).slice(0, 6)
    banners.value = bannerList || []
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const goMenu = (categoryId: number): void => {
  router.push({ path: '/menu', query: { categoryId: String(categoryId) } })
}

/** 点击轮播图跳转 */
const onBannerClick = (banner: Banner): void => {
  if (!banner.link) return
  if (banner.link.startsWith('http://') || banner.link.startsWith('https://')) {
    window.location.href = banner.link
  } else {
    router.push(banner.link)
  }
}

/** 添加菜品到购物车（支持口味选择） */
const handleAdd = (dish: Dish, tasteSelection?: string): void => {
  cartStore.addItem(dish, tasteSelection)
  showToast(`已添加 ${dish.name}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--color-bg-page);
  padding-bottom: 60px;
}

.home-page :deep(.van-nav-bar) {
  box-shadow: 0 2px 8px rgba(255, 96, 52, 0.15);
}

.home-page :deep(.van-nav-bar__title) {
  font-size: 18px;
  font-weight: 700;
}

.home-swipe {
  height: 180px;
  margin: -20px 16px 0;
}

.swipe-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 12px;
}

/* 分类区域 */
.category-section {
  margin-top: 10px;
  background: #fff;
  padding: 14px 0 16px;
}

.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px 12px;
}

.section-title {
  font-size: var(--font-size-h2);
  font-weight: 700;
  color: var(--color-text-primary);
  border-left: 3px solid var(--color-primary);
  padding-left: 10px;
}

.section-subtitle {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
}

/* 横向滚动分类 */
.category-scroll {
  display: flex;
  overflow-x: auto;
  padding: 0 12px;
  gap: 14px;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
}

.category-scroll::-webkit-scrollbar {
  display: none;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  cursor: pointer;
}

.category-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.12);
  transition: transform 0.2s ease;
}

.category-card:active .category-icon-wrap {
  transform: scale(0.92);
}

.category-emoji {
  font-size: 26px;
  line-height: 1;
}

.category-label {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-primary);
  margin-top: 8px;
  white-space: nowrap;
}

/* 热门推荐区域 */
.hot-section {
  margin: 10px 12px 0;
  background: var(--color-bg-card);
  padding: 14px 0 8px;
  border-radius: var(--radius-card);
  overflow: hidden;
}

.hot-list {
  padding: 0 12px;
}
</style>

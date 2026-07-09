<template>
  <div class="menu-page">
    <van-nav-bar title="菜单" left-arrow @click-left="router.back()" />

    <!-- 搜索栏 -->
    <van-search
      v-model="keyword"
      placeholder="搜索菜品"
      shape="round"
      @search="onSearch"
      @clear="onClear"
    />

    <!-- 搜索结果模式 -->
    <div v-if="isSearchMode" class="search-results">
      <van-empty v-if="searchResults.length === 0" description="未找到相关菜品" />
      <div v-else class="dish-list-padding">
        <DishCard
          v-for="dish in searchResults"
          :key="dish.id"
          :dish="dish"
          @add="handleAdd"
        />
      </div>
    </div>

    <!-- 正常菜单模式 -->
    <div v-else class="menu-content">
      <!-- 左侧分类侧边栏 -->
      <div class="menu-sidebar">
          <div
            v-for="(cat, index) in categories"
            :key="cat.id"
            :ref="(el) => setSidebarRef(el, index)"
            class="sidebar-item"
            :class="{ active: activeCategory === index }"
            @click="onCategoryChange(index)"
          >
          <div v-if="activeCategory === index" class="active-bar"></div>
          <span class="sidebar-text">{{ cat.name }}</span>
        </div>
      </div>

      <!-- 右侧菜品列表 -->
      <div class="dish-scroll" ref="dishScrollRef">
        <div
          v-for="(cat, index) in categories"
          :key="cat.id"
          :ref="(el) => setSectionRef(el, index)"
          class="dish-section"
        >
          <div class="category-title">{{ cat.name }}</div>
          <DishCard
            v-for="dish in getDishesByCategory(cat.id)"
            :key="dish.id"
            :dish="dish"
            @add="handleAdd"
          />
          <van-empty
            v-if="getDishesByCategory(cat.id).length === 0"
            description="该分类暂无菜品"
            :image-size="80"
          />
        </div>
      </div>
    </div>

    <!-- 底部购物车栏 -->
    <CartBar />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import router from '@/router'
import { showToast } from 'vant'
import DishCard from '@/components/DishCard.vue'
import CartBar from '@/components/CartBar.vue'
import { getCategories, getDishes, searchDishes } from '@/api/dish'
import { useCartStore } from '@/store/modules/cart'
import type { Category, Dish } from '@/types'

const route = useRoute()
const cartStore = useCartStore()

const categories = ref<Category[]>([])
const allDishes = ref<Dish[]>([])
const activeCategory = ref(0)
const keyword = ref('')
const isSearchMode = ref(false)
const searchResults = ref<Dish[]>([])
const dishScrollRef = ref<HTMLElement | null>(null)
const sectionRefs = ref<HTMLElement[]>([])
const sidebarRefs = ref<HTMLElement[]>([])
/** 标记是否由点击分类触发的滚动，避免滚动监听反向覆盖选中状态 */
let isProgrammaticScroll = false

/** 设置分类区块的ref引用 */
const setSectionRef = (el: any, index: number): void => {
  if (el) {
    sectionRefs.value[index] = el as HTMLElement
  }
}

/** 设置左侧分类项的ref引用 */
const setSidebarRef = (el: any, index: number): void => {
  if (el) {
    sidebarRefs.value[index] = el as HTMLElement
  }
}

/** 按分类获取菜品 */
const getDishesByCategory = (categoryId: number): Dish[] => {
  return allDishes.value.filter((dish) => dish.categoryId === categoryId)
}

/** 加载分类和菜品数据 */
const loadData = async (): Promise<void> => {
  try {
    const [cats, dishes] = await Promise.all([
      getCategories(),
      getDishes(),
    ])
    categories.value = cats || []
    allDishes.value = dishes || []

    // 如果URL带categoryId参数，定位到对应分类
    const queryCategoryId = route.query.categoryId
    if (queryCategoryId) {
      const index = categories.value.findIndex(
        (c) => c.id === Number(queryCategoryId)
      )
      if (index >= 0) {
        activeCategory.value = index
        // 等待DOM更新后滚动
        await nextTick()
        setTimeout(() => onCategoryChange(index), 200)
      }
    }
  } catch (error) {
    console.error('加载菜单数据失败:', error)
  }
}

/** 左侧分类切换，右侧滚动到对应位置 */
const onCategoryChange = (index: number): void => {
  activeCategory.value = index
  isProgrammaticScroll = true
  const el = sectionRefs.value[index]
  if (el) {
    // scrollIntoView 会滚动最近的可滚动祖先元素（.dish-scroll）
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
  // 滚动动画结束后恢复滚动监听
  setTimeout(() => {
    isProgrammaticScroll = false
  }, 400)
}

/** 滚动监听回调 - 根据滚动位置更新左侧选中分类 */
const onDishScroll = (): void => {
  // 点击分类触发的程序滚动期间，不更新选中状态
  if (isProgrammaticScroll) return
  if (!dishScrollRef.value) return
  const containerTop = dishScrollRef.value.getBoundingClientRect().top
  let currentIdx = 0
  for (let i = 0; i < sectionRefs.value.length; i++) {
    const el = sectionRefs.value[i]
    if (el) {
      const rect = el.getBoundingClientRect()
      // 当分类区块的顶部到达容器顶部附近时，切换到该分类
      if (rect.top - containerTop <= 20) {
        currentIdx = i
      }
    }
  }
  activeCategory.value = currentIdx
}

/** 监听选中分类变化，自动将左侧选中项滚入可视区域 */
watch(activeCategory, (newIdx) => {
  const el = sidebarRefs.value[newIdx]
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  }
})

/** 搜索菜品 */
const onSearch = async (): Promise<void> => {
  if (!keyword.value.trim()) {
    isSearchMode.value = false
    return
  }
  isSearchMode.value = true
  try {
    searchResults.value = await searchDishes(keyword.value.trim())
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = []
  }
}

/** 清除搜索 */
const onClear = (): void => {
  isSearchMode.value = false
  searchResults.value = []
}

/** 添加菜品到购物车（支持口味选择） */
const handleAdd = (dish: Dish, tasteSelection?: string): void => {
  cartStore.addItem(dish, tasteSelection)
  showToast(`已添加 ${dish.name}`)
}

onMounted(() => {
  loadData()
  // 等待数据加载和 DOM 渲染后绑定 scroll 监听
  nextTick(() => {
    if (dishScrollRef.value) {
      dishScrollRef.value.addEventListener('scroll', onDishScroll, { passive: true })
    }
  })
})

onUnmounted(() => {
  if (dishScrollRef.value) {
    dishScrollRef.value.removeEventListener('scroll', onDishScroll)
  }
})
</script>

<style scoped>
.menu-page {
  height: 100vh;
  overflow: hidden;
  background: var(--color-bg-page);
  display: flex;
  flex-direction: column;
}

.menu-page :deep(.van-search) {
  background: var(--color-bg-page);
}

.menu-page :deep(.van-search__content) {
  box-shadow: var(--shadow-sm);
}

.menu-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  /* 留出底部 TabBar + CartBar 的空间 */
  padding-bottom: 100px;
}

/* 左侧分类侧边栏 - 固定不随右侧滚动 */
.menu-sidebar {
  width: 85px;
  flex-shrink: 0;
  height: 100%;
  overflow-y: auto;
  background: #f0f1f3;
  scrollbar-width: none;
}

.menu-sidebar::-webkit-scrollbar {
  display: none;
}

.sidebar-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 10px;
  font-size: var(--font-size-body-sm);
  color: #646566;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sidebar-item.active {
  background: #fff;
  color: var(--color-primary);
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.active-bar {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(180deg, #ff8a65, #ff6034);
}

.sidebar-text {
  text-align: center;
  line-height: 1.4;
  word-break: break-all;
}

.dish-scroll {
  flex: 1;
  height: 100%;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: 0 8px 16px;
}

.dish-section {
  margin-bottom: 4px;
}

.category-title {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  padding: 12px 4px 8px;
}

.category-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 14px;
  background: var(--color-primary);
  border-radius: 2px;
  margin-right: 6px;
  vertical-align: middle;
}

.search-results {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 50px;
}

.dish-list-padding {
  padding: 8px;
}
</style>

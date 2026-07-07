<template>
  <div class="dish-card">
    <van-image
      width="88"
      height="88"
      radius="10"
      fit="cover"
      :src="dish.image || defaultImage"
    />
    <div class="dish-info">
      <div class="dish-name">{{ dish.name }}</div>
      <div v-if="hasTasteConfig" class="taste-hint">
        <van-icon name="bookmark-o" class="taste-icon" />
        <span>可选口味</span>
      </div>
      <div class="dish-desc">{{ dish.description || '暂无描述' }}</div>
      <div class="dish-bottom">
        <span class="dish-price">¥{{ dish.price.toFixed(2) }}</span>
        <van-icon name="add" class="add-btn" @click.stop="handleAdd" />
      </div>
    </div>
    <!-- 口味选择弹窗 -->
    <TastePicker
      v-if="hasTasteConfig"
      v-model:show="showTastePicker"
      :dish="dish"
      @confirm="onTasteConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Dish } from '@/types'
import TastePicker from './TastePicker.vue'

const props = defineProps<{
  dish: Dish
}>()

const emit = defineEmits<{
  add: [dish: Dish, tasteSelection?: string]
}>()

const defaultImage = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const showTastePicker = ref(false)

/** 菜品是否有口味配置 */
const hasTasteConfig = computed(() => {
  return !!props.dish.tasteConfig && props.dish.tasteConfig.trim().length > 0
})

/** 点击加号：有口味配置则弹出口味选择，否则直接加入 */
const handleAdd = (): void => {
  if (hasTasteConfig.value) {
    showTastePicker.value = true
  } else {
    emit('add', props.dish)
  }
}

/** 口味选择确认后，携带口味信息加入购物车 */
const onTasteConfirm = (tasteSelection: string): void => {
  emit('add', props.dish, tasteSelection)
}
</script>

<style scoped>
.dish-card {
  display: flex;
  padding: var(--space-card-inner);
  background: var(--color-bg-card);
  border-radius: var(--radius-card);
  margin-bottom: var(--space-list-item);
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.2s ease;
}

.dish-card:active {
  box-shadow: var(--shadow-md);
}

.dish-info {
  flex: 1;
  margin-left: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.dish-name {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.taste-hint {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-top: 3px;
  font-size: var(--font-size-caption-mini);
  color: var(--color-primary);
  background: var(--color-primary-bg);
  padding: 1px 8px;
  border-radius: var(--radius-tag);
  width: fit-content;
}

.taste-icon {
  font-size: 12px;
}

.dish-desc {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin: 4px 0;
}

.dish-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  font-size: 16px;
  color: var(--color-primary);
  font-weight: var(--font-weight-bold);
}

.add-btn {
  font-size: 20px;
  color: #fff;
  background: var(--color-primary);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-primary);
  transition: transform 0.15s ease;
}

.add-btn:active {
  transform: scale(0.85);
}
</style>

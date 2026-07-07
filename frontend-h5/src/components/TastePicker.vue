<template>
  <van-popup
    :show="show"
    @update:show="onUpdateShow"
    position="bottom"
    round
    closeable
    :style="{ maxHeight: '75%' }"
  >
    <div class="taste-picker">
      <!-- 标题栏 -->
      <div class="picker-header">
        <div class="dish-name">{{ dish.name }}</div>
        <div class="dish-price">¥{{ (dish.price || 0).toFixed(2) }}</div>
      </div>

      <!-- 口味选项区域 -->
      <div class="taste-body">
        <div
          v-for="(group, gi) in tasteGroups"
          :key="gi"
          class="taste-group"
        >
          <div class="group-title">
            <span class="group-name">{{ group.name }}</span>
            <span v-if="group.required" class="required-badge">必选</span>
            <span v-else class="optional-badge">可选</span>
          </div>
          <div class="option-row">
            <div
              v-for="opt in group.options"
              :key="opt.value"
              class="option-chip"
              :class="{ selected: selectedValues[gi] === opt.value }"
              @click="selectOption(gi, opt.value)"
            >
              {{ opt.label }}
            </div>
          </div>
        </div>
      </div>

      <!-- 底部确认栏 -->
      <div class="picker-footer">
        <van-button
          type="primary"
          block
          round
          :disabled="!canConfirm"
          class="confirm-btn"
          @click="onConfirm"
        >
          加入购物车
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { showToast } from 'vant'
import type { Dish } from '@/types'

/** 口味配置选项接口 */
interface TasteOption {
  label: string
  value: string
}

/** 口味配置组接口 */
interface TasteGroup {
  name: string
  type: string
  required: boolean
  options: TasteOption[]
}

const props = defineProps<{
  /** 菜品对象 */
  dish: Dish
  /** 弹窗显示状态 */
  show: boolean
}>()

const emit = defineEmits<{
  /** 更新弹窗显示状态 */
  'update:show': [value: boolean]
  /** 确认选择口味，返回口味选择字符串 */
  confirm: [tasteSelection: string]
}>()

/** 解析口味配置 */
const tasteGroups = ref<TasteGroup[]>([])

/** 每个分组的选择值 */
const selectedValues = ref<Record<number, string>>({})

/** 解析 dish.tasteConfig JSON */
const parseTasteConfig = (): void => {
  tasteGroups.value = []
  selectedValues.value = {}
  if (props.dish.tasteConfig) {
    try {
      const parsed = JSON.parse(props.dish.tasteConfig) as TasteGroup[]
      if (Array.isArray(parsed)) {
        tasteGroups.value = parsed
      }
    } catch (e) {
      console.error('解析口味配置失败:', e)
    }
  }
}

/** 当弹窗打开时解析配置 */
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      parseTasteConfig()
    }
  }
)

/** 选择某个分组的选项 */
const selectOption = (groupIndex: number, value: string): void => {
  // 如果已选中同一个值，则取消选择（仅对非必选项生效）
  const group = tasteGroups.value[groupIndex]
  if (!group.required && selectedValues.value[groupIndex] === value) {
    delete selectedValues.value[groupIndex]
  } else {
    selectedValues.value[groupIndex] = value
  }
}

/** 是否所有必选项都已选择 */
const canConfirm = computed(() => {
  return tasteGroups.value.every(
    (group, gi) => !group.required || !!selectedValues.value[gi]
  )
})

/** 更新弹窗显示状态 */
const onUpdateShow = (val: boolean): void => {
  emit('update:show', val)
}

/** 确认选择，拼接口味字符串 */
const onConfirm = (): void => {
  if (!canConfirm.value) {
    const missing = tasteGroups.value.find(
      (g, gi) => g.required && !selectedValues.value[gi]
    )
    if (missing) {
      showToast(`请选择${missing.name}`)
    }
    return
  }

  const selections: string[] = []
  tasteGroups.value.forEach((group, gi) => {
    const val = selectedValues.value[gi]
    if (val) {
      selections.push(val)
    }
  })

  const result = selections.join(',')
  emit('confirm', result)
  emit('update:show', false)
}
</script>

<style scoped>
.taste-picker {
  display: flex;
  flex-direction: column;
  max-height: 75vh;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 12px;
  border-bottom: 1px solid #f2f3f5;
}

.dish-name {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.dish-price {
  font-size: 18px;
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.taste-body {
  flex: 1;
  overflow-y: auto;
  padding: 4px 16px 8px;
}

.taste-group {
  padding: 12px 0;
  border-bottom: 1px solid #f7f8fa;
}

.taste-group:last-child {
  border-bottom: none;
}

.group-title {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.group-name {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
}

.required-badge {
  font-size: 10px;
  color: #fff;
  background: var(--color-primary);
  padding: 1px 6px;
  border-radius: 4px;
  margin-left: 6px;
}

.optional-badge {
  font-size: 10px;
  color: var(--color-text-secondary);
  background: var(--color-bg-section);
  padding: 1px 6px;
  border-radius: 4px;
  margin-left: 6px;
}

.option-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.option-chip {
  padding: 8px 18px;
  border-radius: 20px;
  font-size: var(--font-size-body-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-section);
  border: 1.5px solid transparent;
  transition: all 0.2s ease;
  cursor: pointer;
  user-select: none;
}

.option-chip.selected {
  color: var(--color-primary);
  background: var(--color-primary-bg);
  border-color: var(--color-primary);
  font-weight: var(--font-weight-semi);
  box-shadow: 0 2px 8px rgba(255, 96, 52, 0.2);
}

.picker-footer {
  padding: 12px 16px 16px;
  border-top: 1px solid #f2f3f5;
}

.confirm-btn {
  --van-button-primary-background: var(--color-primary);
  --van-button-primary-border-color: var(--color-primary);
}
</style>

<template>
  <van-popup
    :show="show"
    round
    closeable
    close-icon="cross"
    :style="{ width: '320px', padding: '20px' }"
    @update:show="onUpdateShow"
  >
    <div class="nick-popup">
      <div class="nick-popup-title">修改昵称</div>

      <van-field
        v-model="nickname"
        label="昵称"
        placeholder="请输入昵称（最多20字）"
        maxlength="20"
        show-word-limit
      />

      <div class="nick-popup-tip" v-if="remaining !== null">
        今日还可修改 {{ remaining }} 次
      </div>

      <div class="nick-popup-btns">
        <van-button block round @click="onCancel">取消</van-button>
        <van-button
          block
          round
          type="primary"
          :loading="loading"
          loading-text="保存中..."
          @click="onConfirm"
        >
          保存
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { showToast } from 'vant'
import { useMemberStore } from '@/store/modules/member'

const props = defineProps<{
  /** 弹窗显隐（支持 v-model:show） */
  show: boolean
  /** 当前昵称（打开时回填） */
  currentNickname: string
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'success'): void
}>()

const memberStore = useMemberStore()

const nickname = ref('')
const loading = ref(false)
/** 今日剩余修改次数（打开弹窗时同步） */
const remaining = ref<number | null>(memberStore.nickRemaining)

/** 打开弹窗时回填当前昵称与剩余次数 */
watch(
  () => props.show,
  (visible) => {
    if (visible) {
      nickname.value = props.currentNickname
      remaining.value = memberStore.nickRemaining
    }
  }
)

/** 弹窗显隐变化（点击遮罩/关闭按钮） */
const onUpdateShow = (value: boolean): void => {
  emit('update:show', value)
}

/** 取消 */
const onCancel = (): void => {
  emit('update:show', false)
}

/** 确认保存：调修改昵称接口（后端强制每日≤3次） */
const onConfirm = async (): Promise<void> => {
  const name = nickname.value.trim()
  if (!name) {
    showToast('请输入昵称')
    return
  }
  if (name.length > 20) {
    showToast('昵称最多20字')
    return
  }
  loading.value = true
  try {
    const res = await memberStore.updateNickname({ nickname: name })
    remaining.value = res
    showToast('昵称已更新')
    emit('success')
    emit('update:show', false)
  } catch (error) {
    console.error('修改昵称失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.nick-popup-title {
  text-align: center;
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  margin-bottom: 16px;
}

.nick-popup-tip {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
  margin: 4px 0 12px;
  text-align: center;
}

.nick-popup-btns {
  display: flex;
  gap: 12px;
}
</style>

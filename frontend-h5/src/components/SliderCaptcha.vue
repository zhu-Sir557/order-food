<template>
  <div class="slider-captcha">
    <!-- 验证码图片区域 -->
    <div class="captcha-image" ref="containerRef">
      <img v-if="captchaData.backgroundImage" :src="captchaData.backgroundImage" class="bg-image" alt="背景图" />
      <img v-if="captchaData.puzzleImage" :src="captchaData.puzzleImage" class="puzzle-image"
        :style="{ top: captchaData.y + 'px', left: puzzleLeft + 'px' }" alt="拼图块" />
      <div v-if="loading" class="captcha-loading">
        <van-loading type="spinner" />
      </div>
    </div>

    <!-- 滑块条 -->
    <div class="slider-track" ref="trackRef">
      <div class="slider-progress" :style="{ width: sliderLeft + 'px' }"></div>
      <div class="slider-text" v-if="!dragging && !verified">向右拖动滑块完成验证</div>
      <div class="slider-text success" v-if="verified">验证通过</div>
      <div class="slider-handle" ref="handleRef"
        :class="{ dragging: dragging, verified: verified }"
        :style="{ left: sliderLeft + 'px' }"
        @mousedown="onMouseDown"
        @touchstart.passive="onTouchStart">
        <van-icon :name="verified ? 'success' : 'arrow'" />
      </div>
    </div>

    <!-- 刷新按钮 -->
    <div class="refresh-btn" @click="loadCaptcha">
      <van-icon name="replay" size="16" />
      <span>换一张</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSliderCaptcha, checkSlider } from '@/api/captcha'
import type { SliderCaptchaVO } from '@/api/captcha'

const emit = defineEmits<{
  (e: 'success', captchaToken: string): void
  (e: 'fail'): void
}>()

const containerRef = ref<HTMLElement>()
const trackRef = ref<HTMLElement>()
const handleRef = ref<HTMLElement>()

const loading = ref(false)
const dragging = ref(false)
const verified = ref(false)

const captchaData = reactive<SliderCaptchaVO>({
  captchaId: '',
  backgroundImage: '',
  puzzleImage: '',
  y: 0,
})

const sliderLeft = ref(0)
const puzzleLeft = ref(0)

/** 滑块条宽度减去滑块宽度 = 最大可拖动距离 */
const maxSlide = 260
/** 拼图块起始偏移（对齐滑块位置） */
const puzzleOffset = 10

/** 加载验证码 */
const loadCaptcha = async (): Promise<void> => {
  loading.value = true
  verified.value = false
  sliderLeft.value = 0
  puzzleLeft.value = 0
  try {
    const data = await getSliderCaptcha()
    captchaData.captchaId = data.captchaId
    captchaData.backgroundImage = data.backgroundImage
    captchaData.puzzleImage = data.puzzleImage
    captchaData.y = data.y
  } catch (error) {
    console.error('加载验证码失败:', error)
  } finally {
    loading.value = false
  }
}

/** 拖动开始 */
const startDrag = (clientX: number): void => {
  if (verified.value || loading.value) return
  dragging.value = true
  const handleRect = handleRef.value?.getBoundingClientRect()
  if (!handleRect) return
  // 记录起始位置
  startX = clientX
  startLeft = sliderLeft.value
}

let startX = 0
let startLeft = 0

/** 拖动中 */
const onMove = (clientX: number): void => {
  if (!dragging.value) return
  const delta = clientX - startX
  let newLeft = startLeft + delta
  if (newLeft < 0) newLeft = 0
  if (newLeft > maxSlide) newLeft = maxSlide
  sliderLeft.value = newLeft
  puzzleLeft.value = newLeft + puzzleOffset
}

/** 拖动结束 */
const endDrag = async (): Promise<void> => {
  if (!dragging.value) return
  dragging.value = false

  // 校验
  try {
    const result = await checkSlider(captchaData.captchaId, puzzleLeft.value)
    if (result.success && result.captchaToken) {
      verified.value = true
      emit('success', result.captchaToken)
    } else {
      emit('fail')
      // 验证失败，重置并重新加载
      setTimeout(() => {
        loadCaptcha()
      }, 800)
    }
  } catch (error) {
    console.error('校验滑块失败:', error)
    emit('fail')
    loadCaptcha()
  }
}

/** 鼠标事件 */
const onMouseDown = (e: MouseEvent): void => {
  startDrag(e.clientX)
  const onMouseMove = (ev: MouseEvent): void => onMove(ev.clientX)
  const onMouseUp = (): void => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
    endDrag()
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

/** 触摸事件 */
const onTouchStart = (e: TouchEvent): void => {
  startDrag(e.touches[0].clientX)
  const onTouchMove = (ev: TouchEvent): void => onMove(ev.touches[0].clientX)
  const onTouchEnd = (): void => {
    document.removeEventListener('touchmove', onTouchMove)
    document.removeEventListener('touchend', onTouchEnd)
    endDrag()
  }
  document.addEventListener('touchmove', onTouchMove, { passive: true })
  document.addEventListener('touchend', onTouchEnd)
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped>
.slider-captcha {
  width: 310px;
  margin: 0 auto;
}

.captcha-image {
  position: relative;
  width: 310px;
  height: 160px;
  border-radius: 4px;
  overflow: hidden;
  background: #f0f0f0;
}

.bg-image {
  width: 100%;
  height: 100%;
  display: block;
}

.puzzle-image {
  position: absolute;
  height: 44px;
  width: 44px;
  transition: none;
  filter: drop-shadow(0 0 2px rgba(0, 0, 0, 0.3));
}

.captcha-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.7);
}

.slider-track {
  position: relative;
  width: 310px;
  height: 40px;
  margin-top: 10px;
  background: #f0f0f0;
  border-radius: 4px;
  border: 1px solid #ddd;
  overflow: hidden;
}

.slider-progress {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: rgba(102, 175, 255, 0.3);
  transition: none;
}

.slider-text {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 13px;
  color: #999;
  pointer-events: none;
}

.slider-text.success {
  color: #67c23a;
}

.slider-handle {
  position: absolute;
  top: 0;
  left: 0;
  width: 50px;
  height: 40px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  user-select: none;
  z-index: 2;
  transition: background 0.2s;
}

.slider-handle.dragging {
  cursor: grabbing;
  background: #e6f0ff;
}

.slider-handle.verified {
  background: #67c23a;
  color: #fff;
  border-color: #67c23a;
}

.refresh-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  cursor: pointer;
}

.refresh-btn:hover {
  color: #409eff;
}
</style>

<template>
  <div class="rich-text-editor">
    <div class="rte-toolbar">
      <button type="button" class="rte-btn" title="加粗" @mousedown.prevent="exec('bold')"><b>B</b></button>
      <button type="button" class="rte-btn" title="标题" @mousedown.prevent="exec('formatBlock', 'h2')">H</button>
      <button type="button" class="rte-btn" title="无序列表" @mousedown.prevent="exec('insertUnorderedList')">• 列表</button>
      <el-upload
        class="rte-upload"
        :show-file-list="false"
        action="/api/admin/upload"
        :headers="uploadHeaders"
        :before-upload="beforeUpload"
        :on-success="onUploadSuccess"
        :on-error="onUploadError"
      >
        <button type="button" class="rte-btn" title="插入图片" @mousedown.prevent>图片</button>
      </el-upload>
    </div>
    <div
      ref="editorRef"
      class="rte-content"
      contenteditable="true"
      @input="onInput"
      @keyup="saveSelection"
      @mouseup="saveSelection"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, type UploadFile, type UploadFiles, type UploadProps } from 'element-plus'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const editorRef = ref<HTMLDivElement | null>(null)

/** 上传所需鉴权头（与现有 BannerDialog 一致，从 localStorage 读取 admin token） */
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('admin_token') || ''}`
}

/** 缓存编辑器内最近一次选区，用于失焦后（如图片上传完成）恢复插入位置 */
let savedRange: Range | null = null

/** 将外部传入的内容渲染进 contenteditable */
function renderContent(value: string): void {
  const el = editorRef.value
  if (!el) return
  const next = value || ''
  if (el.innerHTML !== next) {
    el.innerHTML = next
  }
}

onMounted(() => {
  renderContent(props.modelValue)
})

watch(
  () => props.modelValue,
  (val) => {
    renderContent(val)
  }
)

function onInput(): void {
  const el = editorRef.value
  if (!el) return
  emit('update:modelValue', el.innerHTML)
}

function saveSelection(): void {
  const sel = window.getSelection()
  if (!sel || sel.rangeCount === 0) return
  const range = sel.getRangeAt(0)
  if (editorRef.value && editorRef.value.contains(range.commonAncestorContainer)) {
    savedRange = range.cloneRange()
  }
}

function restoreSelection(): void {
  const sel = window.getSelection()
  if (!sel || !editorRef.value) return
  if (savedRange) {
    sel.removeAllRanges()
    sel.addRange(savedRange)
  } else {
    // 无缓存选区时把光标放到编辑器末尾
    editorRef.value.focus()
    const range = document.createRange()
    range.selectNodeContents(editorRef.value)
    range.collapse(false)
    sel.removeAllRanges()
    sel.addRange(range)
  }
}

/** 执行富文本命令（加粗 / 标题 / 列表等），并同步 v-model */
function exec(command: string, value?: string): void {
  restoreSelection()
  document.execCommand(command, false, value)
  onInput()
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

const onUploadSuccess: UploadProps['onSuccess'] = (response: any, _file: UploadFile, _fileList: UploadFiles) => {
  if (response && response.code === 200 && response.data) {
    restoreSelection()
    document.execCommand('insertImage', false, response.data)
    onInput()
    ElMessage.success('图片插入成功')
  } else {
    ElMessage.error((response && response.message) || '图片上传失败')
  }
}

const onUploadError: UploadProps['onError'] = () => {
  ElMessage.error('图片上传失败，请重试')
}
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
}

.rte-toolbar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  background: #f7f8fa;
  border-bottom: 1px solid var(--el-border-color);
}

.rte-btn {
  border: 1px solid var(--el-border-color);
  background: #fff;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  cursor: pointer;
  line-height: 1.4;
}

.rte-btn:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.rte-content {
  min-height: 220px;
  max-height: 480px;
  overflow-y: auto;
  padding: 12px 14px;
  outline: none;
  font-size: 14px;
  line-height: 1.7;
  color: var(--el-text-color-primary);
}

.rte-content:empty::before {
  content: '请输入关于我们的介绍内容…';
  color: #c0c4cc;
}
</style>

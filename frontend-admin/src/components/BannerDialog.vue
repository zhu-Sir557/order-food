<template>
  <el-dialog
    :model-value="visible"
    :title="bannerData ? '编辑轮播图' : '新增轮播图'"
    width="500px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入轮播图标题（可选）" />
      </el-form-item>
      <el-form-item label="图片" prop="image">
        <el-upload
          class="image-uploader"
          action="/api/admin/upload"
          :show-file-list="false"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :before-upload="beforeUpload"
        >
          <img v-if="formData.image" :src="formData.image" class="preview-image" />
          <div v-else class="upload-placeholder">
            <el-icon :size="28"><Plus /></el-icon>
            <span>点击上传</span>
          </div>
        </el-upload>
      </el-form-item>
      <el-form-item label="跳转链接" prop="link">
        <el-input
          v-model="formData.link"
          placeholder="不填则纯展示，填了点击可跳转（如 /menu）"
        />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" :step="1" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadProps } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { Banner } from '@/types'

const props = defineProps<{
  visible: boolean
  bannerData: Banner | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'save', data: Partial<Banner>, id?: number): void
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = reactive({
  title: '',
  image: '',
  link: '',
  sort: 0
})

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('admin_token') || ''}`
}

const rules: FormRules = {
  image: [{ required: true, message: '请上传图片', trigger: 'change' }]
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.bannerData) {
        formData.title = props.bannerData.title || ''
        formData.image = props.bannerData.image
        formData.link = props.bannerData.link || ''
        formData.sort = props.bannerData.sort
      } else {
        resetForm()
      }
    }
  }
)

function resetForm(): void {
  formData.title = ''
  formData.image = ''
  formData.link = ''
  formData.sort = 0
}

const handleUploadSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    formData.image = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  return true
}

function handleClose(): void {
  emit('update:visible', false)
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const data: Partial<Banner> = {
        title: formData.title || undefined,
        image: formData.image,
        link: formData.link || undefined,
        sort: formData.sort
      }
      emit('save', data, props.bannerData?.id)
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.image-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 10px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 280px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.preview-image {
  width: 280px;
  height: 120px;
  object-fit: cover;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #8c939d;
  font-size: 12px;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}
</style>

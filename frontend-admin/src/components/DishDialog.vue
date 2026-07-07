<template>
  <el-dialog
    :model-value="visible"
    :title="dishData ? '编辑菜品' : '新增菜品'"
    width="560px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入菜品名称" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
          <el-option
            v-for="cat in categoryList"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input-number
          v-model="formData.price"
          :min="0"
          :precision="2"
          :step="1"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="库存" prop="stock">
        <el-input-number
          v-model="formData.stock"
          :min="0"
          :step="1"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入菜品描述"
        />
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
      <el-form-item label="口味配置">
        <div class="taste-config">
          <div
            v-for="(group, gIndex) in tasteGroups"
            :key="gIndex"
            class="taste-group"
          >
            <div class="taste-group-header">
              <el-input
                v-model="group.name"
                placeholder="口味组名称（如：辣度）"
                style="width: 180px"
              />
              <el-select v-model="group.type" style="width: 100px" placeholder="选择方式">
                <el-option label="单选" value="single" />
                <el-option label="多选" value="multi" />
              </el-select>
              <span class="taste-label">必选</span>
              <el-switch v-model="group.required" />
              <el-button type="danger" link @click="removeTasteGroup(gIndex)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
            <div class="taste-options">
              <el-tag
                v-for="(opt, oIndex) in group.options"
                :key="oIndex"
                closable
                @close="removeTasteOption(gIndex, oIndex)"
              >
                {{ opt.label }}
              </el-tag>
              <el-input
                v-if="group.inputVisible"
                v-model="group.inputValue"
                class="option-input"
                size="small"
                placeholder="选项名"
                @keyup.enter="confirmOption(gIndex)"
                @blur="confirmOption(gIndex)"
              />
              <el-button v-else size="small" @click="group.inputVisible = true">
                + 添加选项
              </el-button>
            </div>
          </div>
          <el-button type="primary" plain @click="addTasteGroup">
            + 添加口味组
          </el-button>
        </div>
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
import { Plus, Delete } from '@element-plus/icons-vue'
import type { Dish, Category } from '@/types'

const props = defineProps<{
  visible: boolean
  dishData: Dish | null
  categoryList: Category[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'save', data: Partial<Dish>, id?: number): void
  (e: 'close'): void
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = reactive({
  name: '',
  categoryId: undefined as number | undefined,
  price: 0,
  stock: 0,
  description: '',
  image: '',
  tasteConfig: '' as string
})

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('admin_token') || ''}`
}

const rules: FormRules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

/** 口味选项 */
interface TasteOption {
  label: string
  value: string
}

/** 口味组 */
interface TasteGroup {
  name: string
  type: string
  required: boolean
  options: TasteOption[]
  inputVisible: boolean
  inputValue: string
}

/** 口味配置组列表（编辑时的中间状态） */
const tasteGroups = ref<TasteGroup[]>([])

/** 添加口味组 */
function addTasteGroup(): void {
  tasteGroups.value.push({
    name: '',
    type: 'single',
    required: false,
    options: [],
    inputVisible: false,
    inputValue: ''
  })
}

/** 删除口味组 */
function removeTasteGroup(index: number): void {
  tasteGroups.value.splice(index, 1)
}

/** 确认添加口味选项 */
function confirmOption(gIndex: number): void {
  const group = tasteGroups.value[gIndex]
  if (!group) return
  const value = group.inputValue.trim()
  if (value) {
    group.options.push({ label: value, value: value })
  }
  group.inputVisible = false
  group.inputValue = ''
}

/** 删除口味选项 */
function removeTasteOption(gIndex: number, oIndex: number): void {
  const group = tasteGroups.value[gIndex]
  if (group) {
    group.options.splice(oIndex, 1)
  }
}

/** 将 tasteGroups 序列化为 JSON 字符串存入 formData.tasteConfig */
function serializeTasteGroups(): void {
  const validGroups = tasteGroups.value.filter(
    g => g.name.trim() !== '' || g.options.length > 0
  )
  const config = validGroups.map(g => ({
    name: g.name.trim(),
    type: g.type,
    required: g.required,
    options: g.options.map(o => ({ label: o.label, value: o.value }))
  }))
  formData.tasteConfig = config.length > 0 ? JSON.stringify(config) : ''
}

/** 从 formData.tasteConfig 解析 JSON 渲染到 tasteGroups */
function parseTasteGroups(): void {
  if (!formData.tasteConfig) {
    tasteGroups.value = []
    return
  }
  try {
    const parsed = JSON.parse(formData.tasteConfig) as Array<{
      name: string
      type: string
      required: boolean
      options: TasteOption[]
    }>
    tasteGroups.value = parsed.map(g => ({
      name: g.name || '',
      type: g.type || 'single',
      required: !!g.required,
      options: Array.isArray(g.options) ? g.options : [],
      inputVisible: false,
      inputValue: ''
    }))
  } catch (e) {
    console.error('解析口味配置失败:', e)
    tasteGroups.value = []
  }
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.dishData) {
        formData.name = props.dishData.name
        formData.categoryId = props.dishData.categoryId
        formData.price = props.dishData.price
        formData.stock = props.dishData.stock
        formData.description = props.dishData.description
        formData.image = props.dishData.image
        formData.tasteConfig = props.dishData.tasteConfig || ''
        parseTasteGroups()
      } else {
        resetForm()
      }
    }
  }
)

function resetForm(): void {
  formData.name = ''
  formData.categoryId = undefined
  formData.price = 0
  formData.stock = 0
  formData.description = ''
  formData.image = ''
  formData.tasteConfig = ''
  tasteGroups.value = []
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
  emit('close')
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      // 将口味组配置序列化为 JSON 字符串
      serializeTasteGroups()
      const data: Partial<Dish> = {
        name: formData.name,
        categoryId: formData.categoryId!,
        price: formData.price,
        stock: formData.stock,
        description: formData.description,
        image: formData.image,
        status: props.dishData?.status ?? 1,
        tasteConfig: formData.tasteConfig || undefined
      }
      emit('save', data, props.dishData?.id)
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.image-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-color-primary-light-5);
  border-radius: 10px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 148px;
  height: 148px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.preview-image {
  width: 148px;
  height: 148px;
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

.taste-config {
  width: 100%;
}

.taste-group {
  border: 1px solid var(--el-color-primary-light-7);
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 12px;
  background-color: var(--el-color-primary-light-9);
}

.taste-group-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.taste-label {
  font-size: 13px;
  color: var(--color-text-regular);
}

.taste-options {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.option-input {
  width: 120px;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}
</style>

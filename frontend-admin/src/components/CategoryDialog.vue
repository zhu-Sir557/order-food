<template>
  <el-dialog
    :model-value="visible"
    :title="categoryData ? '编辑分类' : '新增分类'"
    width="440px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" :step="1" style="width: 100%" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { type FormInstance, type FormRules } from 'element-plus'
import type { Category } from '@/types'

const props = defineProps<{
  visible: boolean
  categoryData: Category | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'save', data: Partial<Category>, id?: number): void
  (e: 'close'): void
}>()

const formRef = ref<FormInstance>()

const formData = reactive({
  name: '',
  sort: 0,
  status: 1
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }]
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.categoryData) {
        formData.name = props.categoryData.name
        formData.sort = props.categoryData.sort
        formData.status = props.categoryData.status
      } else {
        formData.name = ''
        formData.sort = 0
        formData.status = 1
      }
    }
  }
)

function handleClose(): void {
  emit('update:visible', false)
  emit('close')
}

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    const data: Partial<Category> = {
      name: formData.name,
      sort: formData.sort,
      status: formData.status
    }
    emit('save', data, props.categoryData?.id)
  })
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="tableData ? '编辑桌台' : '新增桌台'"
    width="440px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="桌号" prop="code">
        <el-input v-model="formData.code" placeholder="请输入桌号，如 A01" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入桌台名称" />
      </el-form-item>
      <el-form-item label="容量" prop="capacity">
        <el-input-number v-model="formData.capacity" :min="1" :max="50" :step="1" style="width: 100%" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" style="width: 100%">
          <el-option label="空闲" :value="0" />
          <el-option label="使用中" :value="1" />
          <el-option label="已预约" :value="2" />
          <el-option label="待清理" :value="3" />
        </el-select>
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
import type { DiningTable } from '@/types'

const props = defineProps<{
  visible: boolean
  tableData: DiningTable | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'save', data: Partial<DiningTable>, id?: number): void
  (e: 'close'): void
}>()

const formRef = ref<FormInstance>()

const formData = reactive({
  code: '',
  name: '',
  capacity: 4,
  status: 0
})

const rules: FormRules = {
  code: [{ required: true, message: '请输入桌号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入桌台名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.tableData) {
        formData.code = props.tableData.code
        formData.name = props.tableData.name
        formData.capacity = props.tableData.capacity
        formData.status = props.tableData.status
      } else {
        formData.code = ''
        formData.name = ''
        formData.capacity = 4
        formData.status = 0
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
    const data: Partial<DiningTable> = {
      code: formData.code,
      name: formData.name,
      capacity: formData.capacity,
      status: formData.status
    }
    emit('save', data, props.tableData?.id)
  })
}
</script>

<template>
  <div class="merchant-config-page">
    <el-card shadow="never" class="form-card">
      <template #header>
        <span class="card-title">商家设置</span>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="90px"
        v-loading="loading"
      >
        <el-form-item label="关于我们" prop="aboutUsContent">
          <RichTextEditor v-model="formData.aboutUsContent" />
        </el-form-item>

        <el-form-item label="联系商家" prop="contactPhone">
          <el-input
            v-model="formData.contactPhone"
            placeholder="请输入手机号（如 13800138000）或固话（如 010-12345678）"
            maxlength="32"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import RichTextEditor from '@/components/RichTextEditor.vue'
import {
  getMerchantConfig,
  saveMerchantConfig,
  updateMerchantConfig,
  type MerchantConfigSaveDTO,
} from '@/api/merchant-config'

const loading = ref(false)
const saving = ref(false)

/** 已存在的配置 id（固定为 1）；无配置时为 null，用于决定 POST / PUT */
const configId = ref<number | null>(null)

const formData = reactive({
  aboutUsContent: '',
  contactPhone: '',
})

const formRef = ref<FormInstance>()

const rules: FormRules = {
  contactPhone: [
    {
      pattern: /^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})?$/,
      message: '联系电话格式不正确（需为 11 位手机号或区号-号码固话）',
      trigger: 'blur',
    },
  ],
}

/** 拉取现有配置 */
async function fetchConfig(): Promise<void> {
  loading.value = true
  try {
    const res = await getMerchantConfig()
    configId.value = res.id ?? null
    formData.aboutUsContent = res.aboutUsContent || ''
    formData.contactPhone = res.contactPhone || ''
  } catch {
    // 业务错误已由请求拦截器统一提示
  } finally {
    loading.value = false
  }
}

/** 保存（已存在则 PUT，否则 POST） */
async function handleSubmit(): Promise<void> {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    const dto: MerchantConfigSaveDTO = {
      aboutUsContent: formData.aboutUsContent,
      contactPhone: formData.contactPhone || undefined,
    }
    try {
      if (configId.value) {
        await updateMerchantConfig(dto)
      } else {
        await saveMerchantConfig(dto)
      }
      ElMessage.success('保存成功')
      // 保存后重新拉取，刷新 configId 与最新内容
      await fetchConfig()
    } catch {
      // 业务错误（含电话格式/超长校验）已由请求拦截器统一提示
    } finally {
      saving.value = false
    }
  })
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped>
.form-card {
  border-radius: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.merchant-config-page {
  max-width: 900px;
}
</style>

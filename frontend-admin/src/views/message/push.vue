<template>
  <div class="message-push-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息推送</span>
        </div>
      </template>

      <!-- 发送表单 -->
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" class="push-form">
        <el-form-item label="消息类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择消息类型" style="width: 220px">
            <el-option label="全员广播" value="BROADCAST" />
            <el-option label="指定用户" value="SPECIFIED" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.type === 'SPECIFIED'" label="接收范围" prop="receiverIds">
          <el-select
            v-model="form.receiverIds"
            multiple
            filterable
            remote
            :remote-method="searchMembers"
            :loading="memberLoading"
            placeholder="搜索并选择会员"
            style="width: 360px"
          >
            <el-option
              v-for="m in memberOptions"
              :key="m.id"
              :label="m.username"
              :value="m.id"
            />
          </el-select>
          <span class="selected-tip" v-if="form.receiverIds.length">已选 {{ form.receiverIds.length }} 人</span>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="请输入标题（≤50字）" style="width: 360px" />
        </el-form-item>

        <el-form-item label="正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入正文内容"
            style="width: 480px"
          />
        </el-form-item>

        <el-form-item label="图片">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :http-request="customUpload"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="preview-img" alt="预览" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
          <el-button v-if="form.imageUrl" link type="danger" @click="form.imageUrl = ''">移除图片</el-button>
        </el-form-item>

        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="可选，如 https://... 或 /pages/..." style="width: 360px" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 发送记录 -->
    <el-card class="record-card">
      <template #header>
        <div class="card-header">
          <span>发送记录</span>
        </div>
      </template>

      <el-table :data="records" v-loading="loading" border style="width: 100%">
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column label="范围" width="100">
          <template #default="{ row }">{{ scopeText(row.receiverScope) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SENT' ? 'success' : 'info'" size="small">
              {{ row.status === 'SENT' ? '已发送' : '已撤回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDetail(row as MessageVO)">详情</el-button>
            <el-button
              v-if="row.revocable"
              type="warning"
              size="small"
              link
              @click="handleRevoke(row as MessageVO)"
            >撤回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadRecords"
        @current-change="loadRecords"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="560px">
      <div v-if="currentDetail" class="detail-content">
        <h3 class="detail-title">{{ currentDetail.title }}</h3>
        <div class="detail-time">{{ currentDetail.createTime }}</div>
        <img v-if="currentDetail.imageUrl" :src="currentDetail.imageUrl" class="detail-img" alt="图片" />
        <p class="detail-body">{{ currentDetail.content }}</p>
        <el-link v-if="currentDetail.linkUrl" type="primary" :href="currentDetail.linkUrl" target="_blank">
          查看详情链接
        </el-link>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadProps } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/api/request'
import { sendMessage, getMessageList, getMessageDetail, revokeMessage } from '@/api/message'
import type { MessageVO } from '@/api/message'
import { getMemberList } from '@/api/member'
import type { AdminMember } from '@/types'

const formRef = ref<FormInstance>()
const sending = ref(false)

const form = reactive<{
  type: 'BROADCAST' | 'SPECIFIED'
  title: string
  content: string
  imageUrl: string
  linkUrl: string
  receiverIds: number[]
}>({
  type: 'BROADCAST',
  title: '',
  content: '',
  imageUrl: '',
  linkUrl: '',
  receiverIds: [],
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择消息类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }],
  receiverIds: [
    {
      validator: (_rule, value: number[], callback) => {
        if (form.type === 'SPECIFIED' && (!value || value.length === 0)) {
          callback(new Error('指定用户发送时至少选择一个接收人'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

// ============ 会员选择 ============
const memberLoading = ref(false)
const memberOptions = ref<AdminMember[]>([])

const searchMembers = async (keyword?: string): Promise<void> => {
  memberLoading.value = true
  try {
    const res = await getMemberList({ page: 1, size: 200, keyword: keyword || '' })
    memberOptions.value = res.records
  } catch {
    // 业务错误已由拦截器统一提示
  } finally {
    memberLoading.value = false
  }
}

// ============ 图片上传 ============
const beforeImageUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('仅支持上传图片文件')
    return false
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const customUpload = async (options: { file: File }): Promise<void> => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const url = (await request.post<string>('/api/admin/upload', formData)) as unknown as string
    form.imageUrl = url
    ElMessage.success('图片上传成功')
  } catch {
    // 业务错误已由拦截器统一提示
  }
}

// ============ 发送 ============
const handleSend = (): void => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    try {
      await ElMessageBox.confirm('确认发送该消息？', '提示', { type: 'warning' })
    } catch {
      return
    }
    sending.value = true
    try {
      await sendMessage({ ...form })
      ElMessage.success('发送成功')
      resetForm()
      query.page = 1
      await loadRecords()
    } catch {
      // 错误提示已由拦截器处理
    } finally {
      sending.value = false
    }
  })
}

const resetForm = (): void => {
  formRef.value?.resetFields()
  form.imageUrl = ''
  form.linkUrl = ''
  form.receiverIds = []
}

// ============ 发送记录 ============
const loading = ref(false)
const records = ref<MessageVO[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

const loadRecords = async (): Promise<void> => {
  loading.value = true
  try {
    const res = await getMessageList({ ...query })
    records.value = res.records
    total.value = res.total
  } catch {
    // 错误提示已由拦截器处理
  } finally {
    loading.value = false
  }
}

const typeText = (type?: string): string =>
  type === 'BROADCAST' ? '全员广播' : type === 'SPECIFIED' ? '指定用户' : type || '-'
const scopeText = (scope?: string): string =>
  scope === 'ALL' ? '全部' : scope === 'SPECIFIED' ? '指定' : scope || '-'

// ============ 详情 / 撤回 ============
const detailVisible = ref(false)
const currentDetail = ref<MessageVO | null>(null)

const openDetail = async (row: MessageVO): Promise<void> => {
  try {
    currentDetail.value = await getMessageDetail(row.id)
    detailVisible.value = true
  } catch {
    // 错误提示已由拦截器处理
  }
}

const handleRevoke = async (row: MessageVO): Promise<void> => {
  try {
    await ElMessageBox.confirm('确认撤回该消息？撤回后在线用户将不再可见。', '撤回确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    await revokeMessage(row.id)
    ElMessage.success('撤回成功')
    await loadRecords()
  } catch {
    // 错误提示已由拦截器处理
  }
}

onMounted(() => {
  searchMembers()
  loadRecords()
})
</script>

<style scoped>
.message-push-page {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.push-form {
  max-width: 640px;
}

.selected-tip {
  margin-left: 12px;
  color: var(--el-color-primary);
  font-size: 13px;
}

.image-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader :deep(.el-upload):hover {
  border-color: var(--el-color-primary);
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.preview-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
}

.record-card {
  margin-top: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-title {
  margin: 0 0 8px;
  font-size: 18px;
}

.detail-time {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin-bottom: 12px;
}

.detail-img {
  max-width: 100%;
  border-radius: 8px;
  margin-bottom: 12px;
}

.detail-body {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>

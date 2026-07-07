<template>
  <div class="recharge-page">
    <van-nav-bar title="兑换点卡" left-arrow @click-left="router.back()" />

    <!-- 余额展示 -->
    <div class="balance-card">
      <div class="balance-label">当前余额</div>
      <div class="balance-value">¥{{ memberStore.balance.toFixed(2) }}</div>
    </div>

    <!-- 我的点卡列表 -->
    <div v-if="myCards.length > 0" class="my-cards-section">
      <div class="section-title">我的点卡（待兑换）</div>
      <div v-for="card in myCards" :key="card.id" class="card-item">
        <div class="card-info">
          <div class="card-amount">¥{{ card.amount.toFixed(2) }}</div>
          <div class="card-no">卡号：{{ card.cardNo }}</div>
          <div class="card-time">发放时间：{{ card.assignedAt }}</div>
        </div>
        <van-button
          type="primary"
          size="small"
          round
          :loading="redeemingId === card.id"
          @click="quickRedeem(card)"
        >
          兑换
        </van-button>
      </div>
    </div>

    <!-- 手动兑换表单 -->
    <div class="manual-section">
      <div class="section-title">手动兑换</div>
      <van-form @submit="handleRedeem" class="redeem-form">
        <van-cell-group inset>
          <van-field
            v-model="form.cardNo"
            name="cardNo"
            label="卡号"
            placeholder="请输入卡号"
            :rules="[{ required: true, message: '请输入卡号' }]"
          />
          <van-field
            v-model="form.cardPassword"
            name="cardPassword"
            label="卡密"
            placeholder="请输入卡密"
            :rules="[{ required: true, message: '请输入卡密' }]"
          />
        </van-cell-group>

        <div class="redeem-btn">
          <van-button
            type="primary"
            block
            round
            size="large"
            :loading="loading"
            loading-text="兑换中..."
            native-type="submit"
          >
            确认兑换
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- 提示信息 -->
    <div class="tips">
      <div class="tips-title">温馨提示</div>
      <div class="tips-item">1. 后台发放的点卡会显示在上方列表，点击兑换即可</div>
      <div class="tips-item">2. 也可以手动输入卡号和卡密进行兑换</div>
      <div class="tips-item">3. 点卡兑换后余额即时到账</div>
      <div class="tips-item">4. 每张点卡只能使用一次</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import router from '@/router'
import { showToast } from 'vant'
import { useMemberStore } from '@/store/modules/member'
import { redeemCard, getMyCards } from '@/api/member'
import type { MyCard } from '@/types'

const memberStore = useMemberStore()

const form = reactive({
  cardNo: '',
  cardPassword: '',
})

const loading = ref(false)
const redeemingId = ref<number | null>(null)
const myCards = ref<MyCard[]>([])

/** 加载我的点卡列表 */
const loadMyCards = async (): Promise<void> => {
  try {
    myCards.value = await getMyCards()
  } catch (error) {
    console.error('加载点卡列表失败:', error)
  }
}

/** 一键兑换 */
const quickRedeem = async (card: MyCard): Promise<void> => {
  redeemingId.value = card.id
  try {
    await redeemCard({
      cardNo: card.cardNo,
      cardPassword: card.cardPassword,
    })
    showToast('兑换成功')
    await memberStore.refreshInfo()
    await loadMyCards()
    router.replace('/balance')
  } catch (error) {
    console.error('兑换失败:', error)
  } finally {
    redeemingId.value = null
  }
}

/** 手动兑换 */
const handleRedeem = async (): Promise<void> => {
  loading.value = true
  try {
    await redeemCard({
      cardNo: form.cardNo,
      cardPassword: form.cardPassword,
    })
    showToast('兑换成功')
    await memberStore.refreshInfo()
    await loadMyCards()
    router.replace('/balance')
  } catch (error) {
    console.error('兑换失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  memberStore.refreshInfo()
  loadMyCards()
})
</script>

<style scoped>
.recharge-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.balance-card {
  background: linear-gradient(135deg, #ff6034, #ff8a65, #ffab91);
  padding: 30px 20px;
  text-align: center;
  color: #fff;
  margin: -20px 16px 0;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(255, 96, 52, 0.2);
}

.balance-label {
  font-size: 14px;
  opacity: 0.9;
}

.balance-value {
  font-size: 36px;
  font-weight: 700;
  margin-top: 8px;
}

.section-title {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  padding: 16px 16px 8px;
}

.my-cards-section {
  margin-top: 12px;
}

.card-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 16px 8px;
  padding: 16px;
  background: var(--color-bg-card);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-divider);
}

.card-info {
  flex: 1;
}

.card-amount {
  font-size: 20px;
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  text-shadow: 0 1px 2px rgba(255, 96, 52, 0.1);
}

.card-no {
  font-size: var(--font-size-body-sm);
  color: var(--color-text-secondary);
  margin-top: 4px;
  font-family: monospace;
}

.card-time {
  font-size: var(--font-size-caption);
  color: var(--color-text-disabled);
  margin-top: 2px;
}

.manual-section {
  margin-top: 12px;
}

.redeem-form {
  margin-top: 0;
}

.redeem-form :deep(.van-cell-group--inset) {
  box-shadow: var(--shadow-sm);
}

.redeem-btn {
  padding: 20px 16px;
}

.tips {
  margin: 0 16px 20px;
  padding: 16px;
  background: #fff9f5;
  border-left: 3px solid var(--color-primary);
  border-radius: var(--radius-card);
}

.tips-title {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.tips-item {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
  line-height: 1.8;
}
</style>

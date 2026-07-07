<template>
  <div class="pay-page">
    <van-nav-bar title="收银台" left-arrow @click-left="router.back()" />

    <template v-if="order">
      <!-- 支付金额 -->
      <div class="amount-block">
        <div class="amount-label">支付金额</div>
        <div class="amount-value">¥{{ order.totalAmount.toFixed(2) }}</div>
        <div class="order-no">订单号: {{ order.orderNo }}</div>
      </div>

      <!-- 支付方式 -->
      <van-cell-group inset class="pay-methods">
        <div class="methods-title">选择支付方式</div>
        <van-radio-group v-model="payMethod">
          <!-- 微信支付 — 功能开发中，置灰 -->
          <van-cell class="pay-cell-disabled" :border="true">
            <template #icon>
              <van-icon name="wechat-pay" class="pay-icon wechat" />
            </template>
            <template #title>
              <span class="pay-name">微信支付</span>
              <span class="dev-tag">功能开发中</span>
            </template>
            <template #right-icon>
              <van-radio name="wechat" disabled />
            </template>
          </van-cell>
          <!-- 支付宝 — 功能开发中，置灰 -->
          <van-cell class="pay-cell-disabled" :border="true">
            <template #icon>
              <van-icon name="alipay" class="pay-icon alipay" />
            </template>
            <template #title>
              <span class="pay-name">支付宝</span>
              <span class="dev-tag">功能开发中</span>
            </template>
            <template #right-icon>
              <van-radio name="alipay" disabled />
            </template>
          </van-cell>
          <!-- 余额支付（仅已登录会员显示） -->
          <van-cell
            v-if="memberStore.isLoggedIn"
            clickable
            :border="true"
            @click="handleSelectBalance"
          >
            <template #icon>
              <van-icon name="balance-pay" class="pay-icon balance" />
            </template>
            <template #title>
              <span class="pay-name">余额支付</span>
              <span class="pay-balance">余额 ¥{{ memberStore.balance.toFixed(2) }}</span>
            </template>
            <template #right-icon>
              <van-radio name="balance" :disabled="balanceInsufficient" />
            </template>
          </van-cell>
        </van-radio-group>
        <div class="balance-tip" v-if="memberStore.isLoggedIn && balanceInsufficient">
          余额不足，无法使用余额支付
        </div>
      </van-cell-group>

      <!-- 确认支付按钮 -->
      <div class="pay-button-bar">
        <van-button
          type="primary"
          block
          round
          size="large"
          :loading="paying"
          :disabled="!hasAvailableMethod || (payMethod === 'balance' && balanceInsufficient)"
          loading-text="支付中..."
          @click="handlePay"
        >
          {{ hasAvailableMethod ? `确认支付 ¥${order.totalAmount.toFixed(2)}` : '请先登录' }}
        </van-button>
      </div>
    </template>

    <!-- 支付成功遮罩 -->
    <div class="pay-success-overlay" v-if="paySuccess">
      <div class="success-icon-wrapper">
        <van-icon name="checked" class="success-icon" />
      </div>
      <div class="success-text">支付成功</div>
    </div>

    <!-- 加载中 -->
    <van-loading v-if="!order && !paySuccess" class="page-loading" type="spinner" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import router from '@/router'
import { showToast } from 'vant'
import { getOrderDetail, payOrder, payByBalance } from '@/api/order'
import { useMemberStore } from '@/store/modules/member'
import type { Order } from '@/types'

const route = useRoute()
const memberStore = useMemberStore()

const order = ref<Order | null>(null)
const payMethod = ref(memberStore.isLoggedIn ? 'balance' : 'wechat')
const paying = ref(false)
const paySuccess = ref(false)

/** 余额是否不足 */
const balanceInsufficient = computed((): boolean => {
  if (!order.value || !memberStore.isLoggedIn) return true
  return memberStore.balance < order.value.totalAmount
})

/** 加载订单详情 */
const loadOrder = async (): Promise<void> => {
  const id = Number(route.params.id)
  if (!id) return
  try {
    order.value = await getOrderDetail(id)
  } catch (error) {
    console.error('加载订单失败:', error)
  }
}

/** 选择余额支付时检查余额 */
const handleSelectBalance = (): void => {
  if (balanceInsufficient.value) {
    showToast('余额不足')
    return
  }
  payMethod.value = 'balance'
}

/** 是否有可用的支付方式 */
const hasAvailableMethod = computed((): boolean => {
  if (memberStore.isLoggedIn) return true
  return false
})

/** 确认支付 */
const handlePay = async (): Promise<void> => {
  if (!order.value) return

  if (payMethod.value === 'wechat' || payMethod.value === 'alipay') {
    showToast('该支付方式功能开发中')
    return
  }

  const orderId = order.value.id
  paying.value = true
  try {
    if (payMethod.value === 'balance') {
      // 余额支付
      await payByBalance(orderId)
      // 刷新会员余额
      await memberStore.refreshInfo()
    } else {
      // 微信/支付宝支付
      const methodCode = payMethod.value === 'wechat' ? 1 : 2
      await payOrder(orderId, methodCode)
    }

    // 显示支付成功动画
    paying.value = false
    paySuccess.value = true

    // 延迟跳转到订单详情
    setTimeout(() => {
      showToast('支付成功')
      router.replace(`/order/${orderId}`)
    }, 1500)
  } catch (error) {
    console.error('支付失败:', error)
    paying.value = false
  }
}

onMounted(() => {
  loadOrder()
  if (memberStore.isLoggedIn) {
    memberStore.refreshInfo()
  }
})
</script>

<style scoped>
.pay-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.amount-block {
  background: linear-gradient(135deg, #ff6034 0%, #ff8a65 60%, #ffab91 100%);
  padding: 36px 16px 28px;
  text-align: center;
  color: #fff;
}

.amount-label {
  font-size: 14px;
  opacity: 0.9;
}

.amount-value {
  font-size: var(--font-size-display);
  font-weight: var(--font-weight-bold);
  margin-top: 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-no {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 8px;
}

.pay-methods {
  margin-top: -16px;
  border-radius: 16px 16px 0 0;
  background: var(--color-bg-page);
}

.pay-methods :deep(.van-cell-group--inset) {
  box-shadow: var(--shadow-sm);
}

.methods-title {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  padding: 12px 16px 4px;
}

.pay-icon {
  font-size: 24px;
  margin-right: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
}

.pay-icon.wechat {
  color: #07c160;
}

.pay-icon.alipay {
  color: #1677ff;
}

.pay-icon.balance {
  color: var(--color-primary);
}

.pay-name {
  font-size: var(--font-size-h3);
  color: var(--color-text-primary);
}

/* 置灰的支付选项 */
.pay-cell-disabled {
  opacity: 0.5;
  pointer-events: none;
}

.pay-cell-disabled .pay-name {
  color: var(--color-text-secondary);
}

/* 功能开发中标签 */
.dev-tag {
  display: inline-block;
  font-size: 10px;
  color: #fff;
  background: linear-gradient(135deg, #d3d4d6, #c8c9cc);
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: 8px;
  vertical-align: middle;
}

.pay-balance {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
  margin-left: 8px;
}

.balance-tip {
  font-size: 12px;
  color: #ee0a24;
  padding: 4px 16px 8px;
}

.pay-button-bar {
  padding: 24px 16px;
}

/* 支付成功遮罩 */
.pay-success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 999;
  animation: fadeIn 0.3s ease;
}

.success-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #67c23a;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: scaleIn 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.success-icon {
  font-size: 48px;
  color: #fff;
}

.success-text {
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-semi);
  color: var(--color-text-primary);
  margin-top: 16px;
  animation: fadeIn 0.5s ease 0.3s both;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
</style>

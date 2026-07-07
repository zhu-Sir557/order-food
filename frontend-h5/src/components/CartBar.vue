<template>
  <div class="cart-bar">
    <div class="cart-bar-left" @click="goCart">
      <div class="cart-icon-wrapper">
        <van-icon name="shopping-cart-o" class="cart-icon" />
        <van-badge v-if="totalCount > 0" :content="totalCount" class="cart-badge" />
      </div>
      <div class="cart-info">
        <span class="cart-amount">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
    </div>
    <div
      class="cart-bar-right"
      :class="{ disabled: totalCount === 0 }"
      @click="goCart"
    >
      去结算
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import router from '@/router'
import { useCartStore } from '@/store/modules/cart'

const cartStore = useCartStore()

const totalCount = computed(() => cartStore.totalCount)
const totalAmount = computed(() => cartStore.totalAmount)

const goCart = (): void => {
  if (totalCount.value > 0) {
    router.push('/cart')
  }
}
</script>

<style scoped>
.cart-bar {
  position: fixed;
  bottom: 56px;
  left: 0;
  right: 0;
  height: 50px;
  background: linear-gradient(135deg, #3a3a3a, #2a2a2a);
  border-radius: 28px 28px 0 0;
  box-shadow: var(--shadow-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 100;
}

.cart-bar-left {
  display: flex;
  align-items: center;
  flex: 1;
  padding-left: 12px;
  cursor: pointer;
}

.cart-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: rgba(255, 96, 52, 0.15);
  border-radius: 50%;
}

.cart-icon {
  font-size: 24px;
  color: #fff;
}

.cart-info {
  margin-left: 8px;
}

.cart-amount {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.cart-bar-right {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  background: linear-gradient(135deg, #ff6034, #ff8a65);
  border-radius: 28px 0 0 0;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.cart-bar-right.disabled {
  background: linear-gradient(135deg, #999, #888);
}

.cart-badge {
  animation: badgeBounce 0.3s ease;
}

@keyframes badgeBounce {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}
</style>

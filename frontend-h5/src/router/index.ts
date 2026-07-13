import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/menu',
    name: 'Menu',
    component: () => import('@/views/menu/index.vue'),
    meta: { title: '菜单' },
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/cart/index.vue'),
    meta: { title: '购物车' },
  },
  {
    path: '/confirm',
    name: 'Confirm',
    component: () => import('@/views/confirm/index.vue'),
    meta: { title: '确认订单' },
  },
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('@/views/order/list.vue'),
    meta: { title: '我的订单' },
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/detail.vue'),
    meta: { title: '订单详情' },
  },
  {
    path: '/pay/:id',
    name: 'Pay',
    component: () => import('@/views/pay/index.vue'),
    meta: { title: '支付' },
  },
  {
    path: '/me',
    name: 'Me',
    component: () => import('@/views/me/index.vue'),
    meta: { title: '我的' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/bind-phone',
    name: 'BindPhone',
    component: () => import('@/views/bind-phone/index.vue'),
    meta: { title: '绑定手机' },
  },
  {
    path: '/set-password',
    name: 'SetPassword',
    component: () => import('@/views/set-password/index.vue'),
    meta: { title: '设置密码' },
  },
  {
    path: '/balance',
    name: 'Balance',
    component: () => import('@/views/balance/index.vue'),
    meta: { title: '余额记录' },
  },
  {
    path: '/recharge',
    name: 'Recharge',
    component: () => import('@/views/recharge/index.vue'),
    meta: { title: '兑换点卡' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  const title = (to.meta?.title as string) || '在线点餐'
  document.title = title
})

export default router

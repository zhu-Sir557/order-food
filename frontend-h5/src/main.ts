import { createApp } from 'vue'
import App from './App.vue'
import { store } from './store'
import router from './router'
import { useUserStore } from './store/modules/user'
import 'vant/lib/index.css'
import './styles/index.css'

const app = createApp(App)

app.use(store)
app.use(router)

// 恢复用户会话，如果没有token则自动创建临时用户，完成后再挂载应用
const userStore = useUserStore()
userStore.restore().then(() => {
  app.mount('#app')
})

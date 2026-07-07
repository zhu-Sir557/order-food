# 点餐系统 UI 优化 — 文件级任务分解

> **输入文档**: `deliverables/ui-optimization-design.md`  
> **覆盖范围**: H5 用户端 (frontend-h5) + Admin 管理端 (frontend-admin)  
> **约束**: 仅改 CSS 和模板结构，不改功能逻辑  
> **日期**: 2025-07

---

## 一、文件列表及修改概要（按实施顺序排列）

### P0 — 全局基础（必须最先完成）

| 序号 | 文件路径 | 修改类型 | 具体修改内容 | 优先级 |
|------|---------|---------|-------------|--------|
| 1 | `frontend-h5/src/styles/index.css` | 全局CSS | ① 在 `:root` 中新增全部色彩变量（`--color-primary: #ff6034` 等 15 个色值）；新增间距变量（`--space-page-x: 16px` 等 7 个）；新增圆角变量（`--radius-card: 12px` 等 6 个）；新增阴影变量（`--shadow-sm` / `--shadow-md` / `--shadow-lg` / `--shadow-primary` / `--shadow-top`）；新增字体变量（`--font-size-display` 至 `--font-size-caption-mini` + `--font-weight-*` + `--line-height-*`）② `body` 背景 `#f5f5f5` → `var(--color-bg-page)` ③ `.page-container` 背景 `#f5f5f5` → `var(--color-bg-page)` ④ `.section-title` color `#323233` → `var(--color-text-primary)`；font-size `16px` → `var(--font-size-h2)` 即 `17px` ⑤ `.van-dialog__header/.van-dialog__title` color `#323233` → `var(--color-text-primary)` ⑥ 新增 `html { scroll-behavior: smooth; -webkit-overflow-scrolling: touch; }` ⑦ 新增空状态变量 `--van-empty-image-size: 120px` 等 ⑧ 新增 `--van-tabbar-height: 56px; --van-tabbar-item-text-font-size: 11px;` ⑨ 新增 `--van-pull-refresh-head-text-color: var(--color-primary)` | P0 |
| 2 | `frontend-admin/src/styles/index.css` | 全局CSS | ① 在 `:root` 中新增 Element Plus 主题覆盖：`--el-color-primary: #ff6034` 及 9 级渐变（`-light-3` 至 `-light-9` + `-dark-2`）② 新增语义色：`--el-color-success: #07c160; --el-color-warning: #ff9f2c; --el-color-danger: #ee0a24` ③ 新增中性色：`--color-text-primary: #1a1a1a; --color-text-regular: #4a4a4a; --color-text-secondary: #8a8a8a` ④ 新增背景色：`--color-bg-page: #f5f6fa; --color-bg-card: #ffffff; --color-sidebar-bg: #2b2d3a; --color-sidebar-logo: #232530` ⑤ 新增间距变量（`--space-page-x: 24px` 等 5 个）；阴影变量（`--shadow-sm/md/lg/card-hover`）；边框变量（`--color-border: #ebebed; --color-divider: #f2f3f5`）⑥ `body` color `#303133` → `var(--color-text-primary)`；background-color `#f5f7fa` → `#f5f6fa` ⑦ `.el-main` padding `20px` → `24px` ⑧ 滚动条 thumb `#c0c4cc` → `rgba(255,96,52,0.3)`；hover → `rgba(255,96,52,0.5)` ⑨ 新增空状态样式 `.el-empty { --el-empty-padding: 40px 0 } .el-empty__description { color: var(--color-text-secondary); font-size: 14px }` | P0 |
| 3 | `frontend-h5/src/components/DishCard.vue` | 组件样式 + 模板 | ① **模板**: `van-image` width/height `80` → `88`；radius `8` → `10` ② **样式**: `.dish-card` border-radius `10px` → `12px`；box-shadow `0 1px 4px rgba(0,0,0,0.04)` → `var(--shadow-sm)` ③ `.dish-card:active` box-shadow → `var(--shadow-md)` ④ `.dish-name` color `#323233` → `var(--color-text-primary)` ⑤ `.dish-desc` font-size `12px` → `13px`；color `#969799` → `var(--color-text-secondary)` ⑥ `.taste-hint` border-radius `8px` → `6px` ⑦ `.add-btn` 从 `padding: 2px` 改为 `width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; box-shadow: var(--shadow-primary)`；新增 `transition: transform 0.15s ease`；`:active { transform: scale(0.85) }` | P0 |
| 4 | `frontend-h5/src/views/home/index.vue` | 页面样式 | ① NavBar 优化：添加 `--van-nav-bar-background: linear-gradient(135deg, #ff6034, #ff8a65)` 覆盖（在 scoped style 中通过 `:deep(.van-nav-bar)` 或全局变量）；添加 `box-shadow: 0 2px 8px rgba(255,96,52,0.15)`；标题字号 `18px / 700` ② `.home-swipe` height `160px` → `180px`；添加 `margin: -20px 16px 0`；`.swipe-img` height `160px` → `180px`；添加 `border-radius: 12px` ③ `.category-icon-wrap` width/height `52px` → `56px`；border-radius `16px` → `18px` ④ `.category-label` font-size `12px` → `13px`；margin-top `6px` → `8px`；添加 `transition: transform 0.2s ease`；新增 `.category-card:active .category-icon-wrap { transform: scale(0.92) }` ⑤ `.hot-section` 添加 `border-radius: 12px; margin: 10px 12px 0; overflow: hidden` ⑥ `.section-title` 添加 `border-left: 3px solid #ff6034; padding-left: 10px` ⑦ 颜色替换：`#f5f5f5` → `var(--color-bg-page)`；`#323233` → `var(--color-text-primary)`；`#969799` → `var(--color-text-secondary)` | P0 |

### P1 — 核心页面与布局

| 序号 | 文件路径 | 修改类型 | 具体修改内容 | 优先级 |
|------|---------|---------|-------------|--------|
| 5 | `frontend-h5/src/components/CartBar.vue` | 组件样式 | ① `.cart-bar` background `#333` → `linear-gradient(135deg, #3a3a3a, #2a2a2a)`；添加 `border-radius: 28px 28px 0 0`；添加 `box-shadow: var(--shadow-lg)` ② `.cart-bar-right` background `#ff6034` → `linear-gradient(135deg, #ff6034, #ff8a65)`；添加 `border-radius: 28px 0 0 0` ③ `.cart-icon-wrapper` width/height `40px` → `44px`；添加 `background: rgba(255,96,52,0.15); border-radius: 50%` ④ `.cart-badge` 添加 `animation: badgeBounce 0.3s ease` + `@keyframes badgeBounce` | P1 |
| 6 | `frontend-h5/src/components/OrderCard.vue` | 组件样式 | ① `.order-card` border-radius `8px` → `12px`；添加 `box-shadow: var(--shadow-sm)`；padding `12px` → `14px 16px`；添加 `transition: box-shadow 0.2s ease` ② `.order-card:active` 添加 `box-shadow: var(--shadow-md)` ③ `.order-status` 添加 `background: var(--color-primary-bg); padding: 2px 8px; border-radius: 6px; font-size: 12px` ④ 颜色替换：`#969799` → `var(--color-text-secondary)`；`#323233` → `var(--color-text-primary)`；`#c8c9cc` → `var(--color-text-disabled)` | P1 |
| 7 | `frontend-h5/src/views/menu/index.vue` | 页面样式 | ① `van-search` 添加 `background: var(--color-bg-page)` 融合；搜索框内 `box-shadow: var(--shadow-sm)` ② `.menu-sidebar` background `#f7f8fa` → `#f0f1f3` ③ `.sidebar-item` padding `16px 8px` → `14px 10px` ④ `.active-bar` width `3px` → `4px`；height `20px` → `24px` ⑤ 选中项添加 `box-shadow: 0 2px 8px rgba(0,0,0,0.04)` ⑥ `.category-title` 添加 `&::before { content: ''; display: inline-block; width: 3px; height: 14px; background: #ff6034; border-radius: 2px; margin-right: 6px; vertical-align: middle }` ⑦ 颜色替换：`#f5f5f5` → `var(--color-bg-page)`；`#323233` → `var(--color-text-primary)` | P1 |
| 8 | `frontend-h5/src/views/cart/index.vue` | 页面样式 | ① `.cart-items` 添加 `margin: 10px 12px; border-radius: 12px; overflow: hidden; box-shadow: var(--shadow-sm)` ② `.cart-item` padding `12px` → `14px 16px` ③ `.item-name` font-size `14px` → `15px` ④ `.table-cell` 添加 `margin: 10px 12px; border-radius: 12px; overflow: hidden; box-shadow: var(--shadow-sm)` ⑤ `.cart-footer` 添加 `border-radius: 16px 16px 0 0`；box-shadow `0 -2px 8px rgba(0,0,0,0.06)` → `var(--shadow-top)` ⑥ `.checkout-btn` width `120px` → `140px` ⑦ 颜色替换：`#f5f5f5` → `var(--color-bg-page)`；`#323233` → `var(--color-text-primary)` | P1 |
| 9 | `frontend-h5/src/views/confirm/index.vue` | 页面样式 | ① `van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px`；margin-top `8px` → `10px` ② `.order-item` padding `12px 16px` → `14px 16px` ③ `van-image` radius `6` → `8` ④ `.item-taste` border-radius `8px` → `6px` ⑤ `.amount-section` 添加 `margin: 10px 12px; border-radius: 12px; box-shadow: var(--shadow-sm)` ⑥ `.amount-value` font-size `22px` → `24px` ⑦ `.submit-bar` 添加 `border-radius: 16px 16px 0 0`；box-shadow → `var(--shadow-top)` ⑧ 颜色替换：`#f5f5f5` → `var(--color-bg-page)`；`#323233` → `var(--color-text-primary)`；`#969799` → `var(--color-text-secondary)` | P1 |
| 10 | `frontend-h5/src/views/pay/index.vue` | 页面样式 | ① `.amount-block` gradient 从 `linear-gradient(135deg, #ff6034, #ff8a65)` → `linear-gradient(135deg, #ff6034 0%, #ff8a65 60%, #ffab91 100%)`；padding `32px 16px` → `36px 16px 28px` ② `.amount-value` 添加 `text-shadow: 0 2px 8px rgba(0,0,0,0.1)` ③ `.pay-methods` margin-top `12px` → `-16px`；添加 `border-radius: 16px 16px 0 0; background: var(--color-bg-page)` ④ `.pay-methods` 内 `van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm)` ⑤ `.pay-icon` 添加 `width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #f7f8fa` ⑥ `.dev-tag` background `#c8c9cc` → `linear-gradient(135deg, #d3d4d6, #c8c9cc)`；padding `1px 6px` → `2px 8px`；border-radius `8px` → `4px`；font-size `10px` 保持 ⑦ 颜色替换：`#f5f5f5` → `var(--color-bg-page)`；`#323233` → `var(--color-text-primary)`；`#969799` → `var(--color-text-secondary)`；`#c8c9cc` → `var(--color-text-disabled)` | P1 |
| 11 | `frontend-h5/src/views/me/index.vue` | 页面样式 | ① `.user-card` gradient → `linear-gradient(135deg, #ff6034 0%, #ff8a65 50%, #ffab91 100%)`；padding `24px 16px` → `28px 20px`；添加 `margin-bottom: -20px` ② `.avatar` 添加 `width: 64px; height: 64px; border-radius: 50%; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; border: 2px solid rgba(255,255,255,0.3)` ③ `.menu-group` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px`；margin-top `10px` → `12px` ④ 余额信息添加胶囊样式 `background: rgba(255,255,255,0.2); padding: 2px 10px; border-radius: 12px; display: inline-block; margin-top: 6px` ⑤ 颜色替换：`#f5f5f5` → `var(--color-bg-page)` | P1 |
| 12 | `frontend-h5/src/views/balance/index.vue` | 页面样式 | ① `.balance-card` gradient → `linear-gradient(135deg, #ff6034, #ff8a65, #ffab91)`；添加 `margin: -20px 16px 0; border-radius: 16px; box-shadow: 0 8px 24px rgba(255,96,52,0.2)` ② `.record-group` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px` ③ `.record-amount` font-size `16px` → `17px` ④ `.load-more` 改为 `padding: 12px; text-align: center; color: var(--color-primary); font-size: 14px; cursor: pointer`；添加 `&:active { opacity: 0.7 }` ⑤ 颜色替换：`#f5f5f5` → `var(--color-bg-page)`；`#323233` → `var(--color-text-primary)`；`#969799` → `var(--color-text-secondary)` | P1 |
| 13 | `frontend-h5/src/views/recharge/index.vue` | 页面样式 | ① 余额卡片统一：与余额页相同 `margin: -20px 16px 0; border-radius: 16px; box-shadow: 0 8px 24px rgba(255,96,52,0.2)`；3 段渐变 ② `.card-item` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px; border: 1px solid var(--color-divider)` ③ `.card-amount` 添加 `text-shadow: 0 1px 2px rgba(255,96,52,0.1)` ④ `.tips` 添加 `border-left: 3px solid #ff6034; background: #fff9f5`；border-radius `8px` → `12px` ⑤ `van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm)` ⑥ 颜色替换：`#f5f5f5` → `var(--color-bg-page)`；`#323233` → `var(--color-text-primary)`；`#969799` → `var(--color-text-secondary)`；`#c8c9cc` → `var(--color-text-disabled)` | P1 |
| 14 | `frontend-h5/src/views/order/list.vue` | 页面样式 | ① `van-tabs` 添加 `--van-tabs-nav-background: var(--color-bg-page)`；active tab 添加 `font-weight: 700` ② `van-empty` 添加 `image-size="120"`；描述文字 color → `var(--color-text-secondary); font-size: 14px`；描述改为 "暂无订单记录" ③ 颜色替换：`#f5f5f5` → `var(--color-bg-page)` | P1 |
| 15 | `frontend-h5/src/views/order/detail.vue` | 页面样式 | ① `.status-banner` 改为渐变 `background: linear-gradient(135deg, var(--status-color), var(--status-color-light))`；padding `24px 16px` → `28px 20px`；添加 `margin-bottom: -16px` ② `van-steps` 添加 `--van-step-finish-line-color: #ffab91; --van-step-finish-text-color: #ff6034` ③ `.detail-section` 的 `van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px` ④ `.action-bar` 添加 `border-radius: 16px 16px 0 0`；box-shadow → `var(--shadow-top)` ⑤ 颜色替换：`#f5f5f5` → `var(--color-bg-page)`；`#323233` → `var(--color-text-primary)`；`#969799` → `var(--color-text-secondary)` | P1 |
| 16 | `frontend-admin/src/layout/Sidebar.vue` | 组件样式 + 模板 | ① **模板**: `background-color="#304156"` → `"#2b2d3a"`；`text-color="#bfcbd9"` → `"#a0a3b1"`；`active-text-color="#409EFF"` → `"#ff6034"` ② **样式**: `.sidebar` background-color `#304156` → `#2b2d3a` ③ `.logo` background-color `#2b3a4d` → `#232530`；添加 `border-bottom: 1px solid rgba(255,255,255,0.05)` ④ `.logo-text` 添加 `letter-spacing: 0.5px` ⑤ 选中菜单项添加左侧高亮竖线 `:deep(.el-menu-item.is-active) { box-shadow: inset 3px 0 0 #ff6034 }` | P1 |
| 17 | `frontend-admin/src/layout/Header.vue` | 组件样式 | ① `.header` border-bottom `1px solid #e6e6e6` → `none`；改为 `box-shadow: 0 1px 4px rgba(0,0,0,0.04)`（替换原有 box-shadow 值） ② `.admin-info` 添加 `padding: 6px 12px; border-radius: 8px; transition: background 0.2s`；新增 `&:hover { background: var(--color-bg-page) }` ③ `.admin-name` color `#303133` → `var(--color-text-primary)` ④ `el-avatar` 添加 `border: 2px solid var(--el-color-primary-light-7)`（通过 `:deep(.el-avatar)` 实现） | P1 |
| 18 | `frontend-admin/src/views/login/index.vue` | 页面样式 + 模板 | ① **模板**: `el-icon` color `#409EFF` → `#ff6034`；size `36` → `40` ② **样式**: `.login-container` background `linear-gradient(135deg, #667eea 0%, #764ba2 100%)` → `linear-gradient(135deg, #ff6034 0%, #ff8a65 50%, #ffab91 100%)` ③ `.login-card` border-radius `12px` → `16px`；添加 `box-shadow: 0 16px 48px rgba(0,0,0,0.12)` ④ `.login-btn` 添加 `box-shadow: 0 4px 12px rgba(255,96,52,0.3); border-radius: 8px` ⑤ `.login-header h2` color `#303133` → `var(--color-text-primary)` ⑥ `.login-header p` color `#909399` → `var(--color-text-secondary)` | P1 |
| 19 | `frontend-admin/src/views/dashboard/index.vue` | 页面样式 + 模板 + Script | ① **模板**: 4 个 `.stat-icon` 内联 style 背景色分别改为 `#ff6034` / `#07c160` / `#ff9f2c` / `#7c4dff`（替换 `#409EFF` / `#67C23A` / `#E6A23C` / `#F56C6C`）② **Script**: `chartOption` 中 `itemStyle: { color: '#409EFF' }` → `{ color: '#ff6034' }`；`colorStops` 中 `rgba(64,158,255,0.3)` → `rgba(255,96,52,0.25)`；`rgba(64,158,255,0.01)` → `rgba(255,96,52,0.01)` ③ **样式**: `.stat-card` border-radius `8px` → `12px`；添加 `transition: transform 0.2s, box-shadow 0.2s`；新增 `&:hover { transform: translateY(-2px); box-shadow: var(--shadow-card-hover) }` ④ `.stat-icon` border-radius `8px` → `14px`；width/height `56px` → `60px` ⑤ `.stat-label` color `#909399` → `var(--color-text-secondary)` ⑥ `.stat-value` color `#303133` → `var(--color-text-primary)` ⑦ `el-card` 添加 `border-radius: 12px` | P1 |
| 20 | `frontend-admin/src/views/dish/index.vue` | 页面样式 | ① `.search-card` 添加 `border-radius: 12px` ② `el-table` stripe 行背景 → `#fafafa`（通过 `:deep(.el-table__row:hover > td)` 等）；border 颜色 → `var(--color-border)`；表头添加 `background: #f7f8fa; font-weight: 600; color: var(--color-text-primary)` ③ `el-image` 圆角 `4px` → `8px`；尺寸 `50x50` → `56x56`；`.image-placeholder` 圆角同步 `8px` ④ `.pagination` margin-top `16px` → `20px`；添加 `padding-top: 16px; border-top: 1px solid var(--color-divider)` ⑤ `el-form-item` label color → `var(--color-text-regular)` | P1 |
| 21 | `frontend-admin/src/views/category/index.vue` | 页面样式 | ① `.toolbar` 添加 `padding-bottom: 16px; border-bottom: 1px solid var(--color-divider)` ② `.page-title` color → `var(--color-text-primary)` ③ 上移/下移按钮添加 `padding: 4px 8px` ④ 表格统一：stripe `#fafafa` + 表头样式（同 dish）⑤ `el-tag` 添加 `border-radius: 4px`（通过 `:deep(.el-tag)`） | P1 |
| 22 | `frontend-admin/src/views/table/index.vue` | 页面样式 | ① `el-card` 添加 `border-radius: 12px` ② `.toolbar` 添加 `padding-bottom: 16px; border-bottom: 1px solid var(--color-divider)` ③ `.page-title` color → `var(--color-text-primary)` ④ 表格统一：stripe + 表头（同 dish）⑤ `el-tag` 添加 `border-radius: 4px` ⑥ ⚠️ 检查 `tableStatusType` 返回 `'primary'` 时显示是否正确（全局 primary 已变为 `#ff6034`） | P1 |
| 23 | `frontend-admin/src/views/order/index.vue` | 页面样式 + Script | ① `.search-card` 添加 `border-radius: 12px` ② `.amount` color `#F56C6C` → `var(--color-danger)`；font-weight 保持 `700`（当前为 `600`，改为 `700`） ③ ⚠️ **Script**: `statusTagType` 函数中 status 2（制作中）当前返回 `'primary'`，改为返回 `'warning'` 或自定义处理（因全局 primary 已变橙色，与"制作中"语义混淆）④ 操作按钮"接单""完成""取餐"添加 `size="small" round` ⑤ 表格统一：stripe + 表头（同 dish） ⑥ `.dish-summary` color `#606266` → `var(--color-text-regular)` ⑦ `.pagination` margin-top `16px` → `20px`；添加分割线 | P1 |
| 24 | `frontend-admin/src/views/member/index.vue` | 页面样式 | ① `.member-page` 移除 `padding: 20px`（由 el-main 统一控制）② 余额 ¥ 文字添加 `color: var(--el-color-primary); font-weight: 600` ③ `.expense` color `#f56c6c` → `var(--color-danger)` ④ `.income` color `#67c23a` 保持 ⑤ 余额记录弹窗内表格添加 `border-radius: 8px; overflow: hidden` ⑥ `.search-form` margin-bottom `20px` → `16px` | P1 |

### P2 — 辅助页面、组件细节、动画

| 序号 | 文件路径 | 修改类型 | 具体修改内容 | 优先级 |
|------|---------|---------|-------------|--------|
| 25 | `frontend-h5/src/App.vue` | 模板结构调整 + 全局CSS | ① **模板**: `<router-view />` 包裹 `<transition name="fade"><router-view /></transition>` ② **样式**: padding-bottom `50px` → `56px`（TabBar 高度增加）③ 添加 `.fade-enter-active { transition: opacity 0.2s ease }` 和 `.fade-enter-from { opacity: 0 }` | P2 |
| 26 | `frontend-h5/src/components/TabBar.vue` | 组件样式 | ① inactive-color `#969799` → `var(--color-text-secondary)`（可通过 CSS 变量或属性值）② 添加 `--van-tabbar-height: 56px`（已在全局 CSS 中定义，确认覆盖生效）③ 添加 `--van-tabbar-item-text-font-size: 11px` | P2 |
| 27 | `frontend-h5/src/components/TastePicker.vue` | 组件样式 | ① `.option-chip` border-radius `16px` → `20px`；padding `6px 16px` → `8px 18px` ② `.option-chip.selected` 添加 `box-shadow: 0 2px 8px rgba(255,96,52,0.2)` ③ `.required-badge` border-radius `8px` → `4px` ④ 颜色替换：`#323233` → `var(--color-text-primary)`；`#969799` → `var(--color-text-secondary)` | P2 |
| 28 | `frontend-h5/src/views/login/index.vue` | 页面样式 | ① 背景从 `#f5f5f5` → `linear-gradient(180deg, #fff5f0 0%, #f7f8fa 30%, #f7f8fa 100%)` ② Logo 区域添加 80px 圆角方形渐变容器 `background: linear-gradient(135deg, #fff5f0, #ffe4d7); box-shadow: 0 8px 24px rgba(255,96,52,0.15)` ③ `van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px` ④ `.login-btn` 添加 `box-shadow: var(--shadow-primary)` ⑤ 颜色替换：`#323233` → `var(--color-text-primary)`；`#969799` → `var(--color-text-secondary)` | P2 |
| 29 | `frontend-h5/src/views/register/index.vue` | 页面样式 | ① 背景渐变化（同登录页）② Logo 容器圆角化（同登录页 80px 圆角方形渐变）③ 表单卡片化（`box-shadow + border-radius: 12px`）④ 确认密码字段 `van-field` 添加 `left-icon="lock"` ⑤ 颜色替换：`#f5f5f5` → `var(--color-bg-page)`；`#323233` → `var(--color-text-primary)`；`#969799` → `var(--color-text-secondary)` | P2 |
| 30 | `frontend-admin/src/views/banner/index.vue` | 页面样式 | ① `.search-card` 简化为 `display: flex; justify-content: flex-end; padding: 16px 20px; border-radius: 12px` ② `el-image` 圆角 `4px` → `8px`；`.image-placeholder` 圆角同步 ③ `.link-text` color `#409eff` → `var(--el-color-primary)` ④ 表格统一：stripe + 表头（同 dish）⑤ `.pagination` margin-top `16px` → `20px`；添加分割线 ⑥ `.image-placeholder` color `#c0c4cc` 保持 | P2 |
| 31 | `frontend-admin/src/views/card/index.vue` | 页面样式 | ① `.card-page` 移除 `padding: 20px`（由 el-main 控制）② `.card-header` 添加 `font-size: 16px; font-weight: 600; color: var(--color-text-primary)` ③ `.search-form` margin-bottom `20px` → `16px` ④ 表格 + 分页统一（同 dish：stripe、表头、分页 margin-top 20px） | P2 |
| 32 | `frontend-admin/src/views/user/index.vue` | 页面样式 | ① `.toolbar` 添加 `padding-bottom: 16px; border-bottom: 1px solid var(--color-divider)` ② `.page-title` color → `var(--color-text-primary)` ③ `el-avatar` 添加 `background: var(--el-color-primary-light-8)`（通过 `:deep(.el-avatar)` 实现）④ 表格统一：stripe + 表头（同 dish） ⑤ ⚠️ 角色标签：服务员(role=2)当前 `type="primary"` 会显示为橙色，如需区分可改为 `type="success"` | P2 |
| 33 | `frontend-admin/src/components/DishDialog.vue` | 组件样式 | ① `:deep(.el-dialog)` 添加 `border-radius: 12px; overflow: hidden` ② `.image-uploader :deep(.el-upload)` border `1px dashed #d9d9d9` → `1px dashed var(--el-color-primary-light-5)`；border-radius `6px` → `10px` ③ `.image-uploader :deep(.el-upload:hover)` border-color `#409eff` → `var(--el-color-primary)` ④ `.taste-group` background-color `#fafafa` → `var(--el-color-primary-light-9)`；border `1px solid #ebeef5` → `1px solid var(--el-color-primary-light-7)`；border-radius `6px` → `10px` ⑤ `.taste-label` color `#606266` → `var(--color-text-regular)` | P2 |
| 34 | `frontend-admin/src/components/BannerDialog.vue` | 组件样式 | ① `:deep(.el-dialog)` 添加 `border-radius: 12px; overflow: hidden` ② upload hover border-color `#409eff` → `var(--el-color-primary)` ③ upload border-radius → `10px`（与 DishDialog 一致） | P2 |
| 35 | `frontend-admin/src/components/OrderDetailDialog.vue` | 组件样式 | ① `:deep(.el-dialog)` 添加 `border-radius: 12px; overflow: hidden` ② `.amount` color `#F56C6C` → `var(--color-danger)` ③ color `#303133` → `var(--color-text-primary)` | P2 |

---

## 二、实施顺序与依赖关系

### 2.1 依赖关系说明

```
全局CSS变量定义 (序号1,2) ──→ 所有其他文件
    │
    ├──→ H5组件 (序号3,5,6,7,27) ──→ 引用 var() 的页面
    ├──→ H5页面 (序号4,8-15,28,29) ──→ 引用 var() 
    ├──→ Admin布局 (序号16,17) ──→ Admin页面
    ├──→ Admin页面 (序号18-24,30-32) ──→ 引用 var()
    └──→ Admin弹窗 (序号33-35) ──→ 引用 var()
```

**核心规则**:
- **序号 1 和 2（两端全局 CSS 变量）必须最先完成**，所有后续文件都依赖这些变量定义
- 序号 3（DishCard）和 6（OrderCard）应在引用它们的页面之前完成
- 序号 5（CartBar）应在 home 页面之前或同时完成
- Admin 端序号 16-17（布局组件）应在页面组件之前完成

### 2.2 建议批量实施分组

| 批次 | 包含文件 | 说明 | 可并行 |
|------|---------|------|--------|
| **批次 A** (P0 基础) | 序号 1, 2 | 两端全局 CSS 变量定义 | ✅ 两端可并行 |
| **批次 B** (P0 核心) | 序号 3, 4 | H5 DishCard + 首页 | ✅ 两个文件可并行 |
| **批次 C** (P1 H5 组件) | 序号 5, 6 | CartBar + OrderCard | ✅ 可并行 |
| **批次 D** (P1 H5 页面-1) | 序号 7, 8, 9, 10 | menu, cart, confirm, pay | ✅ 四个页面可并行 |
| **批次 E** (P1 H5 页面-2) | 序号 11, 12, 13, 14, 15 | me, balance, recharge, order/list, order/detail | ✅ 五个页面可并行 |
| **批次 F** (P1 Admin 布局) | 序号 16, 17 | Sidebar + Header | ✅ 可并行 |
| **批次 G** (P1 Admin 页面-1) | 序号 18, 19 | login + dashboard | ✅ 可并行 |
| **批次 H** (P1 Admin 页面-2) | 序号 20, 21, 22, 23, 24 | dish, category, table, order, member | ✅ 五个页面可并行 |
| **批次 I** (P2 H5 辅助) | 序号 25, 26, 27, 28, 29 | App, TabBar, TastePicker, login, register | ✅ 可并行 |
| **批次 J** (P2 Admin 辅助) | 序号 30, 31, 32, 33, 34, 35 | banner, card, user, DishDialog, BannerDialog, OrderDetailDialog | ✅ 可并行 |

### 2.3 最小可验证路径

如果希望快速看到效果，建议按以下最小路径执行：

1. **批次 A** → 看到全局色彩变化
2. **批次 B** → 看到 H5 首页菜品卡片变化
3. **批次 C** → 看到购物车栏和订单卡片变化
4. **批次 F + G** → 看到 Admin 端整体换色效果

---

## 三、共享知识

### 3.1 CSS 变量命名规范

| 端 | 变量前缀 | 示例 | 说明 |
|----|---------|------|------|
| H5 端 | `--color-*` / `--space-*` / `--radius-*` / `--shadow-*` / `--font-*` | `--color-primary: #ff6034` | 自定义变量，不与 Vant 冲突 |
| H5 端 | `--van-*` | `--van-primary-color: #ff6034` | Vant 4 组件变量覆盖 |
| Admin 端 | `--el-color-*` | `--el-color-primary: #ff6034` | Element Plus 主题覆盖 |
| Admin 端 | `--color-*` / `--space-*` / `--shadow-*` | `--color-text-primary: #1a1a1a` | 自定义变量，用于页面样式 |

### 3.2 跨文件一致的值

以下值在多个文件中必须保持完全一致：

| 属性 | H5 端统一值 | Admin 端统一值 | 涉及文件 |
|------|-----------|---------------|---------|
| 主色 | `#ff6034` | `#ff6034` | 所有文件 |
| 卡片圆角 | `12px` | `12px` (el-card) / `10px` (设计文档原值，但建议统一 12px) | DishCard, OrderCard, 各 section, 各 el-card |
| 卡片阴影（默认） | `var(--shadow-sm)` 即 `0 1px 3px rgba(0,0,0,0.04)` | `var(--shadow-sm)` 即 `0 1px 2px rgba(0,0,0,0.03)` | 所有卡片容器 |
| 卡片阴影（悬浮） | `var(--shadow-md)` 即 `0 2px 8px rgba(0,0,0,0.06)` | `var(--shadow-md)` 即 `0 2px 12px rgba(0,0,0,0.06)` | `:active` / `:hover` 态 |
| 底部固定栏阴影 | `var(--shadow-top)` 即 `0 -2px 8px rgba(0,0,0,0.04)` | — | cart-footer, submit-bar, action-bar |
| 主色按钮阴影 | `var(--shadow-primary)` 即 `0 4px 12px rgba(255,96,52,0.25)` | `0 4px 12px rgba(255,96,52,0.3)` | login-btn, add-btn |
| 主文字色 | `#1a1a1a` (via `--color-text-primary`) | `#1a1a1a` (via `--color-text-primary`) | 替换所有 `#323233`(H5) / `#303133`(Admin) |
| 次要文字色 | `#8a8a8a` (via `--color-text-secondary`) | `#8a8a8a` (via `--color-text-secondary`) | 替换所有 `#969799` |
| 页面背景 | `#f7f8fa` (via `--color-bg-page`) | `#f5f6fa` (via `--color-bg-page`) | 替换所有 `#f5f5f5`(H5) / `#f5f7fa`(Admin) |
| 成功色 | `#07c160` | `#07c160` | 收入/成功状态 |
| 警告色 | `#ff9f2c` | `#ff9f2c` | 待处理/使用中状态 |
| 危险色 | `#ee0a24` | `#ee0a24` | 支出/删除/已取消 |
| 主色背景 | `#fff5f0` (via `--color-primary-bg`) | `var(--el-color-primary-light-9)` 即 `#fff5f0` | 标签底色、提示区底色 |

### 3.3 需要同步修改的关联文件

| 关联组 | 文件 | 关联原因 |
|-------|------|---------|
| 余额卡片样式 | `balance/index.vue` + `recharge/index.vue` | 必须使用完全相同的渐变 + 圆角 + 阴影 |
| 表格统一样式 | `dish/index.vue` + `category/index.vue` + `table/index.vue` + `order/index.vue` + `banner/index.vue` + `card/index.vue` + `member/index.vue` + `user/index.vue` | stripe 行色、表头样式、border 颜色必须一致 |
| 登录/注册页 | `frontend-h5/login/index.vue` + `frontend-h5/register/index.vue` | 背景渐变、Logo 容器、表单卡片化必须保持一致 |
| Admin 弹窗 | `DishDialog.vue` + `BannerDialog.vue` + `OrderDetailDialog.vue` | `el-dialog` 圆角 `12px` 必须一致 |
| 颜色替换映射 | 所有 H5 页面 | `#f5f5f5` → `var(--color-bg-page)`、`#323233` → `var(--color-text-primary)`、`#969799` → `var(--color-text-secondary)` 必须全量替换 |
| 颜色替换映射 | 所有 Admin 页面 | `#303133` → `var(--color-text-primary)`、`#909399` → `var(--color-text-secondary)`、`#606266` → `var(--color-text-regular)`、`#409EFF`/`#409eff` → `var(--el-color-primary)` 或 `#ff6034` 必须全量替换 |

---

## 四、注意事项

### 4.1 硬编码颜色需要替换的文件清单

#### H5 端硬编码颜色

| 文件 | 硬编码颜色 | 替换为 | 出现位置 |
|------|----------|-------|---------|
| `frontend-h5/src/styles/index.css` | `#f5f5f5` | `var(--color-bg-page)` | body, .page-container |
| `frontend-h5/src/styles/index.css` | `#323233` | `var(--color-text-primary)` | .section-title, .van-dialog__header |
| `frontend-h5/src/views/home/index.vue` | `#f5f5f5` | `var(--color-bg-page)` | .home-page |
| `frontend-h5/src/views/home/index.vue` | `#323233` | `var(--color-text-primary)` | .section-title, .category-label |
| `frontend-h5/src/views/home/index.vue` | `#969799` | `var(--color-text-secondary)` | .section-subtitle |
| `frontend-h5/src/components/DishCard.vue` | `#323233` | `var(--color-text-primary)` | .dish-name |
| `frontend-h5/src/components/DishCard.vue` | `#969799` | `var(--color-text-secondary)` | .dish-desc |
| `frontend-h5/src/components/OrderCard.vue` | `#323233` | `var(--color-text-primary)` | .order-items-summary, .picker-title |
| `frontend-h5/src/components/OrderCard.vue` | `#969799` | `var(--color-text-secondary)` | .order-no, .order-time |
| `frontend-h5/src/components/OrderCard.vue` | `#c8c9cc` | `var(--color-text-disabled)` | .arrow-icon |
| `frontend-h5/src/components/TastePicker.vue` | `#323233` | `var(--color-text-primary)` | .dish-name, .group-name, .option-chip |
| `frontend-h5/src/components/TastePicker.vue` | `#969799` | `var(--color-text-secondary)` | .optional-badge |
| `frontend-h5/src/views/menu/index.vue` | `#f5f5f5` / `#323233` | `var(--color-bg-page)` / `var(--color-text-primary)` | 页面背景、多处文字 |
| `frontend-h5/src/views/cart/index.vue` | `#f5f5f5` / `#323233` | 同上 | 页面背景、多处文字 |
| `frontend-h5/src/views/confirm/index.vue` | `#f5f5f5` / `#323233` / `#969799` | 同上 | 页面背景、多处文字 |
| `frontend-h5/src/views/pay/index.vue` | `#f5f5f5` / `#323233` / `#969799` / `#c8c9cc` | 同上 | 页面背景、多处文字、dev-tag |
| `frontend-h5/src/views/me/index.vue` | `#f5f5f5` | `var(--color-bg-page)` | 页面背景 |
| `frontend-h5/src/views/balance/index.vue` | `#f5f5f5` / `#323233` / `#969799` | 同上 | 页面背景、多处文字 |
| `frontend-h5/src/views/recharge/index.vue` | `#f5f5f5` / `#323233` / `#969799` / `#c8c9cc` | 同上 | 页面背景、多处文字 |
| `frontend-h5/src/views/order/list.vue` | `#f5f5f5` | `var(--color-bg-page)` | 页面背景 |
| `frontend-h5/src/views/order/detail.vue` | `#f5f5f5` / `#323233` / `#969799` | 同上 | 页面背景、多处文字 |
| `frontend-h5/src/views/login/index.vue` | `#f5f5f5` / `#323233` / `#969799` | 同上 | 页面背景、多处文字 |
| `frontend-h5/src/views/register/index.vue` | `#f5f5f5` / `#323233` / `#969799` | 同上 | 页面背景、多处文字 |
| `frontend-h5/src/components/TabBar.vue` | `#969799` | `var(--color-text-secondary)` | inactive-color 属性 |

#### Admin 端硬编码颜色

| 文件 | 硬编码颜色 | 替换为 | 出现位置 |
|------|----------|-------|---------|
| `frontend-admin/src/styles/index.css` | `#303133` / `#f5f7fa` / `#c0c4cc` | `var(--color-text-primary)` / `#f5f6fa` / `rgba(255,96,52,0.3)` | body, 滚动条 |
| `frontend-admin/src/layout/Sidebar.vue` | `#304156` / `#2b3a4d` / `#bfcbd9` / `#409EFF` | `#2b2d3a` / `#232530` / `#a0a3b1` / `#ff6034` | el-menu 属性、CSS |
| `frontend-admin/src/layout/Header.vue` | `#e6e6e6` / `#303133` | `none`(改用 shadow) / `var(--color-text-primary)` | .header, .admin-name |
| `frontend-admin/src/views/login/index.vue` | `#667eea` / `#764ba2` / `#409EFF` / `#303133` / `#909399` | `#ff6034` / `#ff8a65` / `#ff6034` / `var(--color-text-primary)` / `var(--color-text-secondary)` | 背景、图标、文字 |
| `frontend-admin/src/views/dashboard/index.vue` | `#409EFF` / `#67C23A` / `#E6A23C` / `#F56C6C` / `#909399` / `#303133` | `#ff6034` / `#07c160` / `#ff9f2c` / `#7c4dff` / `var(--color-text-secondary)` / `var(--color-text-primary)` | 4 个图标内联 style、chart 配置、文字色 |
| `frontend-admin/src/views/order/index.vue` | `#F56C6C` / `#606266` | `var(--color-danger)` / `var(--color-text-regular)` | .amount, .dish-summary |
| `frontend-admin/src/views/banner/index.vue` | `#409eff` / `#c0c4cc` / `#f5f7fa` | `var(--el-color-primary)` / 保持 / 保持 | .link-text, .image-placeholder |
| `frontend-admin/src/views/member/index.vue` | `#67c23a` / `#f56c6c` | 保持 / `var(--color-danger)` | .income, .expense |
| `frontend-admin/src/components/DishDialog.vue` | `#d9d9d9` / `#409eff` / `#ebeef5` / `#fafafa` / `#606266` | `var(--el-color-primary-light-5)` / `var(--el-color-primary)` / `var(--el-color-primary-light-7)` / `var(--el-color-primary-light-9)` / `var(--color-text-regular)` | upload border, taste-group |
| `frontend-admin/src/components/BannerDialog.vue` | `#409eff` | `var(--el-color-primary)` | upload hover border |
| `frontend-admin/src/components/OrderDetailDialog.vue` | `#F56C6C` / `#303133` | `var(--color-danger)` / `var(--color-text-primary)` | .amount, 文字色 |

### 4.2 Script 部分需要微调的文件

| 文件 | 函数名 | 当前返回值 | 需要改为 | 原因 |
|------|--------|----------|---------|------|
| `frontend-admin/src/views/order/index.vue` | `statusTagType()` | status 2 → `'primary'` | status 2 → `'warning'` 或自定义紫色 tag | 全局 primary 已变为 `#ff6034`（橙色），"制作中"用橙色 tag 会与品牌色混淆。建议改为 `effect="dark"` + 自定义 `color: #7c4dff` 紫色 tag |
| `frontend-admin/src/views/dashboard/index.vue` | `chartOption` (computed) | `itemStyle.color: '#409EFF'` | `'#ff6034'` | 图表折线颜色需跟随主题色 |
| `frontend-admin/src/views/dashboard/index.vue` | `chartOption` (computed) | `colorStops: rgba(64,158,255,0.3)` → `rgba(64,158,255,0.01)` | `rgba(255,96,52,0.25)` → `rgba(255,96,52,0.01)` | 图表面积渐变需跟随主题色 |
| `frontend-admin/src/views/table/index.vue` | `tableStatusType()` | status 2 → `'primary'` | 保持 `'primary'`（无需改动） | "已预约"用橙色 tag 语义合理，验证显示即可 |
| `frontend-admin/src/views/user/index.vue` | 角色标签 (template) | role 2 → `type="primary"` | 可改为 `type="success"` 或保持 | 服务员标签会变为橙色，如需与管理员(红色)区分可改 |

### 4.3 可能影响现有功能的修改（需特别注意）

| 风险点 | 文件 | 具体风险 | 建议 |
|--------|------|---------|------|
| **NavBar 渐变覆盖** | `frontend-h5/src/views/home/index.vue` | Vant `van-nav-bar` 的背景色由 `--van-nav-bar-background` 控制，当前全局已设为 `#ff6034`，改为渐变需确认覆盖方式（`:deep` 或全局变量） | 建议在 `frontend-h5/src/styles/index.css` 的 `:root` 中将 `--van-nav-bar-background` 改为 `linear-gradient(135deg, #ff6034, #ff8a65)` |
| **CartBar 底部位置** | `frontend-h5/src/components/CartBar.vue` | `.cart-bar` 添加 `border-radius` 后，底部圆角可能与 TabBar 重叠区域产生视觉异常 | 确认 `bottom: 50px` 是否需同步调整为 `56px`（与 TabBar 新高度一致） |
| **pay-methods 负 margin** | `frontend-h5/src/views/pay/index.vue` | `.pay-methods` margin-top 改为 `-16px` 向上叠浮，需确认与 `.amount-block` 的 padding-bottom（28px）配合后不会遮挡内容 | 测试时确认支付方式列表第一项不被渐变区遮挡 |
| **statusTagType 返回值变更** | `frontend-admin/src/views/order/index.vue` | 修改 `statusTagType` 返回值会影响所有订单状态的标签颜色显示 | 修改后逐一验证 6 种状态的标签显示效果 |
| **Dashboard 内联 style** | `frontend-admin/src/views/dashboard/index.vue` | 4 个 `.stat-icon` 使用内联 `style="background-color: ..."`，需直接修改模板中的硬编码值 | 确认 4 个颜色值正确对应：今日订单`#ff6034`、今日营业额`#07c160`、桌台使用率`#ff9f2c`、总菜品数`#7c4dff` |
| **el-card shadow 属性** | 多个 Admin 页面 | 当前 `shadow="never"`，设计文档建议改为 `shadow="hover"` 或自定义 `--shadow-sm` | 逐一修改 `el-card` 的 `shadow` 属性值 |
| **App.vue 路由过渡** | `frontend-h5/src/App.vue` | 添加 `<transition>` 包裹后，需确认所有路由切换不会出现闪烁或白屏 | 测试首页→菜单→购物车→我的的路由切换 |
| **el-dialog 圆角** | Admin 弹窗组件 | 通过 `:deep(.el-dialog)` 添加圆角 + `overflow: hidden` 可能影响弹窗内部的下拉框等浮动元素 | 测试 DishDialog 的 el-select 下拉、el-date-picker 弹出层是否被裁切 |

### 4.4 不在本次修改范围的内容

以下内容在设计文档中提及但属于交互体验优化（P2），需根据实际情况判断是否纳入：

- **列表项错位淡入动画**（`animation: slideIn` + `animation-delay`）— 需在列表组件的 `v-for` 项上添加 `:style="{ '--i': index }"`，涉及模板结构变更
- **价格变化数字弹跳动画**（`@keyframes priceBounce`）— 需在 JS 中添加 watch 触发动画 class，涉及 script 逻辑
- **全局过渡动画 class**（卡片点击 `scale(0.97)`、按钮点击 `scale(0.95)`）— 可通过全局 CSS 统一添加，但需确认不影响 Vant/Element 自带动画

> **建议**: 以上动画效果可在 P2 批次中作为增强项实施，如果时间紧张可暂不添加。

---

## 五、任务依赖图

```
┌──────────────────────────────────────────────────────────────────┐
│                        批次 A (P0 基础)                           │
│  ┌─────────────────────┐    ┌──────────────────────────┐         │
│  │ #1 H5 index.css     │    │ #2 Admin index.css        │         │
│  │ (全局CSS变量)        │    │ (全局CSS变量+EP主题覆盖)   │         │
│  └──────────┬──────────┘    └─────────────┬────────────┘         │
└─────────────┼─────────────────────────────┼──────────────────────┘
              │                              │
    ┌─────────┴──────────┐         ┌────────┴───────────┐
    │   批次 B (P0 核心)  │         │  批次 F (P1 布局)  │
    │ ┌─────┐  ┌───────┐ │         │ ┌──────┐ ┌───────┐ │
    │ │#3   │  │#4     │ │         │ │#16   │ │#17    │ │
    │ │Dish │  │Home   │ │         │ │Side  │ │Header │ │
    │ │Card │  │Page   │ │         │ │bar   │ │       │ │
    │ └──┬──┘  └───┬───┘ │         │ └──┬───┘ └───┬───┘ │
    └────┼─────────┼─────┘         └────┼─────────┼─────┘
         │         │                    │         │
    ┌────┴─────────┴────┐         ┌─────┴─────────┴─────┐
    │  批次 C (P1 H5)   │         │  批次 G (P1 Admin)  │
    │ ┌──────┐ ┌──────┐│         │ ┌──────┐ ┌────────┐ │
    │ │#5    │ │#6    ││         │ │#18   │ │#19     │ │
    │ │Cart  │ │Order ││         │ │Login │ │Dash    │ │
    │ │Bar   │ │Card  ││         │ │      │ │board   │ │
    │ └──────┘ └──────┘│         │ └──────┘ └────────┘ │
    └──────────────────┘         └─────────────────────┘
    ┌──────────────────┐         ┌─────────────────────┐
    │  批次 D (P1 H5)  │         │  批次 H (P1 Admin)  │
    │ #7 #8 #9 #10     │         │ #20 #21 #22 #23 #24 │
    │ menu cart confirm │         │ dish cat table order│
    │ pay               │         │ member              │
    └──────────────────┘         └─────────────────────┘
    ┌──────────────────┐         ┌─────────────────────┐
    │  批次 E (P1 H5)  │         │  批次 J (P2 Admin)  │
    │ #11 #12 #13      │         │ #30 #31 #32         │
    │ #14 #15          │         │ #33 #34 #35         │
    │ me bal recharge  │         │ banner card user    │
    │ order/list detail│         │ dialogs             │
    └──────────────────┘         └─────────────────────┘
    ┌──────────────────┐
    │  批次 I (P2 H5)  │
    │ #25 #26 #27      │
    │ #28 #29          │
    │ App TabBar Taste  │
    │ login register    │
    └──────────────────┘
```

---

## 六、快速查找索引

### 按文件路径查找

| 文件路径 | 序号 | 批次 | 优先级 |
|---------|------|------|--------|
| `frontend-h5/src/styles/index.css` | #1 | A | P0 |
| `frontend-admin/src/styles/index.css` | #2 | A | P0 |
| `frontend-h5/src/components/DishCard.vue` | #3 | B | P0 |
| `frontend-h5/src/views/home/index.vue` | #4 | B | P0 |
| `frontend-h5/src/components/CartBar.vue` | #5 | C | P1 |
| `frontend-h5/src/components/OrderCard.vue` | #6 | C | P1 |
| `frontend-h5/src/views/menu/index.vue` | #7 | D | P1 |
| `frontend-h5/src/views/cart/index.vue` | #8 | D | P1 |
| `frontend-h5/src/views/confirm/index.vue` | #9 | D | P1 |
| `frontend-h5/src/views/pay/index.vue` | #10 | D | P1 |
| `frontend-h5/src/views/me/index.vue` | #11 | E | P1 |
| `frontend-h5/src/views/balance/index.vue` | #12 | E | P1 |
| `frontend-h5/src/views/recharge/index.vue` | #13 | E | P1 |
| `frontend-h5/src/views/order/list.vue` | #14 | E | P1 |
| `frontend-h5/src/views/order/detail.vue` | #15 | E | P1 |
| `frontend-admin/src/layout/Sidebar.vue` | #16 | F | P1 |
| `frontend-admin/src/layout/Header.vue` | #17 | F | P1 |
| `frontend-admin/src/views/login/index.vue` | #18 | G | P1 |
| `frontend-admin/src/views/dashboard/index.vue` | #19 | G | P1 |
| `frontend-admin/src/views/dish/index.vue` | #20 | H | P1 |
| `frontend-admin/src/views/category/index.vue` | #21 | H | P1 |
| `frontend-admin/src/views/table/index.vue` | #22 | H | P1 |
| `frontend-admin/src/views/order/index.vue` | #23 | H | P1 |
| `frontend-admin/src/views/member/index.vue` | #24 | H | P1 |
| `frontend-h5/src/App.vue` | #25 | I | P2 |
| `frontend-h5/src/components/TabBar.vue` | #26 | I | P2 |
| `frontend-h5/src/components/TastePicker.vue` | #27 | I | P2 |
| `frontend-h5/src/views/login/index.vue` | #28 | I | P2 |
| `frontend-h5/src/views/register/index.vue` | #29 | I | P2 |
| `frontend-admin/src/views/banner/index.vue` | #30 | J | P2 |
| `frontend-admin/src/views/card/index.vue` | #31 | J | P2 |
| `frontend-admin/src/views/user/index.vue` | #32 | J | P2 |
| `frontend-admin/src/components/DishDialog.vue` | #33 | J | P2 |
| `frontend-admin/src/components/BannerDialog.vue` | #34 | J | P2 |
| `frontend-admin/src/components/OrderDetailDialog.vue` | #35 | J | P2 |

### 按修改类型统计

| 修改类型 | 文件数量 |
|---------|---------|
| 全局CSS | 2 |
| 组件样式 | 12 |
| 页面样式 | 17 |
| 模板结构调整 | 4 |
| Script微调 | 3 |
| **总计** | **35** |

> **注**: 部分文件同时涉及多种修改类型，此处按主要类型统计。

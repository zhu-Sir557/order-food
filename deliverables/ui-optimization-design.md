# 点餐系统 UI 优化设计方向文档

> **项目路径**: `D:\source\workbuddy\repo1\order_food`  
> **覆盖范围**: H5 用户端 (frontend-h5) + Admin 管理端 (frontend-admin)  
> **约束**: 仅改 CSS 和模板结构，不改功能逻辑  
> **日期**: 2025-07

---

## 一、整体风格定位

**一句话概括**: 温暖活力的暖橙色系 + 现代简洁的卡片化设计，H5 端追求食欲感与流畅感，Admin 端追求专业品质与高效信息密度。

**设计原则**:
- 主色 `#ff6034` 贯穿两端所有交互元素，建立统一品牌认知
- 卡片化布局承载信息，通过阴影分层而非边框分割
- 充分留白，降低信息密度压迫感
- 现有功能逻辑零改动，仅提升视觉表现力

---

## 二、统一设计规范

### 2.1 色彩体系

#### H5 端 CSS 变量（写入 `frontend-h5/src/styles/index.css` 的 `:root`）

```css
:root {
  /* === 主色系 === */
  --color-primary: #ff6034;          /* 品牌主色 */
  --color-primary-light: #ff8a65;    /* 主色浅色，渐变/悬浮态 */
  --color-primary-lighter: #ffab91;  /* 更浅，用于背景点缀 */
  --color-primary-bg: #fff5f0;       /* 主色背景，标签/提示底色 */
  --color-primary-dark: #e54d2e;     /* 主色深色，按压态 */

  /* === 辅色 === */
  --color-success: #07c160;          /* 成功/收入，微信绿 */
  --color-warning: #ff9f2c;          /* 警告/待处理 */
  --color-danger: #ee0a24;           /* 危险/支出/删除 */

  /* === 中性色 === */
  --color-text-primary: #1a1a1a;     /* 主文字，比原#323233更深 */
  --color-text-regular: #4a4a4a;     /* 常规文字 */
  --color-text-secondary: #8a8a8a;   /* 次要文字，比原#969799略深 */
  --color-text-placeholder: #b0b0b0; /* 占位文字 */
  --color-text-disabled: #c8c9cc;    /* 禁用文字 */

  /* === 背景色 === */
  --color-bg-page: #f7f8fa;          /* 页面背景，比原#f5f5f5更冷调 */
  --color-bg-card: #ffffff;          /* 卡片背景 */
  --color-bg-section: #f2f3f5;       /* 分区背景/侧边栏 */
  --color-bg-mask: rgba(0,0,0,0.5);  /* 遮罩 */

  /* === 边框/分割线 === */
  --color-border: #ebebed;           /* 卡片边框 */
  --color-divider: #f2f3f5;          /* 分割线 */
}
```

#### Admin 端 CSS 变量（写入 `frontend-admin/src/styles/index.css`）

```css
:root {
  /* === 主色系 — 与 H5 端统一 === */
  --el-color-primary: #ff6034;
  --el-color-primary-light-3: #ff8a65;
  --el-color-primary-light-5: #ffa784;
  --el-color-primary-light-7: #ffd0bf;
  --el-color-primary-light-8: #ffe4d7;
  --el-color-primary-light-9: #fff5f0;
  --el-color-primary-dark-2: #e54d2e;

  /* === 中性色 === */
  --color-text-primary: #1a1a1a;
  --color-text-regular: #4a4a4a;
  --color-text-secondary: #8a8a8a;
  --color-bg-page: #f5f6fa;
  --color-bg-card: #ffffff;
  --color-sidebar-bg: #2b2d3a;       /* 深色侧边栏，比原#304156更现代 */
  --color-sidebar-logo: #232530;     /* logo区更深 */

  /* === 语义色 === */
  --el-color-success: #07c160;
  --el-color-warning: #ff9f2c;
  --el-color-danger: #ee0a24;
}
```

> **关键变更**: Admin 端将 Element Plus 默认蓝色 `#409EFF` 全局替换为 `#ff6034`，通过覆盖 `--el-color-primary` 及其 9 级渐变实现。侧边栏从 `#304156` 改为 `#2b2d3a`，更偏冷灰现代感。

### 2.2 间距规范

| 场景 | H5 端 | Admin 端 | CSS 变量 |
|------|-------|----------|----------|
| 页面左右 padding | 16px | 24px | `--space-page-x` |
| 卡片间距（垂直） | 10px | 16px | `--space-card-y` |
| 卡片内 padding | 14px 16px | 20px 24px | `--space-card-inner` |
| 组件内元素间距 | 8px | 12px | `--space-component` |
| 列表项间距 | 10px | — | `--space-list-item` |
| 按钮组间距 | 8px | 12px | `--space-btn-group` |
| 区块标题上下间距 | 12px 16px 8px | 16px 0 12px | `--space-section-title` |

```css
/* H5 端变量 */
:root {
  --space-page-x: 16px;
  --space-card-y: 10px;
  --space-card-inner: 14px 16px;
  --space-component: 8px;
  --space-list-item: 10px;
  --space-btn-group: 8px;
}

/* Admin 端变量 */
:root {
  --space-page-x: 24px;
  --space-card-y: 16px;
  --space-card-inner: 20px 24px;
  --space-component: 12px;
  --space-btn-group: 12px;
}
```

### 2.3 圆角规范

| 元素 | H5 端 | Admin 端 | 说明 |
|------|-------|----------|------|
| 卡片容器 | 12px | 10px | 统一替换原来 8px/10px 混用 |
| 按钮 | 9999px (round) / 8px (rect) | 6px | — |
| 输入框 | 8px | 4px (Element 默认) | — |
| 图片缩略图 | 8px | 6px | — |
| 标签/Tag | 6px | 4px | — |
| 金额块/渐变背景块 | 0 (全宽) | — | 顶部渐变区不圆角 |
| 弹窗 | 16px (顶部) / 0 (底部) | 10px | — |
| 头像 | 50% (圆形) | 50% | — |

```css
:root {
  --radius-card: 12px;       /* H5 */
  --radius-btn: 8px;
  --radius-input: 8px;
  --radius-image: 8px;
  --radius-tag: 6px;
  --radius-popup: 16px;
}
```

> **关键变更**: H5 端 DishCard 从 `border-radius: 10px` 统一为 `12px`；OrderCard 从 `8px` 统一为 `12px`；分类图标从 `16px` 保持。Admin 端 el-card 从默认 `4px` 提升到 `10px`。

### 2.4 阴影规范

定义 4 级阴影，用于不同层级的视觉表达：

```css
:root {
  /* H5 端 */
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04);           /* 扁平卡片默认 */
  --shadow-md: 0 2px 8px rgba(0, 0, 0, 0.06);           /* 卡片悬浮/交互态 */
  --shadow-lg: 0 4px 16px rgba(0, 0, 0, 0.08);          /* 弹窗/浮层 */
  --shadow-primary: 0 4px 12px rgba(255, 96, 52, 0.25); /* 主色按钮阴影 */
  --shadow-top: 0 -2px 8px rgba(0, 0, 0, 0.04);         /* 底部固定栏顶部阴影 */
}
```

```css
:root {
  /* Admin 端 */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.03);
  --shadow-md: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);
  --shadow-card-hover: 0 4px 16px rgba(0, 0, 0, 0.08);
}
```

> **关键变更**:
> - H5 DishCard 阴影从 `0 1px 4px rgba(0,0,0,0.04)` 改为 `--shadow-sm`，`:active` 态改为 `--shadow-md`
> - H5 CartBar 从无阴影改为 `--shadow-lg`（底部浮动栏）
> - Admin el-card 从 `shadow="never"` 改为 `shadow="hover"` 或自定义 `--shadow-sm`
> - Admin 仪表盘 stat-card 增加 `--shadow-md`，hover 时 `--shadow-card-hover`

### 2.5 字体规范

| 层级 | H5 端字号/字重 | Admin 端字号/字重 | 用途 |
|------|----------------|-------------------|------|
| Display | 36px / 700 | 28px / 700 | 金额大数字（支付/余额） |
| H1-标题 | 20px / 700 | 20px / 600 | 页面标题/用户名 |
| H2-区域标题 | 17px / 700 | 16px / 600 | 区块标题（"热门推荐"） |
| H3-小标题 | 15px / 600 | 14px / 600 | 菜品名/卡片标题 |
| Body-正文 | 14px / 400 | 14px / 400 | 常规文字 |
| Body-次要 | 13px / 400 | 13px / 400 | 次要信息 |
| Caption | 12px / 400 | 12px / 400 | 标签/时间/提示 |
| Caption-mini | 11px / 400 | — | 口味标签等微型文字 |
| Price | 16px / 700 | — | 价格文字（主色） |
| Price-large | 22-24px / 700 | — | 合计/总额 |

```css
/* H5 端字体变量 */
:root {
  --font-size-display: 36px;
  --font-size-h1: 20px;
  --font-size-h2: 17px;
  --font-size-h3: 15px;
  --font-size-body: 14px;
  --font-size-body-sm: 13px;
  --font-size-caption: 12px;
  --font-size-caption-mini: 11px;
  --font-weight-bold: 700;
  --font-weight-semi: 600;
  --font-weight-regular: 400;
  --line-height-tight: 1.3;
  --line-height-normal: 1.5;
  --line-height-relaxed: 1.8;
}
```

> **关键变更**:
> - H5 主文字色从 `#323233` 加深为 `#1a1a1a`，提升对比度
> - H5 菜品描述从 `12px` 提升到 `13px`，改善可读性
> - Admin 正文色从 `#303133` 加深为 `#1a1a1a`

---

## 三、H5 用户端优化要点（逐页面）

### 3.1 首页 (home/index.vue)

1. **NavBar 优化**: 将 `van-nav-bar` 背景从纯色 `#ff6034` 改为渐变 `linear-gradient(135deg, #ff6034, #ff8a65)`，增加 `box-shadow: 0 2px 8px rgba(255,96,52,0.15)`，标题字号从默认改为 `18px / 700`。

2. **轮播图增强**: 高度从 `160px` 增加到 `180px`，添加 `border-radius: 0`（全宽无圆角，贴边显示）；底部添加 `margin: -20px 16px 0` 使其向上叠浮于 NavBar 之下，产生层次感；`swipe-img` 添加 `border-radius: 12px` 实现圆角轮播效果，配合 `overflow: hidden`。

3. **分类卡片微调**: `category-icon-wrap` 尺寸从 `52px` 增大到 `56px`，`border-radius` 从 `16px` 改为 `18px`；`category-label` 字号从 `12px` 提升到 `13px`，`margin-top` 从 `6px` 改为 `8px`；添加 `transition: transform 0.2s ease`，`:active` 时 `transform: scale(0.92)`。

4. **热门推荐区块**: `.hot-section` 添加 `border-radius: 12px; margin: 10px 12px 0; overflow: hidden`，使其成为独立圆角卡片；`.section-title` 前添加橙色竖线装饰 `border-left: 3px solid #ff6034; padding-left: 10px`。

### 3.2 菜单页 (menu/index.vue)

1. **搜索栏美化**: `van-search` 添加 `background: var(--color-bg-page)` 页面背景色融合；搜索框内 `padding: 8px 12px`，`background: #fff`，`box-shadow: var(--shadow-sm)`。

2. **侧边栏优化**: `.menu-sidebar` 背景从 `#f7f8fa` 改为 `#f0f1f3`（略深，与右侧形成对比）；`.sidebar-item` `padding` 从 `16px 8px` 改为 `14px 10px`；`.active-bar` 宽度从 `3px` 增加到 `4px`，`height` 从 `20px` 增加到 `24px`；选中项添加 `background: #fff` 改为 `background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.04)`。

3. **菜品图片放大**: DishCard 组件中 `van-image` 从 `80x80` 增大到 `88x88`，`radius` 从 `8` 改为 `10`。

4. **分类标题增强**: `.category-title` 添加左侧装饰 `&::before { content: ''; display: inline-block; width: 3px; height: 14px; background: #ff6034; border-radius: 2px; margin-right: 6px; vertical-align: middle; }`。

### 3.3 购物车 (cart/index.vue)

1. **CartBar 样式重构**: 背景从 `#333`（纯黑）改为 `linear-gradient(135deg, #3a3a3a, #2a2a2a)`；添加 `border-radius: 28px 28px 0 0`（顶部大圆角）；`box-shadow: var(--shadow-lg)`；`.cart-bar-right` 背景从 `#ff6034` 改为 `linear-gradient(135deg, #ff6034, #ff8a65)`，添加 `border-radius: 28px 0 0 0`；`.cart-icon-wrapper` 添加 `background: rgba(255,96,52,0.15); border-radius: 50%; width: 44px; height: 44px`。

2. **购物车列表卡片化**: `.cart-items` 添加 `margin: 10px 12px; border-radius: 12px; overflow: hidden; box-shadow: var(--shadow-sm)`；`.cart-item` `padding` 从 `12px` 改为 `14px 16px`；`.item-name` 字号从 `14px` 提升到 `15px`。

3. **桌号选择优化**: `.table-cell` 添加 `margin: 10px 12px; border-radius: 12px; overflow: hidden; box-shadow: var(--shadow-sm)`，与列表卡片保持一致的圆角卡片风格。

4. **底部结算栏美化**: `.cart-footer` 添加 `border-radius: 16px 16px 0 0`（顶部圆角），`box-shadow` 从 `0 -2px 8px rgba(0,0,0,0.06)` 改为 `var(--shadow-top)`；`.checkout-btn` 宽度从 `120px` 增加到 `140px`，高度通过 `--van-button-default-height: 40px` 调整。

### 3.4 确认订单 (confirm/index.vue)

1. **分组卡片圆角化**: 所有 `.confirm-section` 的 `van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px`；`margin-top` 从 `8px` 改为 `10px`。

2. **订单明细项优化**: `.order-item` `padding` 从 `12px 16px` 改为 `14px 16px`；`van-image` `radius` 从 `6` 改为 `8`；`.item-taste` `border-radius` 从 `8px` 改为 `6px`（标签更紧凑）。

3. **应付金额区域强化**: `.amount-section` 添加 `margin: 10px 12px; border-radius: 12px; box-shadow: var(--shadow-sm)`；`.amount-value` 字号从 `22px` 提升到 `24px`。

4. **提交栏圆角化**: `.submit-bar` 添加 `border-radius: 16px 16px 0 0`，`box-shadow` 改为 `var(--shadow-top)`。

### 3.5 支付页 (pay/index.vue)

1. **金额渐变区增强**: `.amount-block` 渐变从 `linear-gradient(135deg, #ff6034, #ff8a65)` 改为 `linear-gradient(135deg, #ff6034 0%, #ff8a65 60%, #ffab91 100%)`（三段渐变更丰富）；`padding` 从 `32px 16px` 改为 `36px 16px 28px`；`.amount-value` 字号从 `36px` 保持，添加 `text-shadow: 0 2px 8px rgba(0,0,0,0.1)`。

2. **支付方式卡片化**: `.pay-methods` `margin-top` 从 `12px` 改为 `-16px`（向上叠浮于渐变区上），添加 `border-radius: 16px 16px 0 0; background: var(--color-bg-page)`；内部 `van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm)`。

3. **支付图标圆形背景**: `.pay-icon` 添加 `width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #f7f8fa`，图标颜色保持不变但增加背景圆形底色。

4. **开发中标签美化**: `.dev-tag` 从灰色 `#c8c9cc` 改为 `background: linear-gradient(135deg, #d3d4d6, #c8c9cc); padding: 2px 8px; border-radius: 4px; font-size: 10px`。

### 3.6 登录页 (login/index.vue)

1. **背景渐变化**: `.login-page` 背景从 `#f5f5f5` 改为 `linear-gradient(180deg, #fff5f0 0%, #f7f8fa 30%, #f7f8fa 100%)`（顶部浅橙色渐变到灰），增加品牌温度感。

2. **Logo 区域增强**: `.logo` `padding` 从 `30px 0` 增加到 `40px 0 30px`；`van-icon` 外层添加 `width: 80px; height: 80px; border-radius: 24px; background: linear-gradient(135deg, #fff5f0, #ffe4d7); display: inline-flex; align-items: center; justify-content: center; box-shadow: 0 8px 24px rgba(255,96,52,0.15)`，图标包裹在圆角方形渐变容器中。

3. **表单卡片化**: `.van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px`。

4. **按钮阴影**: `.login-btn van-button` 添加 `box-shadow: var(--shadow-primary)`，即 `0 4px 12px rgba(255,96,52,0.25)`。

### 3.7 注册页 (register/index.vue)

与登录页保持一致优化：
1. 背景渐变化：同登录页 `linear-gradient(180deg, #fff5f0, #f7f8fa)`。
2. Logo 容器圆角化：同登录页 80px 圆角方形渐变容器。
3. 表单卡片化：cell-group 添加 `box-shadow + border-radius: 12px`。
4. 确认密码字段图标：`van-field` 添加 `left-icon="lock"` 增强视觉引导。

### 3.8 我的页面 (me/index.vue)

1. **用户卡片增强**: `.user-card` 渐变改为 `linear-gradient(135deg, #ff6034 0%, #ff8a65 50%, #ffab91 100%)`；`padding` 从 `24px 16px` 改为 `28px 20px`；添加 `margin-bottom: -20px` 使后续内容上浮叠接。

2. **头像容器美化**: `.avatar` 添加 `width: 64px; height: 64px; border-radius: 50%; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; border: 2px solid rgba(255,255,255,0.3)`。

3. **菜单分组卡片化**: `.menu-group` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px`；`margin-top` 从 `10px` 改为 `12px`。

4. **余额信息突出**: `.user-desc` 中余额部分添加 `background: rgba(255,255,255,0.2); padding: 2px 10px; border-radius: 12px; display: inline-block; margin-top: 6px`，使余额以胶囊形式展示。

### 3.9 余额页 (balance/index.vue)

1. **余额卡片增强**: `.balance-card` 渐变改为三段式 `linear-gradient(135deg, #ff6034, #ff8a65, #ffab91)`；添加 `margin: -20px 16px 0; border-radius: 16px; box-shadow: 0 8px 24px rgba(255,96,52,0.2)`，使其浮于页面上方。

2. **记录列表卡片化**: `.record-group` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px`。

3. **金额展示优化**: `.record-amount` 字号从 `16px` 提升到 `17px`；`.income` 颜色保持 `#07c160`；`.expense` 颜色保持 `#ee0a24`。

4. **加载更多按钮美化**: `.load-more` 从文字链接改为 `padding: 12px; text-align: center; color: var(--color-primary); font-size: 14px; cursor: pointer`，添加 `&:active { opacity: 0.7 }`。

### 3.10 充值/兑换页 (recharge/index.vue)

1. **余额卡片统一**: 与余额页使用相同的渐变+圆角+阴影处理，`margin: -20px 16px 0; border-radius: 16px; box-shadow: 0 8px 24px rgba(255,96,52,0.2)`。

2. **点卡列表项美化**: `.card-item` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px; border: 1px solid var(--color-divider)`；`.card-amount` 字号从 `20px` 保持，添加 `text-shadow: 0 1px 2px rgba(255,96,52,0.1)`。

3. **温馨提示区域美化**: `.tips` 添加 `border-left: 3px solid #ff6034; background: #fff9f5`（浅橙底色+左竖线），`border-radius` 从 `8px` 改为 `12px`。

4. **表单卡片化**: `van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm)`。

### 3.11 订单列表 (order/list.vue)

1. **Tab 样式增强**: `van-tabs` 底部指示条颜色已设为 `#ff6034`，保持；添加 `--van-tabs-nav-background: var(--color-bg-page)`；Tab 文字选中态字号增加 `font-weight: 700`（已有 active-text-color，补充字重）。

2. **OrderCard 美化**: `.order-card` `border-radius` 从 `8px` 改为 `12px`；添加 `box-shadow: var(--shadow-sm)`；`padding` 从 `12px` 改为 `14px 16px`；添加 `transition: box-shadow 0.2s ease`，`:active` 时 `box-shadow: var(--shadow-md)`。

3. **订单状态文字强化**: `.order-status` 添加 `background: var(--color-primary-bg); padding: 2px 8px; border-radius: 6px; font-size: 12px`，将状态文字以胶囊标签形式展示。

4. **空状态品牌化**: `van-empty` 添加自定义 `image-size="120"`，描述文字 `color: var(--color-text-secondary); font-size: 14px`。

### 3.12 订单详情 (order/detail.vue)

1. **状态横幅渐变化**: `.status-banner` 从纯色背景改为渐变 `background: linear-gradient(135deg, var(--status-color), var(--status-color-light))`；`padding` 从 `24px 16px` 改为 `28px 20px`；添加 `margin-bottom: -16px` 使后续内容上浮。

2. **步骤条优化**: `van-steps` 的 `active-color` 已为 `#ff6034`，保持；添加 `--van-step-finish-line-color: #ffab91`（已完成连线用浅色），`--van-step-finish-text-color: #ff6034`。

3. **明细卡片化**: `.detail-section` 的 `van-cell-group[inset]` 添加 `box-shadow: var(--shadow-sm); border-radius: 12px`。

4. **底部操作栏圆角**: `.action-bar` 添加 `border-radius: 16px 16px 0 0`，`box-shadow` 改为 `var(--shadow-top)`。

### 3.13 组件优化

#### DishCard.vue
- `border-radius` 从 `10px` 改为 `12px`
- `box-shadow` 从 `0 1px 4px rgba(0,0,0,0.04)` 改为 `var(--shadow-sm)`
- `.add-btn` 从 `padding: 2px` 改为 `width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; box-shadow: var(--shadow-primary)`，添加 `transition: transform 0.15s ease`，`:active` 时 `transform: scale(0.85)`
- `.dish-desc` 字号从 `12px` 提升到 `13px`

#### CartBar.vue
- 如 3.3 所述，整体深色背景渐变化 + 顶部圆角 + 浮动阴影

#### OrderCard.vue
- 如 3.11 所述，圆角 `12px` + 阴影 + 状态标签胶囊化

#### TabBar.vue
- `active-color` 保持 `#ff6034`，添加 `--van-tabbar-height: 56px`（从默认 50px 增加），`--van-tabbar-item-text-font-size: 11px`

#### TastePicker.vue
- `.option-chip` `border-radius` 从 `16px` 改为 `20px`（更圆润）；`padding` 从 `6px 16px` 改为 `8px 18px`
- `.option-chip.selected` 添加 `box-shadow: 0 2px 8px rgba(255,96,52,0.2)`
- `.required-badge` `border-radius` 从 `8px` 改为 `4px`

---

## 四、Admin 管理端优化要点（逐页面）

### 4.1 全局主题覆盖 (styles/index.css)

1. **Element Plus 主题色覆盖**: 在 `:root` 中添加上文定义的 `--el-color-primary` 及 9 级渐变变量，将全局蓝色替换为 `#ff6034`。

2. **滚动条美化**: 滚动条 thumb 颜色从 `#c0c4cc` 改为 `rgba(255,96,52,0.3)`，hover 时 `rgba(255,96,52,0.5)`；track 保持 `#f0f0f0`。

3. **el-main 间距**: `padding` 从 `20px` 改为 `24px`。

4. **body 背景色**: 从 `#f5f7fa` 改为 `#f5f6fa`（微调，更偏冷灰）。

### 4.2 布局 (layout/)

#### Sidebar.vue
1. 背景色从 `#304156` 改为 `#2b2d3a`（更现代的深灰）；logo 区从 `#2b3a4d` 改为 `#232530`。
2. `active-text-color` 从 `#409EFF` 改为 `#ff6034`。
3. `text-color` 从 `#bfcbd9` 改为 `#a0a3b1`（略亮，提升可读性）。
4. Logo 区高度从 `60px` 保持；添加 `border-bottom: 1px solid rgba(255,255,255,0.05)` 分割线。
5. `.logo-text` 字号从 `16px` 保持，添加 `letter-spacing: 0.5px`。
6. 选中菜单项添加 `box-shadow: inset 3px 0 0 #ff6034`（左侧高亮竖线，替代默认的右侧蓝条）。

#### Header.vue
1. `.header` 高度保持 `60px`（el-header 默认）；`border-bottom` 从 `1px solid #e6e6e6` 改为 `none`，改为 `box-shadow: 0 1px 4px rgba(0,0,0,0.04)`（用阴影替代边框，更柔和）。
2. `.admin-info` 添加 `padding: 6px 12px; border-radius: 8px; transition: background 0.2s`，hover 时 `background: var(--color-bg-page)`。
3. `.admin-name` 颜色从 `#303133` 改为 `var(--color-text-primary)`。
4. 头像添加 `border: 2px solid var(--el-color-primary-light-7)`。

### 4.3 登录页 (login/index.vue)

1. **背景渐变替换**: 从紫色 `linear-gradient(135deg, #667eea, #764ba2)` 改为暖色 `linear-gradient(135deg, #ff6034 0%, #ff8a65 50%, #ffab91 100%)`，与品牌主色统一。

2. **Logo 图标颜色**: `el-icon` 的 `color` 从 `#409EFF` 改为 `#ff6034`；尺寸从 `36` 增加到 `40`。

3. **卡片圆角**: `.login-card` `border-radius` 从 `12px` 改为 `16px`；添加 `box-shadow: 0 16px 48px rgba(0,0,0,0.12)`。

4. **登录按钮**: 添加 `box-shadow: 0 4px 12px rgba(255,96,52,0.3)`，`border-radius: 8px`。

5. **标题字重**: `.login-header h2` 字号从 `22px` 保持，`font-weight` 保持 `700`；`.login-header p` 颜色从 `#909399` 改为 `var(--color-text-secondary)`。

### 4.4 仪表盘 (dashboard/index.vue)

1. **统计卡片美化**: `.stat-card` `border-radius` 从 `8px` 改为 `12px`；添加 `transition: transform 0.2s, box-shadow 0.2s`，hover 时 `transform: translateY(-2px); box-shadow: var(--shadow-card-hover)`。

2. **图标背景圆角**: `.stat-icon` `border-radius` 从 `8px` 改为 `14px`（更圆润）；尺寸从 `56px` 增加到 `60px`；四个图标背景色调整：
   - 今日订单：`#ff6034`（主色，替代蓝色 `#409EFF`）
   - 今日营业额：`#07c160`（保持绿色）
   - 桌台使用率：`#ff9f2c`（保持橙色，但与主色区分）
   - 总菜品数：`#7c4dff`（紫色，替代红色 `#F56C6C`，避免与危险色混淆）

3. **图表卡片**: `el-card` `border-radius` 添加 `12px`；图表折线颜色从 `#409EFF` 改为 `#ff6034`，面积渐变从 `rgba(64,158,255,0.3)` 改为 `rgba(255,96,52,0.25)` → `rgba(255,96,52,0.01)`。

4. **统计数值字号**: `.stat-value` 字号从 `24px` 保持；`.stat-label` 颜色从 `#909399` 改为 `var(--color-text-secondary)`。

### 4.5 菜品管理 (dish/index.vue)

1. **搜索卡片**: `.search-card` 添加 `border-radius: 12px`；`margin-bottom` 保持 `16px`；el-form-item label 颜色改为 `var(--color-text-regular)`。

2. **表格美化**: `el-table` 的 `stripe` 行背景从默认改为 `#fafafa`；`border` 颜色改为 `var(--color-border)`；表头添加 `background: #f7f8fa; font-weight: 600; color: var(--color-text-primary)`。

3. **图片缩略图**: `el-image` 圆角从 `4px` 改为 `8px`；尺寸从 `50x50` 增加到 `56x56`；`.image-placeholder` 圆角同步改为 `8px`。

4. **操作按钮间距**: `el-button[link]` 之间添加 `margin: 0 4px`（通过 `el-table` 的 `el-button + el-button { margin-left: 8px }` 已有，微调即可）。

5. **分页美化**: `.pagination` `margin-top` 从 `16px` 改为 `20px`；添加 `padding-top: 16px; border-top: 1px solid var(--color-divider)`。

### 4.6 分类管理 (category/index.vue)

1. **工具栏美化**: `.toolbar` 添加 `padding-bottom: 16px; border-bottom: 1px solid var(--color-divider)`；`.page-title` 字号从 `16px` 保持，颜色改为 `var(--color-text-primary)`。

2. **排序按钮优化**: 上移/下移按钮添加 `padding: 4px 8px`，使点击区域更舒适。

3. **表格统一**: 与菜品管理保持一致的 stripe 行色和表头样式。

4. **状态标签圆角**: `el-tag` 添加 `border-radius: 4px`（Element Plus 默认为 0，添加微圆角）。

### 4.7 桌台管理 (table/index.vue)

1. **卡片圆角**: `el-card` 添加 `border-radius: 12px`。
2. **工具栏与分类管理统一**: 同 4.6 的工具栏处理。
3. **状态标签配色**: 空闲(success)保持绿色；使用中(warning)改为 `#ff9f2c`；已预约(primary)改为 `#ff6034`；待清理(info)保持灰色。注意：这里 primary 已被全局覆盖为 `#ff6034`，需检查 `tableStatusType` 函数返回的 `'primary'` 是否会正确映射。
4. **表格 stripe + 表头**: 与菜品管理统一。

### 4.8 订单管理 (order/index.vue)

1. **搜索卡片**: 与菜品管理保持一致的圆角和间距。

2. **金额展示强化**: `.amount` 颜色从 `#F56C6C`（Element 红色）改为 `var(--color-danger)` 统一；`font-weight: 700`。

3. **状态标签配色调整**: 将 `statusTagType` 函数中的 `'primary'` 改为 `'warning'` 或自定义颜色，因为全局 primary 已变为 `#ff6034`（橙色），"制作中"状态用橙色 tag 会与品牌色混淆。建议：
   - 待支付(info) → 保持灰色
   - 待接单(warning) → 保持 `#ff9f2c`
   - 制作中 → 改用自定义 `color: #7c4dff`（紫色 tag，通过 `effect="dark"` + 自定义 style）
   - 已完成(success) → 保持绿色
   - 已取餐(success) → 保持绿色
   - 已取消(danger) → 保持红色

4. **操作按钮分组**: "接单""完成""取餐"按钮添加 `el-button` 的 `size="small"` + `round`，使操作更醒目。

### 4.9 轮播图管理 (banner/index.vue)

1. **搜索卡片**: 仅有一个新增按钮，将 `.search-card` 简化为 `display: flex; justify-content: flex-end; padding: 16px 20px; border-radius: 12px`。
2. **图片预览**: `el-image` 圆角从 `4px` 改为 `8px`；`.image-placeholder` 圆角同步。
3. **链接文字**: `.link-text` 颜色从 `#409eff` 改为 `var(--el-color-primary)` 即 `#ff6034`。
4. **表格统一**: stripe + 表头与菜品管理一致。

### 4.10 点卡管理 (card/index.vue)

1. **卡片头部**: `.card-header` 字体添加 `font-size: 16px; font-weight: 600; color: var(--color-text-primary)`。
2. **页面 padding 去重**: `.card-page` 的 `padding: 20px` 与 `el-main` 的 padding 重复，移除 `.card-page` 的 padding（由 el-main 统一控制）。
3. **搜索表单间距**: `.search-form` `margin-bottom` 从 `20px` 改为 `16px`。
4. **表格 + 分页**: 与其他管理页统一 stripe、表头、分页样式。

### 4.11 会员管理 (member/index.vue)

1. **页面 padding 去重**: 同点卡管理，移除 `.member-page` 的 `padding: 20px`。
2. **余额列高亮**: 余额 `¥` 文字添加 `color: var(--el-color-primary); font-weight: 600`。
3. **余额记录弹窗**: 弹窗内表格添加 `border-radius: 8px; overflow: hidden`；`.income` 颜色保持 `#67c23a`；`.expense` 颜色从 `#f56c6c` 改为 `var(--color-danger)`。
4. **查询按钮**: 统一使用 `type="primary"` + 主色覆盖后的效果。

### 4.12 用户管理 (user/index.vue)

1. **工具栏统一**: 与分类/桌台管理保持一致的工具栏样式。
2. **头像美化**: `el-avatar` 添加 `background: var(--el-color-primary-light-8)` 作为默认底色。
3. **角色标签配色**: 管理员(danger)保持红色；服务员(primary)变为 `#ff6034`（因全局覆盖），如需区分可改为 `type="success"`。
4. **表格统一**: stripe + 表头一致。

### 4.13 弹窗组件统一 (DishDialog / BannerDialog 等)

1. **弹窗圆角**: `el-dialog` 添加 `border-radius: 12px; overflow: hidden`（通过 `:deep(.el-dialog)` 实现）。
2. **表单间距**: `el-form-item` `margin-bottom` 保持默认，但添加 `padding: 0 4px` 微调。
3. **DishDialog 上传区域**: `.image-uploader .el-upload` 边框从 `1px dashed #d9d9d9` 改为 `1px dashed var(--el-color-primary-light-5)`；hover 时 `border-color: var(--el-color-primary)`；圆角从 `6px` 改为 `10px`。
4. **口味组容器**: `.taste-group` 背景从 `#fafafa` 改为 `var(--el-color-primary-light-9)` 即 `#fff5f0`；`border` 从 `1px solid #ebeef5` 改为 `1px solid var(--el-color-primary-light-7)`；`border-radius` 从 `6px` 改为 `10px`。

---

## 五、交互体验优化

### 5.1 过渡动画

| 场景 | 动画方案 | CSS 实现 |
|------|----------|----------|
| 页面切换 | 路由过渡淡入 | `router-view` 包裹 `<transition name="fade">`，`.fade-enter-active { transition: opacity 0.2s ease }` |
| 卡片点击 | 按压缩放 | `transition: transform 0.15s ease; :active { transform: scale(0.97) }` |
| 按钮点击 | 按压缩放 | `transition: transform 0.1s ease; :active { transform: scale(0.95) }` |
| 弹窗出现 | 上滑+淡入 | Vant/Element 默认动画保持 |
| 列表项进入 | 错位淡入 | `.list-item { animation: slideIn 0.3s ease both }` + `animation-delay: calc(var(--i) * 0.05s)` |
| 侧边栏选中 | 背景过渡 | `transition: background 0.2s ease, color 0.2s ease` |
| 价格变化 | 数字弹跳 | `@keyframes priceBounce { 0%{transform:scale(1)} 50%{transform:scale(1.15)} 100%{transform:scale(1)} }` |
| Tab 切换 | 下划线滑动 | Vant 默认已实现，保持 |

### 5.2 加载状态

| 场景 | H5 端方案 | Admin 端方案 |
|------|-----------|-------------|
| 页面首次加载 | `van-loading` 居中，`color="#ff6034"`，`size="36px"`，`padding: 80px 0` | `v-loading="true"` 全屏遮罩，`text="加载中..."`，`background: rgba(255,255,255,0.8)` |
| 列表加载更多 | 底部 `van-loading` 小号 `size="24px"` + "加载中..."文字 | 底部 `el-loading` 小号 |
| 按钮提交中 | `van-button` 的 `loading` 属性，`loading-text` 已有 | `el-button` 的 `:loading="true"` |
| 图片加载 | `van-image` 默认有 loading 插槽，添加 `#loading` 自定义浅灰背景 | `el-image` 的 `#placeholder` 插槽 |

### 5.3 空状态

统一空状态处理，替换 Vant/Element 默认空状态图：

```css
/* H5 端空状态统一样式 */
:root {
  --van-empty-image-size: 120px;
  --van-empty-description-color: var(--color-text-secondary);
  --van-empty-description-font-size: 14px;
  --van-empty-padding: 40px 16px;
}

/* Admin 端空状态 */
.el-empty {
  --el-empty-padding: 40px 0;
}
.el-empty__description {
  color: var(--color-text-secondary);
  font-size: 14px;
}
```

**具体页面空状态**:
- 购物车空：保持 `van-empty` + "去点餐"按钮，按钮添加 `box-shadow: var(--shadow-primary)`
- 订单列表空：描述文字改为 "暂无订单记录"，添加 `image-size="120"`
- 余额记录空：描述改为 "暂无余额变动记录"
- 搜索无结果：描述改为 "未找到相关菜品，换个关键词试试"

### 5.4 操作反馈

| 操作 | 反馈方式 | 实现 |
|------|----------|------|
| 添加购物车 | Toast + 购物车角标弹跳 | Toast 已有；CartBar 的 `van-badge` 添加 `animation: badgeBounce 0.3s ease`，`@keyframes badgeBounce { 0%{transform:scale(1)} 50%{transform:scale(1.3)} 100%{transform:scale(1)} }` |
| 删除菜品 | Toast + 滑动归位 | Vant SwipeCell 默认行为保持 |
| 提交订单 | 按钮 loading + Toast | 已有 |
| 支付成功 | 全屏遮罩动画 | 已有 `scaleIn` + `fadeIn`，保持 |
| 表单验证失败 | 字段下方红色提示 | Vant/Element 默认，保持 |
| 下拉刷新 | 顶部加载指示器 | Vant `van-pull-refresh` 默认，添加 `--van-pull-refresh-head-text-color: var(--color-primary)` |

### 5.5 滚动体验优化

1. **全局平滑滚动**: `html { scroll-behavior: smooth; -webkit-overflow-scrolling: touch; }`（H5 端）
2. **滚动条隐藏**: H5 端横向滚动区域（分类导航、菜品列表）保持 `scrollbar-width: none; &::-webkit-scrollbar { display: none }`
3. **Admin 滚动条**: 保留美化滚动条，颜色改为 `rgba(255,96,52,0.3)`

---

## 六、实施优先级

| 优先级 | 内容 | 影响范围 |
|--------|------|----------|
| P0 | 全局 CSS 变量定义（色彩/间距/圆角/阴影/字体） | 两端 index.css |
| P0 | Admin 端 Element Plus 主题色覆盖 (#409EFF → #ff6034) | Admin 全局 |
| P0 | H5 端 NavBar 渐变化 + 阴影 | 所有 H5 页面 |
| P0 | H5 端卡片圆角统一 (12px) + 阴影分层 | DishCard/OrderCard/各 section |
| P1 | H5 端 CartBar 样式重构（圆角+渐变+阴影） | CartBar.vue |
| P1 | Admin 端登录页背景渐变替换 | login/index.vue |
| P1 | Admin 端 Sidebar 配色更新 | Sidebar.vue |
| P1 | Admin 仪表盘统计卡片+图表配色 | dashboard/index.vue |
| P1 | Admin 表格统一样式（stripe/表头/边框） | 所有表格页面 |
| P2 | 过渡动画（卡片点击/列表进入/价格弹跳） | 全局 |
| P2 | 空状态统一美化 | 两端 |
| P2 | Admin 弹窗圆角+表单细节 | DishDialog 等 |
| P2 | H5 登录/注册页 Logo 容器美化 | login/register |

---

## 七、注意事项

1. **不改功能逻辑**: 所有优化仅涉及 `<template>` 结构调整和 `<style>` CSS 修改，`<script>` 中的业务逻辑保持不变。如需调整 `statusTagType` 等返回样式类型的函数，仅改返回值映射，不改函数签名。

2. **Vant 主题覆盖**: H5 端通过 `:root` 中的 `--van-*` 变量覆盖 Vant 4 默认主题，现有覆盖项保留，新增变量按需添加。

3. **Element Plus 主题覆盖**: Admin 端通过 `--el-color-primary` 及其 9 级渐变实现全局换色，无需修改组件源码。注意检查所有硬编码 `#409EFF` 的内联样式（如 dashboard 的 `style="background-color: #409EFF"`），替换为 CSS 变量。

4. **兼容性**: 所有 CSS 特性（CSS Variables、flexbox、gradient、box-shadow）均已被目标浏览器广泛支持，无需 polyfill。

5. **性能**: 阴影和渐变会增加 GPU 渲染开销，但本项目的页面复杂度不会产生可感知的性能影响。`will-change: transform` 仅在动画元素上添加，避免全局滥用。

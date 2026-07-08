# 智能点餐系统

一套完整的餐厅点餐系统，包含 H5 用户端、后台管理端和 Java 后端 API。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21 + Spring Boot 3.2.5 + MyBatis-Plus 3.5.7 + MySQL 8.0 |
| H5 用户端 | Vue 3 + Vant 4 + Vite + Pinia + TypeScript |
| 管理后台 | Vue 3 + Element Plus + Vite + TypeScript |
| 图片存储 | 阿里云 OSS |
| 部署 | Nginx + systemd |

## 功能模块

### H5 用户端（移动端点餐）

- 菜品浏览、分类筛选、口味选择
- 购物车管理、订单确认
- 用户注册 / 登录（滑块验证码）
- 余额支付、点卡充值
- 订单查看、订单详情
- 轮播图展示

### Admin 管理后台

- 数据仪表盘（营收统计、订单趋势图表）
- 菜品管理（增删改查、图片上传、口味配置）
- 分类管理
- 桌台管理
- 订单管理（实时查看、状态流转）
- 轮播图管理（支持外部链接跳转）
- 会员管理、点卡批量生成与发放
- 系统用户管理

## 项目结构

```
order-food/
├── backend/                  # Java 后端
│   └── src/main/java/com/restaurant/
│       ├── controller/       # 控制器（admin/ + h5/）
│       ├── service/          # 业务逻辑
│       ├── entity/           # 数据实体
│       ├── mapper/           # MyBatis-Plus Mapper
│       ├── interceptor/      # JWT 认证拦截器
│       ├── config/           # 配置（OSS、WebMvc、Cors）
│       └── common/           # 全局异常处理、统一响应
├── frontend-h5/              # H5 用户端
│   └── src/views/            # home, menu, cart, order, pay, login...
├── frontend-admin/           # 管理后台
│   └── src/views/            # dashboard, dish, category, order...
├── deploy/                   # 部署相关
│   ├── deploy.sh             # 服务器一键部署脚本
│   ├── config/               # 生产环境配置
│   ├── nginx/                # Nginx 配置
│   ├── systemd/              # systemd 服务配置
│   └── sql/                  # 数据库脚本
└── docs/                     # 项目文档
```

## 本地开发

### 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+

### 1. 初始化数据库

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p order_food < backend/src/main/resources/db/data.sql
```

### 2. 配置后端

后端配置采用多 Profile 分离，敏感信息不进 Git：

```bash
# 复制模板并填写真实密码
cp backend/src/main/resources/application-local.yml.example \
   backend/src/main/resources/application-local.yml
```

编辑 `application-local.yml`，填入你的 MySQL 密码、JWT 密钥、OSS 密钥。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

### 4. 启动 H5 用户端

```bash
cd frontend-h5
npm install
npm run dev
# H5 运行在 http://localhost:5174
```

### 5. 启动管理后台

```bash
cd frontend-admin
npm install
npm run dev
# Admin 运行在 http://localhost:5173
```

### 默认账号

- 管理后台：`admin` / `admin123`

## 生产部署

### 架构

```
Nginx (80)
├── /          → H5 静态文件
├── /admin/    → Admin 静态文件
└── /api/      → 反代到 Java (8080)
```

### 部署步骤

1. 构建前端：

```bash
cd frontend-h5 && npm run build   # 产出 dist/
cd frontend-admin && npm run build # 产出 dist/
```

2. 打包后端：

```bash
cd backend && mvn package -DskipTests
# 产出 target/order-food-backend-1.0.0.jar
```

3. 上传 `deploy/` 目录到服务器，执行：

```bash
sudo bash deploy.sh
```

脚本会自动安装 JDK、MySQL、Nginx，导入数据库，配置并启动服务。

### 服务器目录结构

```
/opt/order-food/
├── backend/          # JAR 包
├── frontend/
│   ├── h5/           # H5 静态文件
│   └── admin/        # Admin 静态文件
├── config/           # 生产环境配置
├── logs/             # 日志
└── .credentials      # 数据库密码和 JWT 密钥
```

## API 概览

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| H5 菜品 | `/api/h5/dishes` | 菜品列表、详情 |
| H5 分类 | `/api/h5/categories` | 分类列表 |
| H5 订单 | `/api/h5/orders` | 下单、订单详情、订单列表 |
| H5 会员 | `/api/h5/member` | 注册、登录、余额、点卡兑换 |
| Admin 登录 | `/api/admin/login` | 管理员登录 |
| Admin 菜品 | `/api/admin/dishes` | 菜品 CRUD |
| Admin 订单 | `/api/admin/orders` | 订单管理 |
| Admin 仪表盘 | `/api/admin/dashboard` | 数据统计 |
| 文件上传 | `/api/admin/upload` | 图片上传到 OSS |

## License

MIT

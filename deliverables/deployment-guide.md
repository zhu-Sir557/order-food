# 智能点餐系统 — 项目部署文档

> 版本：1.0.0 · 更新日期：2026-07-07

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术栈与版本](#2-技术栈与版本)
3. [环境要求](#3-环境要求)
4. [项目结构](#4-项目结构)
5. [本地开发运行](#5-本地开发运行)
6. [生产环境构建](#6-生产环境构建)
7. [部署上线（Nginx + Java）](#7-部署上线nginx--java)
8. [Docker Compose 部署](#8-docker-compose-部署可选)
9. [配置项说明](#9-配置项说明)
10. [常见问题排查](#10-常见问题排查)

---

## 1. 项目概述

智能点餐系统是一套完整的餐饮点餐解决方案，包含三个子系统：

| 子系统 | 说明 | 端口 |
|--------|------|------|
| **后端 API** | Java Spring Boot 服务，提供全部业务接口 | 8080 |
| **H5 用户端** | 移动端点餐界面（Vant 4），面向顾客 | 5174（开发） |
| **Admin 管理端** | 后台管理系统（Element Plus），面向餐厅管理员 | 5173（开发） |

**核心功能**：菜品浏览与点餐、购物车、订单管理、支付（余额支付）、用户注册登录（滑块验证码）、点卡充值系统、轮播图管理、桌台管理、数据仪表盘。

---

## 2. 技术栈与版本

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.2.5 | Web 框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL Connector/J | 8.0.33 | 数据库驱动 |
| JJWT | 0.12.6 | JWT 认证 |
| Lombok | 1.18.30 | 代码简化 |
| Hutool | 5.8.27 | 工具库 |
| Spring Security | — | 仅用 BCrypt 密码加密 |
| Maven | 3.9+ | 构建工具 |

### H5 用户端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | ^3.4.0 | 前端框架 |
| Vant | ^4.9.0 | 移动端 UI 库 |
| Vue Router | ^4.3.0 | 路由 |
| Pinia | ^2.1.0 | 状态管理 |
| Axios | ^1.7.0 | HTTP 请求 |
| Vite | ^5.3.0 | 构建工具 |
| TypeScript | ^5.4.0 | 类型安全 |
| Day.js | ^1.11.0 | 日期处理 |

### Admin 管理端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | ^3.4.21 | 前端框架 |
| Element Plus | ^2.7.0 | 桌面端 UI 库 |
| ECharts | ^5.5.0 | 图表库 |
| Vue Router | ^4.3.0 | 路由 |
| Pinia | ^2.1.7 | 状态管理 |
| Axios | ^1.7.2 | HTTP 请求 |
| Vite | ^5.3.1 | 构建工具 |
| TypeScript | ^5.4.5 | 类型安全 |

### 数据库

| 技术 | 版本 | 说明 |
|------|------|------|
| MySQL | 8.0+ | 关系型数据库，字符集 utf8mb4 |

---

## 3. 环境要求

### 本地开发

| 软件 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 21 | 必须为 JDK 21，不支持更低版本 |
| Maven | 3.8+ | 后端构建 |
| Node.js | 18+ | 前端构建（推荐 20 LTS） |
| npm | 9+ | 包管理器 |
| MySQL | 8.0+ | 数据库 |
| Git | 2.30+ | 版本控制 |

### 生产服务器

| 资源 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2 核 | 4 核 |
| 内存 | 2 GB | 4 GB |
| 磁盘 | 20 GB | 50 GB（SSD） |
| 操作系统 | CentOS 7+ / Ubuntu 20.04+ / Debian 11+ | Ubuntu 22.04 LTS |
| 已安装软件 | JDK 21、MySQL 8.0、Nginx | 同左 |

---

## 4. 项目结构

```
order_food/
├── backend/                          # 后端 Spring Boot 项目
│   ├── pom.xml                       # Maven 依赖配置
│   ├── src/main/java/com/restaurant/
│   │   ├── config/                   # 配置类（WebMvc、CORS、MyBatis）
│   │   ├── controller/               # 控制器
│   │   │   ├── admin/                # 管理端接口
│   │   │   └── h5/                   # 用户端接口
│   │   ├── dto/                      # 数据传输对象
│   │   ├── entity/                   # 实体类
│   │   ├── enums/                    # 枚举
│   │   ├── interceptor/              # 拦截器（认证）
│   │   ├── mapper/                   # MyBatis Mapper
│   │   ├── service/                  # 业务逻辑层
│   │   ├── util/                     # 工具类（JWT、验证码等）
│   │   └── vo/                       # 视图对象
│   └── src/main/resources/
│       ├── application.yml           # 主配置
│       ├── application-dev.yml       # 开发环境配置
│       └── db/                       # 数据库脚本
│           ├── schema.sql            # 建表脚本
│           ├── data.sql              # 初始数据
│           ├── migration_taste_config.sql  # 口味配置迁移
│           └── migration_user_balance.sql  # 余额系统迁移
│
├── frontend-h5/                      # H5 用户端
│   ├── package.json
│   ├── vite.config.ts
│   ├── .env.development              # 开发环境变量
│   ├── .env.production               # 生产环境变量
│   └── src/
│       ├── api/                      # API 请求
│       ├── components/               # 公共组件
│       ├── router/                   # 路由
│       ├── store/                    # Pinia 状态
│       ├── styles/                   # 全局样式
│       ├── types/                    # 类型定义
│       └── views/                    # 页面
│
├── frontend-admin/                   # Admin 管理端
│   ├── package.json
│   ├── vite.config.ts
│   ├── .env.development
│   ├── .env.production
│   └── src/
│       ├── api/
│       ├── components/
│       ├── layout/
│       ├── router/
│       ├── store/
│       ├── styles/
│       └── views/
│
├── uploads/                          # 图片上传目录（自动创建）
└── deliverables/                     # 交付文档
```

---

## 5. 本地开发运行

### 5.1 克隆项目

```bash
git clone <仓库地址> order_food
cd order_food
```

### 5.2 数据库初始化

#### 5.2.1 安装并启动 MySQL 8.0

确保 MySQL 服务已启动，能通过以下命令连接：

```bash
mysql -u root -p
```

#### 5.2.2 执行数据库脚本

**按以下顺序**执行 4 个 SQL 脚本：

```bash
# 1. 建表（创建数据库 + 所有表结构）
mysql -u root -p < backend/src/main/resources/db/schema.sql

# 2. 初始数据（管理员账号 + 分类 + 示例菜品）
mysql -u root -p order_food < backend/src/main/resources/db/data.sql

# 3. 口味配置迁移（菜品口味字段）
mysql -u root -p order_food < backend/src/main/resources/db/migration_taste_config.sql

# 4. 余额系统迁移（会员表 + 点卡表 + 余额记录表 + 订单字段）
mysql -u root -p order_food < backend/src/main/resources/db/migration_user_balance.sql
```

> **默认管理员账号**：`admin` / `admin123`

#### 5.2.3 验证数据库

```sql
mysql -u root -p -e "USE order_food; SHOW TABLES;"
```

应看到以下表：`admin_user`、`category`、`dish`、`dish_taste`、`banner`、`restaurant_table`、`order_info`、`order_item`、`member`、`recharge_card`、`balance_record`。

### 5.3 配置数据库连接

编辑 `backend/src/main/resources/application.yml` 和 `application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/order_food?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root          # ← 改为你的 MySQL 用户名
    password: 123456        # ← 改为你的 MySQL 密码
```

> ⚠️ `application-dev.yml` 会覆盖 `application.yml` 中的同名字段，**两个文件都要改**。

### 5.4 启动后端

```bash
cd backend

# 方式一：Maven 直接运行（开发推荐）
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/order-food-backend-1.0.0.jar
```

后端启动后，API 服务在 `http://localhost:8080`。

**验证**：浏览器访问 `http://localhost:8080/api/h5/categories`，应返回分类 JSON 数据。

### 5.5 启动 H5 用户端

```bash
cd frontend-h5

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

H5 用户端在 `http://localhost:5174`，手机浏览器访问效果最佳。

### 5.6 启动 Admin 管理端

```bash
cd frontend-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

Admin 管理端在 `http://localhost:5173`，使用 `admin` / `admin123` 登录。

### 5.7 本地开发端口一览

| 服务 | 地址 | 说明 |
|------|------|------|
| 后端 API | http://localhost:8080 | 所有 /api/* 接口 |
| H5 用户端 | http://localhost:5174 | 移动端点餐 |
| Admin 管理端 | http://localhost:5173 | 后台管理 |
| 图片访问 | http://localhost:8080/uploads/xxx.png | 上传的图片 |

> 开发环境下，前端 Vite 已配置代理，`/api` 和 `/uploads` 请求自动转发到后端 8080 端口，无需额外配置。

---

## 6. 生产环境构建

### 6.1 后端打包

```bash
cd backend

# 编译打包（跳过测试加速）
mvn clean package -DskipTests
```

产物：`backend/target/order-food-backend-1.0.0.jar`

### 6.2 H5 用户端构建

```bash
cd frontend-h5

# 安装依赖（首次）
npm install

# 构建生产包
npm run build
```

产物：`frontend-h5/dist/`（静态文件目录）

### 6.3 Admin 管理端构建

```bash
cd frontend-admin

# 安装依赖（首次）
npm install

# 构建生产包
npm run build
```

产物：`frontend-admin/dist/`（静态文件目录）

### 6.4 构建产物总览

| 组件 | 产物路径 | 说明 |
|------|---------|------|
| 后端 | `backend/target/order-food-backend-1.0.0.jar` | 可执行 JAR |
| H5 前端 | `frontend-h5/dist/` | 静态 HTML/JS/CSS |
| Admin 前端 | `frontend-admin/dist/` | 静态 HTML/JS/CSS |

---

## 7. 部署上线（Nginx + Java）

> 这是最通用的部署方案，适用于 Linux 服务器。

### 7.1 服务器目录规划

```
/opt/order-food/
├── backend/
│   └── order-food-backend-1.0.0.jar    # 后端 JAR
├── frontend/
│   ├── h5/                              # H5 前端静态文件
│   └── admin/                           # Admin 前端静态文件
├── uploads/                             # 图片上传目录
├── logs/                                # 日志目录
└── config/
    └── application-prod.yml             # 生产环境配置
```

### 7.2 上传文件到服务器

```bash
# 在服务器创建目录
ssh user@your-server
sudo mkdir -p /opt/order-food/{backend,frontend/h5,frontend/admin,uploads,logs,config}
sudo chown -R $USER:$USER /opt/order-food

# 在本地执行上传
# 1. 上传后端 JAR
scp backend/target/order-food-backend-1.0.0.jar user@your-server:/opt/order-food/backend/

# 2. 上传 H5 前端
scp -r frontend-h5/dist/* user@your-server:/opt/order-food/frontend/h5/

# 3. 上传 Admin 前端
scp -r frontend-admin/dist/* user@your-server:/opt/order-food/frontend/admin/
```

### 7.3 创建生产环境配置

在服务器上创建 `/opt/order-food/config/application-prod.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/order_food?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: <你的生产数据库密码>       # ← 必须修改
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.restaurant.entity
  configuration:
    map-underscore-to-camel-case: true
    # 生产环境关闭 SQL 日志
    # log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: <生产环境JWT密钥>              # ← 必须修改为随机字符串
  expiration: 24

file:
  upload-path: /opt/order-food/uploads   # ← 生产环境上传路径
  allowed-types: image/jpeg,image/png,image/gif,image/webp

logging:
  level:
    com.restaurant: info
  file:
    name: /opt/order-food/logs/app.log
```

### 7.4 修改后端静态资源映射

后端 JAR 中的 `WebMvcConfig.java` 硬编码了上传路径，生产环境需通过启动参数覆盖。

编辑源码 `backend/src/main/java/com/restaurant/config/WebMvcConfig.java`，将上传路径改为可配置：

```java
@Value("${file.upload-path}")
private String uploadPath;

@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadPath + "/");
}
```

重新打包后上传。或者保持代码不变，确保生产路径与代码中一致即可（推荐改为可配置）。

### 7.5 服务器安装 JDK 21

```bash
# Ubuntu / Debian
sudo apt update
sudo apt install -y openjdk-21-jre-headless

# CentOS / RHEL
sudo yum install -y java-21-openjdk-headless

# 验证
java -version
# 应输出 openjdk version "21.x.x"
```

### 7.6 服务器安装 MySQL 8.0

```bash
# Ubuntu
sudo apt install -y mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql

# CentOS
sudo yum install -y mysql-community-server
sudo systemctl start mysqld
sudo systemctl enable mysqld
```

创建数据库并执行脚本（参考 [5.2 数据库初始化](#52-数据库初始化)）。

**生产环境安全建议**：
```sql
-- 创建专用数据库用户（不用 root）
CREATE USER 'orderfood'@'localhost' IDENTIFIED BY '<强密码>';
GRANT ALL PRIVILEGES ON order_food.* TO 'orderfood'@'localhost';
FLUSH PRIVILEGES;
```

### 7.7 服务器安装 Nginx

```bash
# Ubuntu
sudo apt install -y nginx

# CentOS
sudo yum install -y nginx

sudo systemctl start nginx
sudo systemctl enable nginx
```

### 7.8 配置 Nginx

创建 `/etc/nginx/conf.d/order-food.conf`：

```nginx
# ===== 智能点餐系统 Nginx 配置 =====

# 后端 API 上游
upstream order_food_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

# H5 用户端
server {
    listen 80;
    server_name h5.your-domain.com;       # ← 改为你的域名

    root /opt/order-food/frontend/h5;
    index index.html;

    # 前端路由（Vue Router history 模式）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://order_food_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_read_timeout 60s;
    }

    # 图片代理
    location /uploads/ {
        proxy_pass http://order_food_backend;
        proxy_set_header Host $host;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 文件上传大小限制
    client_max_body_size 10m;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1024;
}

# Admin 管理端
server {
    listen 80;
    server_name admin.your-domain.com;     # ← 改为你的域名

    root /opt/order-food/frontend/admin;
    index index.html;

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://order_food_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 图片代理
    location /uploads/ {
        proxy_pass http://order_food_backend;
        proxy_set_header Host $host;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    client_max_body_size 10m;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1024;
}
```

> **单域名方案**：如果只有一个域名，可以用路径区分：
> - `your-domain.com/` → H5 用户端
> - `your-domain.com/admin/` → Admin 管理端
> 
> 此时 Admin 前端构建需设置 `base: '/admin/'`。

测试并重载 Nginx：

```bash
sudo nginx -t          # 检查配置语法
sudo nginx -s reload   # 重载配置
```

### 7.9 配置 HTTPS（推荐）

使用 Let's Encrypt 免费证书：

```bash
# 安装 Certbot
sudo apt install -y certbot python3-certbot-nginx

# 自动配置 HTTPS
sudo certbot --nginx -d h5.your-domain.com -d admin.your-domain.com

# 自动续期（已自动配置，可验证）
sudo certbot renew --dry-run
```

### 7.10 创建后端系统服务

创建 `/etc/systemd/system/order-food.service`：

```ini
[Unit]
Description=Order Food Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=www-data
Group=www-data
WorkingDirectory=/opt/order-food/backend
ExecStart=/usr/bin/java -Xms256m -Xmx512m \
    -jar /opt/order-food/backend/order-food-backend-1.0.0.jar \
    --spring.profiles.active=prod \
    --spring.config.additional-location=file:/opt/order-food/config/
ExecStop=/bin/kill -TERM $MAINPID
Restart=always
RestartSec=10
StandardOutput=append:/opt/order-food/logs/stdout.log
StandardError=append:/opt/order-food/logs/stderr.log

[Install]
WantedBy=multi-user.target
```

启动并设置开机自启：

```bash
sudo systemctl daemon-reload
sudo systemctl start order-food
sudo systemctl enable order-food

# 查看状态
sudo systemctl status order-food

# 查看日志
sudo journalctl -u order-food -f
```

### 7.11 部署验证

```bash
# 1. 检查后端健康
curl http://localhost:8080/api/h5/categories

# 2. 检查 Nginx 代理
curl http://localhost/api/h5/categories

# 3. 浏览器访问
# H5: http://h5.your-domain.com
# Admin: http://admin.your-domain.com (admin / admin123)
```

---

## 8. Docker Compose 部署（可选）

> 适合使用 Docker 的团队，一键编排所有服务。

### 8.1 创建 Dockerfile

**后端 `backend/Dockerfile`**：

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/order-food-backend-1.0.0.jar app.jar

RUN mkdir -p /app/uploads /app/logs

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar", \
    "--spring.profiles.active=prod", \
    "--spring.config.additional-location=file:/app/config/"]
```

**H5 前端 `frontend-h5/Dockerfile`**：

```dockerfile
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**Admin 前端 `frontend-admin/Dockerfile`**：

```dockerfile
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### 8.2 创建 docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: order-food-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD:-changeme}
      MYSQL_DATABASE: order_food
      MYSQL_CHARSET: utf8mb4
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/src/main/resources/db/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./backend/src/main/resources/db/data.sql:/docker-entrypoint-initdb.d/02-data.sql
      - ./backend/src/main/resources/db/migration_taste_config.sql:/docker-entrypoint-initdb.d/03-taste.sql
      - ./backend/src/main/resources/db/migration_user_balance.sql:/docker-entrypoint-initdb.d/04-balance.sql
    restart: always
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: order-food-backend
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/order_food?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${DB_ROOT_PASSWORD:-changeme}
    ports:
      - "8080:8080"
    volumes:
      - uploads_data:/app/uploads
      - ./logs:/app/logs
    restart: always

  h5:
    build: ./frontend-h5
    container_name: order-food-h5
    ports:
      - "8081:80"
    restart: always

  admin:
    build: ./frontend-admin
    container_name: order-food-admin
    ports:
      - "8082:80"
    restart: always

  nginx:
    image: nginx:alpine
    container_name: order-food-nginx
    depends_on:
      - backend
      - h5
      - admin
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf
    restart: always

volumes:
  mysql_data:
  uploads_data:
```

### 8.3 启动

```bash
# 创建 .env 文件
echo "DB_ROOT_PASSWORD=your_secure_password" > .env

# 构建并启动
docker compose up -d --build

# 查看状态
docker compose ps

# 查看后端日志
docker compose logs -f backend
```

---

## 9. 配置项说明

### 9.1 后端配置

| 配置项 | 位置 | 默认值 | 说明 |
|--------|------|--------|------|
| 服务端口 | `server.port` | 8080 | 后端 API 端口 |
| 数据库 URL | `spring.datasource.url` | localhost:3306/order_food | MySQL 连接地址 |
| 数据库用户名 | `spring.datasource.username` | root | MySQL 用户名 |
| 数据库密码 | `spring.datasource.password` | 123456 | MySQL 密码（**生产必须改**） |
| 上传大小限制 | `spring.servlet.multipart.max-file-size` | 10MB | 单文件上传限制 |
| JWT 密钥 | `jwt.secret` | restaurant-order-food-jwt-secret-key-2024 | JWT 签名密钥（**生产必须改**） |
| JWT 过期时间 | `jwt.expiration` | 24 | 单位：小时 |
| 文件上传路径 | `file.upload-path` | 项目目录/uploads | 上传文件存储目录 |
| 允许的图片类型 | `file.allowed-types` | jpeg,png,gif,webp | 上传文件 MIME 白名单 |

### 9.2 前端环境变量

| 文件 | 变量 | 值 | 说明 |
|------|------|----|------|
| `.env.development` | `VITE_API_BASE_URL` | `http://localhost:8080` | 开发环境 API 地址 |
| `.env.production` | `VITE_API_BASE_URL` | `/` | 生产环境同源访问（走 Nginx 反代） |

### 9.3 Vite 开发代理

开发环境下，两个前端项目均配置了 Vite proxy：

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
  '/uploads': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
}
```

> 生产环境不需要 Vite proxy，由 Nginx 反代处理。

### 9.4 生产环境必须修改的配置

| 配置项 | 原因 | 风险等级 |
|--------|------|---------|
| 数据库密码 | 默认 `123456` 极不安全 | 🔴 高危 |
| JWT 密钥 | 默认密钥可被伪造 Token | 🔴 高危 |
| `spring.profiles.active` | 改为 `prod` | 🟡 中等 |
| 文件上传路径 | 改为服务器实际路径 | 🟡 中等 |
| SQL 日志 | 生产环境建议关闭 | 🟢 低 |
| 管理员密码 | 首次登录后立即修改 | 🔴 高危 |

---

## 10. 常见问题排查

### Q1: 后端启动报数据库连接失败

```
Caused by: java.sql.SQLException: Access denied for user 'root'@'localhost'
```

**解决**：检查 `application.yml` 和 `application-dev.yml` 中的数据库用户名密码是否正确。注意 `application-dev.yml` 会覆盖 `application.yml` 的同名字段。

### Q2: 前端图片不显示（404）

**原因**：Vite 开发服务器未代理 `/uploads` 路径，或 Nginx 未配置图片代理。

**解决**：
- 开发环境：确认 `vite.config.ts` 中 proxy 配置包含 `/uploads`
- 生产环境：确认 Nginx 配置了 `location /uploads/` 反代到后端

### Q3: 上传图片报"创建目录失败"

**原因**：上传路径无写入权限。

**解决**：
```bash
# Linux 服务器
sudo mkdir -p /opt/order-food/uploads
sudo chown -R www-data:www-data /opt/order-food/uploads
```

### Q4: 后端 JAR 启动后立即退出

**排查**：
```bash
# 查看详细日志
java -jar order-food-backend-1.0.0.jar --debug

# 常见原因：
# 1. 端口 8080 被占用 → lsof -i:8080
# 2. 数据库未启动 → systemctl status mysql
# 3. 配置文件路径错误 → 检查 --spring.config.additional-location
```

### Q5: 前端路由刷新 404

**原因**：Nginx 未配置 Vue Router history 模式的 fallback。

**解决**：确认 Nginx 配置中有：
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### Q6: 滑块验证码不显示

**原因**：验证码接口需要后端正常运行，检查 `/api/h5/captcha/slider` 是否可访问。

**排查**：
```bash
curl http://localhost:8080/api/h5/captcha/slider
# 应返回 JSON，包含 backgroundImage 和 sliderImage
```

### Q7: Docker Compose 中 MySQL 初始化脚本未执行

**原因**：MySQL 容器已存在数据卷，不会重新执行初始化脚本。

**解决**：
```bash
# 清除数据卷后重新启动
docker compose down -v
docker compose up -d --build
```

### Q8: 生产环境如何查看后端日志

```bash
# systemd 方式
sudo journalctl -u order-food -f          # 实时日志
sudo journalctl -u order-food --since today  # 今日日志

# 或直接查看日志文件
tail -f /opt/order-food/logs/app.log
```

---

## 附录：快速部署 Checklist

- [ ] JDK 21 已安装
- [ ] MySQL 8.0 已安装并启动
- [ ] 4 个 SQL 脚本已按顺序执行
- [ ] 后端 JAR 已上传到服务器
- [ ] `application-prod.yml` 已创建（数据库密码、JWT 密钥已修改）
- [ ] H5 前端 dist 已上传
- [ ] Admin 前端 dist 已上传
- [ ] Nginx 已安装
- [ ] Nginx 配置文件已创建并测试通过
- [ ] systemd 服务已创建并启动
- [ ] uploads 目录已创建并有写入权限
- [ ] HTTPS 证书已配置（推荐）
- [ ] 管理员密码已修改
- [ ] 防火墙仅开放 80/443 端口（8080 不对外暴露）

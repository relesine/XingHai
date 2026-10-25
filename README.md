# 星海 XingHai · 视频社交电商全栈平台

> 面向内容创作者的「短视频 + 直播 + 电商」一体化全栈项目，包含用户端、创作者中心、管理后台与后端服务四个工程。

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.4-42B883?logo=vue.js&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![JDK](https://img.shields.io/badge/JDK-17-orange)
![Tests](https://img.shields.io/badge/tests-18%20passed-brightgreen)

---

## 目录

- [一、项目简介](#一项目简介)
- [二、界面预览](#二界面预览)
- [三、功能清单](#三功能清单)
- [四、技术架构](#四技术架构)
- [五、快速开始](#五快速开始)
- [六、配置说明](#六配置说明)
- [七、接口规范](#七接口规范)
- [八、安全与工程化](#八安全与工程化)
- [九、测试与质量](#九测试与质量)
- [十、生产部署](#十生产部署)
- [十一、文档与账号信息](#十一文档与账号信息)
- [十二、常见问题](#十二常见问题)
- [十三、项目说明与路线图](#十三项目说明与路线图)

---

## 一、项目简介

星海（XingHai）是一个前后端分离的单体全栈项目，围绕「内容消费 — 内容创作 — 平台运营」构建完整闭环：

- **用户端**：刷视频、点赞收藏评论、关注创作者、私信聊天、观看直播、进入店铺下单。
- **创作者中心**：查看数据看板、上传与管理作品、经营店铺与商品、处理订单、管理直播。
- **管理后台**：平台数据大盘、视频审核、举报处理、用户与角色权限、公告与广告、直播管理。
- **后端服务**：统一 REST API，提供 JWT 鉴权、RBAC 权限、文件上传、Redis 缓存与限流。

项目采用「不引入重型中间件、单机即可跑通」的设计，适合作为全栈工程实践、毕业设计或企业级开发规范的参考实现。

---

## 二、界面预览

### 用户端

| 首页推荐流 | 视频播放页 |
|---|---|
| ![用户端首页](docs/images/user-home.png) | ![视频播放页](docs/images/user-video.png) |

| 直播广场 | 消息中心 |
|---|---|
| ![直播广场](docs/images/user-live.png) | ![消息中心](docs/images/user-messages.png) |

### 创作者中心

| 数据看板 | 作品管理 |
|---|---|
| ![创作者看板](docs/images/creator-dashboard.png) | ![作品管理](docs/images/creator-videos.png) |

### 管理后台

| 数据大盘 | 视频审核 |
|---|---|
| ![管理端大盘](docs/images/admin-dashboard.png) | ![视频审核](docs/images/admin-audit.png) |

> 截图来自本地实际运行环境（1440×900），演示数据见 `database/xinghai_data.sql`。

---

## 三、功能清单

### 3.1 用户端（`web-user`，端口 5173）

| 模块 | 能力 |
|---|---|
| 内容消费 | 推荐 / 关注 / 同城信息流、分类与标签筛选、关键词搜索、视频详情播放 |
| 互动 | 点赞、收藏、评论、分享、关注创作者 |
| 社交 | 私信会话列表、聊天窗口、消息轮询、未读提示 |
| 直播 | 直播间列表、直播状态与在线人数、进入直播间 |
| 电商 | 商城首页、店铺主页、商品详情、下单、订单列表 |
| 个人 | 注册登录、个人主页、资料编辑、投稿上传、我的收藏与关注 |

### 3.2 创作者中心（`web-creator`，端口 5174）

| 模块 | 能力 |
|---|---|
| 数据看板 | 累计播放 / 获赞 / 粉丝 / 作品 / 销售额、近 7 天趋势 |
| 作品管理 | 作品列表、播放点赞评论数据、审核状态与驳回原因、删除 |
| 店铺经营 | 店铺资料维护、商品上下架与编辑、库存与价格 |
| 订单 | 订单列表、状态流转、发货 / 完成处理 |
| 直播 | 开播 / 关播、直播记录 |

### 3.3 管理后台（`web-admin`，端口 5175）

| 模块 | 能力 |
|---|---|
| 数据大盘 | 用户 / 创作者 / 视频 / 待审核 / 订单 / 成交额 / 举报指标、7 天趋势、分类分布 |
| 内容治理 | 视频审核（通过 / 驳回）、举报处理 |
| 用户与权限 | 用户列表、状态管理、角色分配、角色权限配置 |
| 运营 | 公告管理、广告位管理、直播管理 |

### 3.4 后端服务（`backend`，端口 8080）

- 21 张数据表、94 个 REST 接口，覆盖认证、视频、互动、评论、消息、直播、电商、运营与统计。
- JWT 无状态鉴权 + `@RequireRole` 注解式 RBAC；ThreadLocal 用户上下文，请求结束自动清理。
- 统一响应结构、全局异常处理、参数校验、分页封装。
- 文件上传（头像 / 封面 / 视频），扩展名白名单 + 文件头魔数双重校验。
- Redis 缓存热点数据（分类、标签、公告等）与登录注册限流。

---

## 四、技术架构

### 4.1 整体架构

```
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ 用户端 :5173 │   │ 创作者端:5174│   │ 管理端  :5175│
└──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       └──────────────────┼──────────────────┘
                          │  /api、/upload
                    Nginx（生产）/ Vite Proxy（开发）
                          ▼
                 Spring Boot API :8080
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
    MySQL 8.0          Redis            uploads
  （21 张业务表）   （缓存 / 限流）     （本地文件）
```

### 4.2 技术栈

| 层次 | 技术 | 版本 | 用途 |
|---|---|---|---|
| 后端框架 | Spring Boot | 2.7.18 | Web、IOC、自动配置 |
| 语言 / 构建 | JDK / Maven | 17 / 3.9 | 编译与依赖管理 |
| 持久层 | MyBatis-Plus | 3.5.3.1 | ORM、分页、逻辑删除 |
| 数据库 | MySQL | 8.0 | 业务数据存储 |
| 缓存 | Redis | 7.x | 热点缓存、限流计数 |
| 鉴权 | JJWT / BCrypt | 0.11.5 | Token 签发校验、密码哈希 |
| 前端框架 | Vue | 3.4 | 组合式 API |
| 构建 | Vite | 5.4 | 开发服务器与生产构建 |
| UI | Element Plus | 2.9 | 组件库（深度定制主题） |
| 状态 / 路由 | Pinia / Vue Router | 2.3 / 4.4 | 登录态与页面路由（Hash 模式） |
| 图表 | ECharts | 5.5 | 数据大盘与看板 |
| 请求 | Axios | 1.7 | 统一拦截器与错误处理 |

### 4.3 目录结构

```
XingHai/
├── backend/                 # Spring Boot 后端服务
│   ├── src/main/java/com/xinghai/
│   │   ├── common/          # 统一响应、状态码
│   │   ├── config/          # WebMvc、Redis、MyBatis-Plus、安全响应头
│   │   ├── controller/      # 19 组 REST 控制器
│   │   ├── dto/ vo/         # 请求与响应模型
│   │   ├── entity/ mapper/  # 数据实体与 Mapper
│   │   ├── exception/       # 业务异常与全局异常处理
│   │   ├── security/        # JWT 拦截器、角色注解、用户上下文
│   │   ├── service/         # 业务服务
│   │   └── util/            # JWT、Redis 工具
│   ├── src/main/resources/  # 分环境配置、Logback
│   ├── src/test/java/       # 单元测试 + 接口集成测试
│   ├── Dockerfile
│   └── pom.xml
├── web-user/                # 用户端 Vue3 工程
├── web-creator/             # 创作者中心 Vue3 工程
├── web-admin/               # 管理后台 Vue3 工程
├── database/                # 建表、种子数据、索引脚本
├── deploy/                  # Nginx 生产配置
├── docs/                    # 项目完整文档与界面截图
├── uploads/                 # 本地上传目录
├── .env.example             # 环境变量示例
├── docker-compose.yml       # MySQL + Redis + 后端编排
└── README.md
```

---

## 五、快速开始

### 5.1 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | 后端编译运行 |
| Maven | 3.8+ | 建议 3.9 |
| Node.js | 18+ | 建议 20，三端前端 |
| MySQL | 8.0 | 连接账号通过环境变量配置 |
| Redis | 6+ | 默认 `localhost:6379`，无密码 |

### 5.2 初始化数据库

```bash
# 在项目根目录执行
mysql -uroot -p -e "source database/xinghai_schema.sql"
mysql -uroot -p -e "source database/xinghai_data.sql"
# 可选：旧库补充复合索引（幂等）
mysql -uroot -p xinghai -e "source database/xinghai_upgrade_indexes.sql"
```

### 5.3 启动后端

```bash
cd backend
mvn spring-boot:run          # 默认 dev 环境，端口 8080
```

健康检查：`GET http://localhost:8080/actuator/health` → `{"status":"UP"}`

### 5.4 启动前端

```bash
cd web-user    && npm install && npm run dev   # http://localhost:5173
cd web-creator && npm install && npm run dev   # http://localhost:5174
cd web-admin   && npm install && npm run dev   # http://localhost:5175
```

Windows 下也可直接双击根目录的 `一键启动星海项目.bat` / `一键停止星海项目.bat`。

### 5.5 账号信息

演示账号与基础设施账号统一归档在 [docs/星海项目完整文档.md](docs/星海项目完整文档.md) 第 13 节，仓库内其他文件与三端登录页均不展示账号密码。

---

## 六、配置说明

配置分为 `application.yml`（公共）+ `application-dev.yml`（开发）+ `application-prod.yml`（生产），均支持环境变量覆盖。

| 变量 | 默认值 | 说明 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | 激活的环境 |
| `DB_URL` | 本地 `xinghai` 库 | JDBC 连接串 |
| `DB_USERNAME` / `DB_PASSWORD` | 见完整文档第 13 节 | 数据库账号 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `localhost` / `6379` / 空 | Redis 连接 |
| `JWT_SECRET` | 内置开发密钥 | **生产必须替换**，≥32 字节随机串 |
| `JWT_EXPIRE` | `604800000` | Token 有效期（毫秒，默认 7 天） |
| `UPLOAD_DIR` | `./uploads` | 上传文件根目录 |
| `CORS_ORIGINS` | 三端本地地址 | 允许跨域来源，逗号分隔 |
| `SERVER_PORT` | `8080` | 服务端口 |
| `LOG_DIR` | `./logs` | 生产日志目录 |

前端通过 `.env.local` 配置外链地址：`VITE_USER_URL`、`VITE_CREATOR_URL`、`VITE_ADMIN_URL`，三端各提供 `.env.example`。

---

## 七、接口规范

### 7.1 统一响应

```json
{
  "code": 200,
  "message": "登录成功",
  "data": { "token": "eyJhbGciOi...", "userId": 1006, "nickname": "小鱼干", "roles": ["USER"] }
}
```

### 7.2 鉴权

需登录的接口在请求头携带令牌：

```
Authorization: Bearer <token>
```

白名单接口（信息流、详情、分类、搜索、公告、店铺等）可匿名访问；携带合法令牌时会额外返回 `liked`、`collected`、`followed` 等用户态字段。

### 7.3 分页

请求参数 `pageNum`（默认 1）、`pageSize`（默认 10，最大 100），响应 `data` 为 `{ list, total, pageNum, pageSize }`。

### 7.4 状态码

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 参数错误 / 请求体不可读 |
| 401 | 未登录或令牌失效 |
| 403 | 无访问权限（角色不足） |
| 404 | 资源不存在 |
| 429 | 操作过于频繁（触发限流） |
| 500 | 系统异常（不返回堆栈） |
| 1001 | 用户名或密码错误 |

---

## 八、安全与工程化

### 8.1 安全机制

- **认证授权**：BCrypt 密码哈希、JWT 签发校验、`@RequireRole` 精确角色匹配（避免子串误判）、禁用用户即时失效。
- **接口防护**：Redis 固定窗口限流（登录 10 次/60 秒、注册 5 次/300 秒），按真实客户端 IP 计数，Redis 异常自动降级。
- **上传防护**：扩展名白名单 + 文件头魔数校验（jpg/png/gif/webp/mp4/mov/mkv/webm/avi），禁用 SVG 防存储型 XSS，路径穿越校验。
- **响应安全头**：`X-Content-Type-Options`、`X-Frame-Options`、`Referrer-Policy`、`X-XSS-Protection`、`Permissions-Policy`，`/api/` 响应 `no-store`。
- **数据边界**：全量参数校验、分页上限 100、逻辑删除、订单归属校验（防 IDOR）。
- **配置安全**：生产环境数据库、Redis、JWT、CORS 全部强制环境变量注入，错误响应不泄露堆栈。

### 8.2 工程化

- 环境分层：dev / prod 独立配置，生产关闭 SQL 日志、开启连接池与优雅停机。
- 日志：Logback 控制台 + 滚动文件（单文件 50MB、保留 30 天、总量上限 2GB）。
- 可观测：Actuator `health` / `info` / `probes`，Docker `HEALTHCHECK`。
- 容器化：多阶段构建 Dockerfile + `docker-compose.yml`（MySQL + Redis + Backend）。
- 规范：`.editorconfig`、`.gitignore`、`.env.example`、Nginx 生产配置。

---

## 九、测试与质量

```bash
cd backend && mvn -B test
```

| 测试类 | 覆盖内容 | 用例数 |
|---|---|---|
| `JwtUtilTest` | 签发解析、过期、篡改 | 3 |
| `RateLimitServiceTest` | 窗口计数、超限 429、Redis 降级 | 3 |
| `SecurityHeadersFilterTest` | 接口安全头、静态资源缓存策略 | 2 |
| `ApiIntegrationTest` | 登录/失败/校验、401、RBAC 403、分页上限、匿名访问、安全头 | 10 |

结果：`Tests run: 18, Failures: 0, Errors: 0`。

构建验证：`backend` 编译通过；`web-user` / `web-creator` / `web-admin` 均 `npm run build` 通过。

---

## 十、生产部署

### 10.1 Docker Compose

```bash
cp .env.example .env          # 修改数据库密码、JWT_SECRET 等
docker compose up -d --build
```

启动 MySQL、Redis 与后端；上传文件与日志分别挂载到 `uploads/`、`logs/`。

### 10.2 Nginx

将三端 `dist` 放到服务器目录，使用 [deploy/nginx.conf](deploy/nginx.conf) 托管静态资源并反代 `/api`、`/upload`：

```bash
npm run build             # 在三个前端工程中分别执行
nginx -t && nginx -s reload
```

生产环境务必：配置 HTTPS、替换 `JWT_SECRET`、关闭调试日志、限制管理后台访问来源。

---

## 十一、文档与账号信息

| 文档 | 内容 |
|---|---|
| [星海项目完整文档](docs/星海项目完整文档.md) | 项目唯一总文档：概述、界面预览、功能、架构、流程、数据库、94 个接口、安全、部署、测试、账号信息、版本记录与附录 |

- 账号与密码统一归档在完整文档第 13 节。
- 三端登录页不包含任何账号密码提示。
- `README.md` 仅作为仓库入口，详细内容以完整文档为准。

---

## 十二、常见问题

| 问题 | 排查方向 |
|---|---|
| 后端启动报数据库连接失败 | 确认 MySQL 已启动、`xinghai` 库已导入、账号密码与 `DB_PASSWORD` 一致 |
| Redis 连接超时 | 确认 Redis 运行在 6379；未启动时缓存失效但基础接口仍可用 |
| 前端 404 / 空白 | 使用 Hash 路由，访问形如 `http://localhost:5173/#/`；确认 Vite 代理指向 8080 |
| 上传失败 | 检查 `UPLOAD_DIR` 目录存在且可写、文件类型在允许范围、大小未超限 |
| 登录提示操作频繁 | 触发登录限流，等待 60 秒或更换 IP |
| 第一次 `npm install` 很慢 | 配置国内镜像：`npm config set registry https://registry.npmmirror.com` |

---

## 十三、项目说明与路线图

**定位**：学习与工程实践项目。支付、直播推流、短信等为模拟实现，生产接入需替换为真实服务。

**后续规划**：

- 文件存储迁移对象存储（MinIO / OSS），支持 CDN 加速。
- 私信由轮询升级为 WebSocket 长连接。
- 搜索接入 Elasticsearch，支持分词与高亮。
- 增加接口级压测与前端 E2E 自动化测试。

**许可**：本仓库未附带开源许可证，默认仅供学习交流使用；第三方依赖遵循其各自许可证。

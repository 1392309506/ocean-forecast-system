# Ocean Forecast System (Refactored)

本项目已重构为前后端分离架构，满足科研应用场景下的可维护性与工程规范要求。

## 1. 新架构说明

```
ocean-forecast-system/
├── backend/                      # Spring Boot 后端（MVC + 分层）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ocean/forecast/
│       │   ├── controller/       # API 接口层
│       │   ├── service/          # 业务接口定义
│       │   │   └── impl/         # 业务实现
│       │   ├── mapper/           # MyBatis Mapper 接口
│       │   ├── entity/           # 实体类
│       │   ├── utils/            # 工具类
│       │   └── webconfig/        # Web配置（CORS、异常处理）
│       └── resources/
│           ├── application.yml
│           ├── schema.sql         # 数据库建表脚本
│           └── data.sql           # 初始化样例数据
├── frontend/                     # Vue3 前端（Vite + Router）
│   ├── package.json
│   └── src/
│       ├── api/                  # 接口请求层
│       ├── router/               # 路由定义
│       ├── views/                # 页面视图
│       ├── App.vue
│       └── styles.css
└── README.md
```

## 2. 后端工业级分层流程

前端请求链路如下：

`Frontend -> Controller -> Service(interface) -> ServiceImpl -> Mapper -> Entity`

说明：
- `entity`：统一承载业务实体与 API 响应对象
- `mapper`：MyBatis 数据访问接口，直接对接数据库表
- `service`：先定义接口，再由 `impl` 完成实现
- `webconfig`：集中配置 CORS、全局异常处理
- `utils`：沉淀通用算法/数据生成逻辑

## 3. 前端路由与页面

前端采用 Vue Router，路由清晰：
- `/`：系统概览（服务状态、核心指标）
- `/map`：空间分布数据采样展示
- `/timeseries`：时间序列分析（含趋势线）
- `/forecast`：预报解译（时效与置信信息）

界面风格：
- 采用科研报告风格配色（低饱和蓝绿系）
- 使用信息密度友好的卡片式布局
- 兼容桌面与移动端

## 4. 快速启动

### 4.1 启动后端

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

当前默认使用 H2 内存数据库（便于本地快速验证），可在 `backend/src/main/resources/application.yml` 切换为 MySQL。

### 4.2 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`

## 5. 后端 API

基础路径：`/api/ocean`

- `GET /health`：健康检查
- `GET /grid?dataType=temperature&timestamp=...`：格点数据
- `GET /timeseries?longitude=120.5&latitude=30.5&dataType=temperature`：时间序列
- `GET /forecast?region=东海&forecastHours=72`：预报结果
- `GET /observation?longitude=120.5&latitude=30.5`：单点观测

## 6. 重构结果摘要

本次重构完成以下目标：
- 完成前后端拆分并置于独立目录
- 后端重构为工业级分层结构（controller/service/mapper/entity）
- mapper 已切换为 MyBatis + 数据库脚本初始化
- 前端使用 Vue + Router 重建页面与 API 访问层
- 文档改写为分离式项目使用指南

## 7. 后续可扩展方向

- 将 `mapper` 层替换为 MyBatis-Plus 或 Spring Data JPA
- 接入真实海洋观测数据库（MySQL/PostgreSQL）
- 引入鉴权（JWT/OAuth2）与接口限流
- 增加自动化测试与 CI/CD 流程

# QUICKSTART

## 1. 环境准备

- Java 17+
- Maven 3.8+
- Node.js 18+
- npm 9+

## 2. 启动后端（Spring Boot）

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

后端默认端口：`8080`

## 3. 启动前端（Vue3）

```bash
cd frontend
npm install
npm run dev
```

前端默认端口：`5173`

## 4. 访问系统

- 前端：`http://localhost:5173`
- 后端健康检查：`http://localhost:8080/api/ocean/health`

## 5. 目录定位

- 后端代码：`backend/`
- 前端代码：`frontend/`
- 主文档：`README.md`

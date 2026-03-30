# QUICK REFERENCE

## 后端分层

- `controller`：接口入口、参数接收、响应返回
- `service`：业务接口定义
- `service/impl`：业务实现
- `mapper`：数据访问抽象
- `mapper/impl`：数据访问实现
- `entity`：实体类
- `utils`：工具逻辑
- `webconfig`：跨域与异常处理

## 前端分层

- `src/router`：路由定义
- `src/views`：页面级组件
- `src/api`：接口调用
- `src/styles.css`：全局科研风格主题

## 常用命令

### 后端

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

### 前端

```bash
cd frontend
npm install
npm run dev
npm run build
```

## API 速查

```bash
GET /api/ocean/health
GET /api/ocean/grid?dataType=temperature
GET /api/ocean/timeseries?longitude=120.5&latitude=30.5&dataType=temperature
GET /api/ocean/forecast?region=东海&forecastHours=72
GET /api/ocean/observation?longitude=120.5&latitude=30.5
```

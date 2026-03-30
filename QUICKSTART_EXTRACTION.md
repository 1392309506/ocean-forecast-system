# 内网数据提取 QUICKSTART（重构说明）

当前仓库已完成前后端分离重构：
- 新业务后端：`backend/`
- 新前端：`frontend/`

内网数据提取能力属于历史扩展模块，若继续使用请参考旧实现（原 `src/` 下的提取组件）。

## 建议迁移策略

1. 将旧提取组件迁移到 `backend` 的 `mapper`/`service` 分层内。
2. 统一通过 `controller -> service -> mapper` 暴露提取 API。
3. 将配置项集中到 `backend/src/main/resources/application.yml`。

## 立即可用的主系统验证

```bash
# 后端
cd backend
mvn spring-boot:run

# 前端
cd ../frontend
npm install
npm run dev
```

更多信息请查看：
- `README.md`
- `QUICK_REFERENCE.md`

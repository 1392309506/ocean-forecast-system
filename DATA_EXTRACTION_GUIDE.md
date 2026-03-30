# 数据提取模块迁移指南（面向重构后架构）

## 1. 背景

本项目已完成前后端分离：
- `backend/`：Spring Boot 后端主工程
- `frontend/`：Vue 前端工程

历史版本中的内网提取组件（SFTP/FTP/DB/FILE）尚未并入新的 `backend` 分层结构。

## 2. 目标结构（推荐）

将数据提取能力按如下方式迁移：

- `controller`: `DataExtractionController`
- `service`: `DataExtractionService`（interface）
- `service/impl`: `DataExtractionServiceImpl`
- `mapper`: `DataExtractionMapper`（含协议/数据库访问抽象）
- `entity`: 提取请求与响应实体
- `utils`: 文件转换、CSV 导出等工具

## 3. 迁移步骤

1. 把旧提取逻辑中的协议连接代码下沉到 `mapper`。
2. 在 `service` 层实现业务编排（批量下载、异常重试、导出）。
3. 在 `controller` 层保留 REST API，统一返回 `ApiResponse<T>`。
4. 在 `webconfig` 添加访问控制与全局异常处理策略。
5. 将配置统一放入 `backend/src/main/resources/application.yml`。

## 4. 验证清单

- API 可访问：`/api/data/extraction/**`
- 错误处理统一：400/500 结构化响应
- 文件提取链路可用：连接、下载、导出
- 日志可追踪：请求参数、耗时、异常栈

## 5. 备注

如果你希望，我可以在下一步继续把旧提取模块完整迁移到 `backend` 目录，并补齐对应前端管理页与接口文档。

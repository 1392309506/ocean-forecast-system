# Backend

Spring Boot 后端工程，采用工业级分层结构：
- controller
- service + service/impl
- mapper (MyBatis)
- entity
- utils
- webconfig

默认数据源：H2 内存数据库（启动时自动执行 `schema.sql` 和 `data.sql`）。

## 运行

```bash
mvn clean package
mvn spring-boot:run
```

默认端口：`8080`

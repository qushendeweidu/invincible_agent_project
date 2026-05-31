# 🤖 Laodeng AI Agent

基于 `Spring Boot 3.5 + Spring AI + Alibaba Agent Framework` 的多智能体协作平台，支持：

- 多专家 Agent 路由（搜索 / 文档 / GitHub / 编程 / 设计 / 情感陪伴 / 命令行）
- MCP 工具生态接入（stdio 模式）
- RAG 检索增强（Elasticsearch Vector Store）
- 长短期记忆（MongoDB + Redis/Redisson）
- 流式 SSE 响应 + TTS 语音合成（阿里云 DashScope）+ MinIO 存储
- 动态模型注册与热更新（数据库驱动）

---

## 目录

- [1. 项目定位](#1-项目定位)
- [2. 核心能力](#2-核心能力)
- [3. 系统架构](#3-系统架构)
- [4. 技术栈](#4-技术栈)
- [5. 项目结构](#5-项目结构)
- [6. 快速开始](#6-快速开始)
- [7. 配置说明](#7-配置说明)
- [8. MCP 与 Skills](#8-mcp-与-skills)
- [9. API 概览](#9-api-概览)
- [10. 流式事件与 TTS](#10-流式事件与-tts)
- [11. 开源安全建议](#11-开源安全建议)

---

## 1. 项目定位

本项目是一个偏“生产化”的智能体后端服务，提供统一 API 给前端或其他系统调用。当前后端以 `/api` 作为统一上下文路径，覆盖以下场景：

- 情感陪伴聊天（`LoveApp`）
- 多智能体任务执行（`ReactAgentApp` + `SkillsAgentHook`）
- 文档上传、解析、RAG 入库与检索
- 图片理解
- 模型配置动态管理
- 用户认证授权（Sa-Token）

---

## 2. 核心能力

### 2.1 多智能体协作

`ReactAgentApp` 会构建 Supervisor + 多专家子智能体：

- `search_agent`：搜索 + RAG 检索
- `document_agent`：Office 文档读取
- `git_agent`：GitHub 仓库操作
- `code_agent`：编程与调试
- `design_agent`：UI/UX 与视觉设计
- `mood_agent`：情感陪伴角色
- `cli_agent`：命令行与 SSH 工具

Supervisor 通过 `SkillsAgentHook` 根据 skill 进行工具渐进式披露与路由。

### 2.2 动态模型注册

`DynamicChatModelRegistry` 从数据库加载启用模型并构建 `ChatModel`，支持运行期更新（事件驱动）。

### 2.3 流式输出 + 指标监控

`ReactAgentApp.execute()` 以 `Flux<StreamEvent>` 输出：`start / thinking / tool_call / tool_result / text / done`。
同时将工具调用耗时与 Agent 执行指标写入 InfluxDB。

### 2.4 TTS + MinIO

`ReactAgentServiceImpl` 在流式文本汇总后触发 `TTSUtils` 合成语音，上传 MinIO 并返回预签名 URL。

---

## 3. 系统架构

```text
Client
  -> /api/* Controller (Chat/User/File/Health)
  -> Service Factory (LOVE/IMAGE/REACT)
  -> ReactAgentApp (Supervisor)
       -> SkillsAgentHook + Skill Registry
       -> Sub Agents (search/doc/git/code/design/mood/cli)
       -> MCP Tools + Local Tools
  -> StreamEvent (SSE)
  -> Optional TTS (DashScope) -> MinIO URL

Data/Infra:
- MyS:L (业务配置)
- MongoDB (长时对话记忆)
- Redis/Redisson (会话/短时记忆与图状态)
- Elasticsearch (向量检索)
- MinIO (文件/音频对象存储)
- InfluxDB (运行时指标)
```

---

## 4. 技术栈

- Java 21
- Spring Boot 3.5.10
- Spring AI 1.1.2
- Spring AI Alibaba Agent Framework 1.1.2.0
- MCP Client/Server (WebFlux)
- Sa-Token 1.44.0
- MyBatis-Plus + JPA
- Redis + Redisson
- MongoDB
- Elasticsearch Vector Store
- MinIO
- InfluxDB?

---

## 5. 项目结构

```text
src/main/java/com/laodeng/laodengaiagent
├─ app/                 # LoveApp, ImageApp, ReactAgentApp
├─ controller/          # chat, user, file_load, health
├─ service/             # 业务服务与实现
├─ tool/                # 本地工具与 react tools
├─ config/              # OpenAI/MCP/MinIO/VectorStore/Async 等配置
├─ register/            # DynamicChatModelRegistry
├─ advisor/             # Prompt/Request 增强
├─ aop/                 # 对话历史切面
└─ utils/               # TTS、文本切分、文件处理等工具

src/main/resources
├─ application.yml
├─ prompt/              # 系统提示词
├─ mcp/mcp-servers.json
└─ skills/              # 17 个 skills
```

---

## 6. 快速开始

### 6.1 环境要求

- JDK 21
- Maven 3.9+
- Node.js（用于 npx 启动 MCP server）
- MySQL / Redis / MongoDB / Elasticsearch / MinIO / InfluxDB

### 6.2 必要环境变量

项目通过 `spring.config.import: optional:file:.env[.properties]` 读取本地 `.env`。

至少配置：

```bash
MYSQL_PASSWORD=...
MONGODB_PASSWORD=...
ELASTICSEARCH_PASSWORD=...
MINIO_ACCESS_KEY=...
MINIO_SECRET_KEY=...
INFLUXDB_TOKEN=...
ALIYUN_TTS_KEY=...
GITHUB_PERSONAL_ACCESS_TOKEN=...   # 使用 github MCP 时
```

### 6.3 启动

```bash
mvn spring-boot:run
```

默认访问：

- 服务地址：`http://localhost:8888/api`
- Swagger UI：`http://localhost:8888/api/swagger-ui.html`

---

## 7. 配置说明

核心配置文件：`src/main/resources/application.yml`

关键项：

- `spring.ai.mcp.client.*`：MCP client 配置（ASYNC + stdio）
- `spring.ai.openai.*`：默认模型接入（兼容本地 Ollama/OpenAI 风格网关）
- `spring.data.mongodb.*`：长时记忆
- `spring.data.redis.*`：缓存与会话
- `spring.elasticsearch.*`：向量检索
- `minio.*`：对象存储
- `influxdb.*`：运行指标
- `server.servlet.context-path=/api`

---

## 8. MCP 与 Skills

### 8.1 MCP servers

当前 `mcp/mcp-servers.json` 定义：

- `sequential-thinking`
- `github`
- `bingcn`
- `windows-cli`

### 8.2 Skills

`SkillsConfig` 从 classpath `skills` 目录加载 skills。当前包含 17 个：

- brainstorming
- canvas-design
- code-assist
- doc-coauthoring
- doc-reader
- executing-plans
- github-ops
- mood-chat
- ppt-maker
- skill-creator
- systematic-debugging
- test-driven-development
- theme-factory
- ui-ux-pro-max
- visual-design
- web-search
- writing-plans

---

## 9. API 概览

> 基础前缀：`/api`

### 9.1 用户接口

- `POST /user/register`
- `POST /user/login`
- `POST /user/logout`
- `GET /user/info`
- `POST /user/update`

### 9.2 聊天与模型接口

- `GET /chat_stream`（基础流式）
- `GET /chat`（RAG 对话）
- `GET /react_agent_stream`（多智能体 SSE）
- `GET /image_analyse`
- `POST /add_model_config`
- `PATCH /update_model_config`
- `DELETE /remove_model_config/{id}`
- `GET /list_model_config`
- `GET /get_model_id/{id}`
- `GET /get_ai_history`
- `DELETE /withdraw_message/{messageId}`

### 9.3 文件接口

- `POST /file_load/image`
- `POST /file_load/document`
- `GET /file_load/download`
- `DELETE /file_load/delete`
- `GET /file_load/list`
- `GET /file_load/url`
- `POST /file_load/images`
- `POST /file_load/documents`
- `POST /file_load/add_ragdoc_minio`
- `GET /file_load/download_from_url`

### 9.4 健康检查

- `GET /health`

---

## 10. 流式事件与 TTS

`/react_agent_stream` 返回 `StreamEvent`（SSE）。典型类型：

- `start`：任务开始
- `thinking`：思考中
- `tool_call`：工具调用
- `tool_result`：工具结果
- `text`：模型文本
- `done`：结束
- `error` / `timeout`：异常与超时

当开启 TTS 时，服务会将文本聚合后进行语音合成并回传可访问音频地址（MinIO 预签名 URL）。

---

## 11. 开源安全建议

开源发布前建议执行：

1. 不要提交 `.env`、私钥、生产配置。
2. 对已泄露密钥立即轮换（即使已改 Git 历史也必须轮换）。
3. 为仓库补齐：`LICENSE`、`SECURITY.md`、`CONTRIBUTING.md`。
4. 使用 `gitleaks`/`trufflehog` 做提交前扫描。
5. 将本地基础设施文件（如 `docker-compose.yml`）中的明文凭据改为环境变量注入。

---

## License

当前仓库请补充根目录 `LICENSE` 文件后再正式对外发布（推荐 MIT 或 Apache-2.0）。

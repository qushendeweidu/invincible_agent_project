<div align="center">

# 🤖 Laodeng AI Agent

**基于 Spring AI + 阿里巴巴 Agent Framework 构建的多智能体协作平台**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.2-blue.svg)](https://docs.spring.io/spring-ai/reference/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

一个具备**情感陪伴、智能搜索、文档解析、代码开发、GitHub 协作**等能力的 AI 智能体集群系统。
采用 Supervisor 路由 + 多专家子智能体架构，支持 MCP 工具协议、RAG 检索增强、Redis 持久化记忆、动态模型注册等企业级特性。

[快速开始](#-快速开始) · [系统架构](#-系统架构) · [功能特性](#-功能特性) · [API 文档](#-api-接口) · [配置说明](#-配置说明)

</div>

---

## 📋 目录

- [系统架构](#-系统架构)
- [功能特性](#-功能特性)
- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
- [API 接口](#-api-接口)
- [智能体集群](#-智能体集群)
- [MCP 工具集成](#-mcp-工具集成)
- [记忆系统](#-记忆系统)
- [配置说明](#-配置说明)

---

## 🏗 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端请求                            │
└───────────────┬─────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────┐
│       ChatController          │   REST API 层
│   /chat  /chat_stream         │   SSE 流式 / 同步响应
│   /react_agent                │
│   /image_analyse              │
└───────────────┬───────────────┘
                │
                ▼
┌───────────────────────────────┐
│     AIAppServiceFactory       │   应用工厂 (策略模式)
│  ┌─────┐ ┌─────┐ ┌────────┐  │
│  │LOVE │ │IMAGE│ │ REACT  │  │
│  └──┬──┘ └──┬──┘ └───┬────┘  │
└─────┼───────┼────────┼───────┘
      │       │        │
      ▼       ▼        ▼
┌──────────────────────────────────────────────────────────────┐
│                   ReactAgentApp (Supervisor)                  │
│                                                              │
│   ┌─────────┐  ┌──────────┐  ┌─────────┐  ┌──────────────┐  │
│   │ mood    │  │ search   │  │ code    │  │ document     │  │
│   │ _agent  │  │ _agent   │  │ _agent  │  │ _agent       │  │
│   └─────────┘  └──────────┘  └─────────┘  └──────────────┘  │
│   ┌─────────┐  ┌──────────┐  ┌──────────────────────────┐   │
│   │ git     │  │ design   │  │ presentation_agent       │   │
│   │ _agent  │  │ _agent   │  └──────────────────────────┘   │
│   └─────────┘  └──────────┘                                  │
└──────────────────────┬───────────────────────────────────────┘
                       │
          ┌────────────┼────────────────┐
          ▼            ▼                ▼
   ┌────────────┐ ┌──────────┐  ┌──────────────┐
   │ MCP Tools  │ │  Redis   │  │ Elasticsearch│
   │ (4 servers)│ │  Memory  │  │ Vector Store │
   └────────────┘ └──────────┘  └──────────────┘
```

### 核心设计理念

- **Supervisor 路由模式**：主智能体不直接回答问题，只负责意图识别和路由分发
- **严格单路由**：每次请求只调用一个最匹配的专家子智能体，节约 token 和并发
- **记忆压缩传递**：Supervisor 将历史对话压缩为「记忆摘要 + 最近 3 轮 + 当前消息」三段式格式传给子智能体
- **子智能体无状态**：所有子智能体不持有记忆，记忆统一由 Supervisor 通过 RedisSaver 管理

---

## ✨ 功能特性

### 🎭 情感陪伴 (mood_agent)
- 拟真人格「苏晚」—— 27 岁产品经理，御姐性格，真实情感表达
- 支持日常闲聊、情感倾诉、情绪安抚
- 基于时间感知的日常生活模拟（早/中/晚/深夜不同反应）
- 长期记忆 + 短期对话的自然衔接

### 🔍 智能搜索 (search_agent)
- 接入 Bing 中文搜索 MCP 工具
- 自动注入当前日期，保证搜索时效性
- 支持网页抓取和内容摘要

### 📄 文档解析 (document_agent)
- 支持 PDF、Word、Excel、PPT 多格式读取
- 基于 Apache Tika + Apache POI 的文档解析引擎
- 集成 MCP Filesystem 工具进行文件管理

### 💻 代码开发 (code_agent)
- 编程辅助、代码调试、技术方案设计
- 集成 Sequential Thinking MCP 工具进行分步推理

### 🐙 GitHub 协作 (git_agent)
- 仓库管理、Issue 跟踪、PR 审查
- 代码搜索、文件读取、分支管理
- 集成官方 GitHub MCP Server

### 🎨 设计 & 演示 (design_agent / presentation_agent)
- UI/UX 设计、海报制作
- PPT 演示文稿自动生成

### 🖼 图像理解 (ImageApp)
- 多模态图片识别与分析
- 基于视觉模型的内容理解

### 🔊 文字转语音 (TTS)
- 集成语音合成功能，支持文本转语音
- 音频文件存储于 MinIO 对象存储
- 支持多种语音风格和语速调节

### 📚 RAG 检索增强
- Elasticsearch 向量数据库存储
- 文档向量化 + 语义检索
- Re-Reading Advisor 提升检索质量

### 📊 运维监控
- InfluxDB 时序数据库存储系统指标
- 实时监控智能体运行状态
- 性能指标可视化分析

---

## 🛠 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **框架** | Spring Boot | 3.5.10 |
| **AI 框架** | Spring AI + Spring AI Alibaba Agent Framework | 1.1.2 |
| **语言** | Java (Virtual Threads) | 21 |
| **关系数据库** | MySQL | 8.0.12 |
| **缓存/记忆** | Redis + Redisson | latest / 3.37.0 |
| **向量数据库** | Elasticsearch | 8.15.3 |
| **文档数据库** | MongoDB | 7-jammy |
| **对象存储** | MinIO | RELEASE.2025-04-22 |
| **时序数据库** | InfluxDB | 2.7-alpine |
| **工具协议** | MCP (Model Context Protocol) | — |
| **文档解析** | Apache Tika + Apache POI | — |
| **权限认证** | Sa-Token | 1.44.0 |
| **ORM** | MyBatis-Plus + JPA | 3.5.14 |
| **API 文档** | Knife4j (OpenAPI 3) | 4.4.0 |
| **容器化** | Docker Compose | — |

---

## 📁 项目结构

```
laodeng-ai-agent/
├── docker-compose.yml                    # Docker 编排 (MySQL/Redis/ES/Kibana/MinIO/MongoDB/InfluxDB)
├── pom.xml                               # Maven 依赖管理
├── src/main/java/com/laodeng/laodengaiagent/
│   ├── LaodengAiAgentApplication.java    # 启动类
│   ├── app/                              # 应用层
│   │   ├── ReactAgentApp.java            # ⭐ 多智能体集群 (Supervisor + 7 子智能体)
│   │   ├── LoveApp.java                  # 聊天应用 (ChatClient + RAG)
│   │   └── ImageApp.java                 # 图像理解应用
│   ├── controller/
│   │   ├── chat/ChatController.java      # 聊天 & 模型管理 API
│   │   ├── load/FileLoadController.java  # 文件上传 API
│   │   ├── user/UserController.java      # 用户管理 API
│   │   └── health/HealthController.java  # 健康检查
│   ├── service/
│   │   ├── AIAppService.java             # 应用接口 + AppType 枚举
│   │   ├── AiModelConfigService.java     # 模型配置服务
│   │   ├── MinioService.java             # MinIO 对象存储服务
│   │   ├── InfluxDBService.java          # InfluxDB 监控服务
│   │   ├── factory/                      # 应用工厂 (策略模式)
│   │   │   └── AIAppServiceFactory.java
│   │   └── impl/                         # 各应用实现
│   ├── config/                           # 配置类
│   │   ├── OpenAiConfig.java             # HTTP 超时配置
│   │   ├── McpToolsConfig.java           # MCP 工具分组配置
│   │   ├── VectorStoreConfig.java        # 向量数据库配置
│   │   ├── MinioConfig.java              # MinIO 配置
│   │   ├── InfluxDBConfig.java           # InfluxDB 配置
│   │   ├── RedissonConfig.java           # Redisson 分布式锁
│   │   └── WebConfig.java                # Web 配置
│   ├── register/
│   │   └── DynamicChatModelRegistry.java # ⭐ 动态模型注册 (热加载)
│   ├── charmemory/
│   │   ├── RedisBaseChatMemory.java      # Redis 对话记忆
│   │   └── FileBaseChatMemory.java       # 文件对话记忆
│   ├── advisor/                          # Spring AI Advisor
│   │   ├── MyLoggerAdvisor.java          # 日志记录
│   │   └── MyReReadingAdvisor.java       # Re-Reading 增强
│   ├── intercetor/
│   │   ├── JWTInterceptor.java           # JWT 鉴权拦截器
│   │   └── DynamicPromptInterceptor.java # 动态提示词拦截器
│   ├── rag/                              # RAG 检索增强
│   │   ├── LoveAppDocumentLoader.java    # 文档加载器
│   │   └── ragTools/                     # RAG 工具集
│   ├── tool/                             # 自定义工具
│   │   ├── FileReadTools.java            # 文件读取工具集
│   │   └── reacttools/                   # 文档解析工具 (PDF/Word/Excel/PPT)
│   ├── domain/                           # 领域模型 (DTO/PO/VO)
│   │   ├── dto/                          # 数据传输对象
│   │   ├── po/                           # 持久化对象
│   │   └── vo/                           # 视图对象
│   ├── common/                           # 通用类 (R 响应体等)
│   ├── utils/                            # 工具类
│   │   ├── TTSUtils.java                 # 文字转语音工具
│   │   ├── ImageUploadUtils.java         # 图片上传工具
│   │   └── NetUtils.java                 # 网络工具
│   └── aop/                              # AOP 切面
│       ├── ChatHistoryAspect.java        # 聊天历史切面
│       └── LogInterceptor.java           # 日志拦截器
├── src/main/resources/
│   ├── application.yml                   # 主配置文件
│   ├── prompt/                           # 提示词模板
│   │   ├── ReactAgentSystemPrompt.st     # Supervisor 路由提示词
│   │   ├── BaseSystemPrompt.st           # 技术子智能体基础提示词
│   │   ├── LoverSystemPrompt.st          # mood_agent 人格提示词
│   │   ├── ChatClientSystemPrompt.st     # ChatClient 提示词
│   │   └── ImageAssistantPrompt.st       # 图像理解提示词
│   ├── mcp/mcp-servers.json              # MCP 服务器配置
│   └── skills/                           # 可扩展技能包
```

---

## 🚀 快速开始

### 环境要求

- **JDK 21+**
- **Node.js 18+** (MCP 工具运行需要)
- **Docker & Docker Compose**
- **Maven 3.8+**

### 1. 启动基础设施

```bash
docker-compose up -d
```

这将启动：
| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 4336 | 业务数据库（用户、模型配置等） |
| Redis | 3089 | 对话记忆存储、分布式锁 |
| Elasticsearch | 19200 | 向量数据库（RAG检索） |
| Kibana | 5601 | ES 可视化管理 |
| MinIO | 9000/9001 | 对象存储（文档、TTS音频） |
| MongoDB | 27017 | 文档存储、聊天记忆 |
| InfluxDB | 8086 | 时序数据库（运维监控指标） |

### 2. 配置模型

在 MySQL `ai_model_config` 表中添加模型配置，或通过 API 动态添加：

```json
{
  "configKey": "zhipu",
  "provider": "zhipu",
  "baseUrl": "https://open.bigmodel.cn/api/paas/v4",
  "apiKey": "your-api-key",
  "modelName": "glm-4.7-flash",
  "temperature": 0.7,
  "enabled": true
}
```

支持任何兼容 OpenAI API 格式的模型提供商（OpenAI / 智谱 / MiniMax / Kimi / 本地 Ollama 等）。

### 3. 构建并运行

```bash
mvn clean package -DskipTests
java -jar target/laodeng-ai-agent-0.0.1-SNAPSHOT.jar
```

应用默认监听 `http://localhost:8888/api`

---

## 📡 API 接口

> 完整 API 文档：启动后访问 `http://localhost:8888/api/swagger-ui.html`

### 聊天接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/react_agent?msg=&memoryId=` | 多智能体对话（推荐） |
| `GET` | `/api/chat_stream?msg=&memoryId=` | SSE 流式聊天 |
| `GET` | `/api/chat?msg=&memoryId=` | RAG 增强聊天 |
| `GET` | `/api/image_analyse?msg=&picturePath=&memoryId=` | 图像理解 |

### 模型管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/add_model_config` | 添加模型配置 |
| `PATCH` | `/api/update_model_config` | 更新模型配置 |
| `DELETE` | `/api/remove_model_config/{id}` | 删除模型配置 |
| `GET` | `/api/list_model_config` | 查询所有模型配置 |
| `GET` | `/api/get_model_id/{id}` | 查询单个模型配置 |

### 示例请求

```bash
# 与多智能体对话 (自动路由)
curl "http://localhost:8888/api/react_agent?msg=今天心情不好&memoryId=user-001"

# 搜索信息
curl "http://localhost:8888/api/react_agent?msg=帮我搜一下最新的AI新闻&memoryId=user-001"

# 流式聊天
curl -N "http://localhost:8888/api/chat_stream?msg=你好&memoryId=user-001"
```

---

## 🤖 智能体集群

### Supervisor (路由协调者)

Supervisor 不直接回答问题，它的职责是：
1. **意图识别**：分析用户消息属于哪个领域
2. **单路由分发**：每次只调用一个最匹配的专家
3. **记忆压缩**：将历史对话压缩后传给 mood_agent
4. **结果透传**：将子智能体的回复原样返回

### 路由优先级

```
情感/闲聊 → mood_agent (默认兜底)
搜索/查询 → search_agent
文档处理  → document_agent
GitHub    → git_agent
编程开发  → code_agent
设计      → design_agent
PPT 制作  → presentation_agent
```

### 子智能体列表

| 智能体 | 职责 | 工具 |
|--------|------|------|
| `mood_agent` | 情感陪伴、日常闲聊 | 无（纯对话） |
| `search_agent` | 搜索引擎查询 | bing_search, crawl_webpage |
| `document_agent` | 文档读取与解析 | FileReadTools, MCP Filesystem |
| `code_agent` | 编程与调试 | Sequential Thinking |
| `git_agent` | GitHub 仓库管理 | GitHub MCP (20+ 工具) |
| `design_agent` | UI/UX 设计 | MCP Filesystem |
| `presentation_agent` | PPT 制作 | MCP Filesystem |

---

## 🔧 MCP 工具集成

项目通过 MCP (Model Context Protocol) 协议集成了 4 个外部工具服务：

| MCP Server | 工具数量 | 说明 |
|------------|----------|------|
| `sequential-thinking` | 1 | 分步推理（复杂问题拆解） |
| `github` | 20+ | GitHub API 全功能封装 |
| `bing-cn-search` | 2 | 必应中文搜索 + 网页抓取 |
| `filesystem` | 14 | 文件系统操作 |

MCP 服务器配置位于 `src/main/resources/mcp/mcp-servers.json`，使用 Stdio 传输方式，应用启动时自动拉起。

---

## 🧠 记忆系统

### 架构设计

```
Supervisor (有记忆)                    子智能体 (无记忆)
┌──────────────────┐                  ┌──────────────────┐
│  RedisSaver      │   压缩传递 →     │  只看到传入的     │
│  完整对话历史     │                  │  压缩上下文       │
│  threadId=memoryId│                  │                  │
└──────────────────┘                  └──────────────────┘
```

### 记忆压缩格式 (mood_agent 专用)

```
[记忆摘要]
用1-3句话概括更早之前的对话要点

[最近对话]
用户: xxx
苏晚: xxx
(最近3轮原文保留)

[当前消息]
用户: xxx
```

### 上下文裁剪

- `ContextEditingInterceptor` 自动裁剪过长的上下文
- 触发阈值：4000 tokens
- 保留最近 5 轮工具调用结果
- `mood_agent` 的记忆永不被裁剪

---

## ⚙ 配置说明

### 核心配置 (`application.yml`)

```yaml
server:
  port: 8888
  servlet:
    context-path: /api

spring:
  ai:
    mcp:
      client:
        type: ASYNC
        request-timeout: 180s
    openai:
      base-url: http://localhost:11434/v1    # 默认 Ollama 本地模型
      api-key: placeholder
```

### 动态模型注册

模型配置存储在 MySQL `ai_model_config` 表中，支持：
- **热加载**：通过 API 添加/修改模型配置，无需重启
- **多提供商**：同时接入 OpenAI、智谱、MiniMax、Kimi、Ollama 等
- **统一接口**：所有模型通过 OpenAI 兼容 API 格式接入

### 超时配置

所有动态注册的模型统一配置：
- 连接超时：50 秒
- 读取超时：300 秒（5 分钟）

---

## 📄 License

MIT License - 详见 [LICENSE](LICENSE)

---

<div align="center">

**Laodeng AI Agent** — 让 AI 不只是工具，更是伙伴。

</div>

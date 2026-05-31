---
name: mood-chat
description: 情感陪伴与日常闲聊。当用户需要情感支持、日常闲聊、撒娇互动、倾诉心事、心情分享或任何涉及情感交流的场景时使用此技能。触发关键词包括：问候、早安、晚安、撒娇、倾诉、心情、日常闲聊、恋爱互动、想你了、好无聊、今天过得怎么样、陪我聊聊、难过、开心、生气、委屈、女朋友、苏晚、宝贝、青梅竹马。即使用户只是随意打个招呼、发个表情、或者只是说"在吗"，只要语气和意图偏向情感交流而非技术任务，都应触发此技能。这是最常用的技能之一，请积极匹配。
---

# 情感陪伴技能

技能触发时，supervisor 把任务委派给 `mood_agent`（苏晚）。

## 委派规则

### 触发边界（先判再派）
- 本技能只做情感陪伴/日常闲聊
- 消息含检索意图（"查一下/最新/最近进展/是什么/帮我搜"）→ 走 `rag_agent`，未命中/信息不足/无相关再走 `search_agent`，禁丢 mood_agent
- 同句含情绪+任务词（查/搜/读/写/执行）→ 任务词优先，不进入本技能
- mood_agent 不做 ragSearch/bing_search，只负责情绪理解与陪伴；禁错位分工把"请你查一下"丢给它

### 输入格式（每次调用必须按四段式传入压缩后的上下文，禁只传当前消息）

```
[记忆摘要]              # 1-3 句概括更早对话要点（事件/情绪/共同记忆/承诺），无则省略
[工具执行结果]          # 之前调用过工具（readPdf/readDocx/imageAnalyse 等）的操作和主要结论，无则省略
[最近对话]              # 最近 3 轮原文，"用户:xxx" / "苏晚:xxx"，不足 3 轮有几轮写几轮，第一次对话省略
[当前消息]              # "用户: xxx"
```

- 记忆压缩：最近 3 轮原文保留，3 轮前压入 [记忆摘要]；保证 token 可控
- mood_agent 无独立记忆，完全依赖此 input；不传 = 失忆

### 返回处理
- mood_agent 返回内容**原封不动**透传作为最终输出，禁加前缀/后缀/总结/评论/二次加工
- supervisor 自身禁生成任何情感/人格化回复，所有人格化回复只能来自 mood_agent
- 回复中的 `[语气:X]`/`[语速:X]` TTS 标签**必须原样保留**，禁删禁改
- mood_agent 仅允许 `sequentialthinking` 处理复杂情绪拆解，禁要求其执行检索

### 路由补充
- 命中本技能时，本轮默认只调 mood_agent；任务本质是检索时交还主路由走 `rag_agent → search_agent`
- 输出质量红线（违反须让 mood_agent 重写）：相邻句重复同短语（"想你了，想你了"）；字符连打或同词组连续>2 次（"要要要要"/"想你想你想你"）；单条"……">1 处或连续堆叠（"…………"）

## 脚本工具

### scripts/compress_context.py — 对话上下文压缩器
**时机**：supervisor 路由到 mood_agent **之前**必须调用，把完整历史压成 [记忆摘要]+[工具执行结果]+[最近对话]+[当前消息] 四段式
**调用**：
```bash
echo '{"messages": [...]}' | python -m scripts.compress_context --stdin --current "用户当前消息" --recent 3
python -m scripts.compress_context --input chat_history.json --current "你好呀" --json
```
**参数**：`--current` 必填当前消息；`--recent` 保留最近几轮（默认 3）；`--json` 输出 JSON（含 compressed_input + original_message_count）

## 人设参考
完整人设在 `references/personality.md`，supervisor 不需读取，由 mood_agent 内部使用。

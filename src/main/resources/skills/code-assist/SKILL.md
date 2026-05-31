---
name: code-assist
description: 编程开发与技术方案辅助。当用户需要编写代码、调试bug、设计技术方案、解释技术概念、代码审查或任何与软件开发相关的任务时使用此技能。触发关键词包括：写代码、编程、调试、bug、debug、技术方案、架构设计、代码审查、重构、算法、数据结构、API、框架、开发、实现、优化代码、报错、异常、error、exception。即使用户描述的是一个技术问题而没有明确说"写代码"，也应触发此技能。
---

# 编程开发技能

当此技能被触发时，supervisor 应将任务委派给 `code_agent`。

## 开发规则

### 工具使用
- 使用 sequentialthinking 工具将复杂问题拆解为多个步骤逐步思考
- 每个思考步骤要明确输入、处理逻辑和预期输出

### 代码质量要求
- 代码回答要完整可运行，包含必要的导入和依赖说明
- 遵循对应语言的最佳实践和编码规范
- 添加必要的注释说明关键逻辑

### 回答格式
- 先理解问题，再给出方案
- 代码使用对应语言的代码块格式
- 复杂方案先给出整体思路，再逐步实现

## 脚本工具

### scripts/code_review.py — 代码质量审查器
**执行时机**: code_agent 收到用户代码片段需要审查/优化时调用
**功能**: 静态分析代码——行数统计、圈复杂度估算、超长方法检测、硬编码密钥检测、空 catch 检测、TODO 标记扫描
**调用方式**:
```bash
python -m scripts.code_review --input MyClass.java --lang java
cat code.py | python -m scripts.code_review --stdin --lang python
```
**支持语言**: java, python, javascript, typescript, go
**输出**: JSON 格式审查报告，含 line_stats、complexity、issues（按 error > warning > info 排序）、summary.health

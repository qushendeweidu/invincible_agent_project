---
name: github-ops
description: GitHub仓库操作与代码管理。当用户需要进行GitHub相关操作时使用此技能，包括：查看仓库、搜索代码、查看PR和Issue、代码提交历史、仓库管理等。触发关键词包括：仓库、GitHub、代码提交、PR、pull request、issue、commit、分支、branch、star、fork、开源项目、代码仓库、git log、代码审查、release。即使用户只是提到项目名称并想了解其代码内容，也应触发此技能。
---

# GitHub 操作技能

当此技能被触发时，supervisor 应将任务委派给 `git_agent`。

## 操作规则

### 仓库定位
- 当用户只提供项目名称但没有指定 owner 时，必须先使用 search_repositories 搜索
- 找到仓库后再使用 get_file_contents 等工具访问具体内容
- 不要猜测 owner 名称，必须通过搜索确认

### 常见操作
- 查看仓库信息：get_file_contents
- 搜索仓库：search_repositories
- 查看 Issue：list_issues / get_issue
- 查看 PR：list_pull_requests
- 查看提交历史：list_commits

### 回答格式
- 提供仓库的关键信息（star数、语言、描述）
- 代码内容使用代码块格式展示
- Issue/PR 列表使用结构化格式

## 脚本工具

### scripts/repo_analyzer.py — 仓库结构分析器
**执行时机**: git_agent 获取到仓库 file tree 后调用，用于快速识别项目类型和技术栈
**功能**: 分析文件扩展名分布、框架识别（Maven/Gradle/Node.js/Spring Boot等）、目录结构概览
**调用方式**:
```bash
echo '{"tree": [...]}' | python -m scripts.repo_analyzer --stdin
python -m scripts.repo_analyzer --input file_tree.json --output report.json
```
**输出**: JSON 格式报告，含 project_type、primary_languages、frameworks_detected、top_directories

---
name: ppt-maker
description: 演示文稿制作与设计。当用户需要创建、修改或设计PPT演示文稿、幻灯片时使用此技能。触发关键词包括：PPT、演示文稿、幻灯片、Slide、PowerPoint、presentation、汇报材料、路演、答辩PPT、演讲稿、做个PPT、报告展示。即使用户只是说"帮我做个汇报材料"或描述了演示需求而没有明确说"PPT"，也应触发此技能。
---

# 演示文稿制作技能

当此技能被触发时，supervisor 应将任务委派给 `presentation_agent`。如果涉及视觉美化，可同时委派 `design_agent`。

## 制作规则

### 文件操作
- 生成演示文稿前，先用 list_allowed_directories 确认可写入的目录
- 创建文件前先用 create_directory 确保目标目录存在

### 结构要求
- 输出的演示文稿应结构清晰
- 包含标题页、目录页、内容页和总结页
- 每页内容精炼，避免文字堆砌

### 设计原则
- 配色统一，风格一致
- 图文结合，增强表达力
- 留白适当，视觉舒适

## 脚本工具

### scripts/generate_pptx.py — PPTX 生成器
**依赖**: `pip install python-pptx`
**执行时机**: presentation_agent 收到演示文稿需求后，先将内容组织为 JSON 结构，再调用此脚本生成 .pptx 文件
**功能**: 根据结构化 JSON（title/subtitle/slides/summary）生成带配色主题的 PPTX 文件，含标题页、内容页（要点列表）、总结页
**调用方式**:
```bash
echo '{"title":"项目汇报","subtitle":"2026Q1","slides":[{"title":"背景","bullets":["点1","点2"]}],"summary":["结论1"]}' | python -m scripts.generate_pptx --stdin --output report.pptx
python -m scripts.generate_pptx --input slides.json --output presentation.pptx
python -m scripts.generate_pptx  # 使用内置演示数据
```
**JSON 输入格式**:
```json
{
  "title": "演示标题",
  "subtitle": "副标题（可选）",
  "slides": [
    {"title": "页面标题", "bullets": ["要点1", "要点2", "要点3"]}
  ],
  "summary": ["总结要点1", "总结要点2"]
}
```
**输出**: .pptx 文件（10×7.5英寸，Indigo主题配色）

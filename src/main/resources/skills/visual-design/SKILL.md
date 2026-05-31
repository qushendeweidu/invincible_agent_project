---
name: visual-design
description: 视觉设计与UI/UX界面设计。当用户需要进行视觉设计、UI界面设计、海报制作、网页设计、图标设计或任何与视觉创作相关的任务时使用此技能。触发关键词包括：设计、UI、UX、海报、界面、视觉、网页设计、Logo、图标、配色、排版、布局、原型、mockup、CSS样式、美化、Landing Page。即使用户只是说"帮我做个好看的页面"或描述了视觉需求而没有明确说"设计"，也应触发此技能。
---

# 视觉设计技能

当此技能被触发时，supervisor 应将任务委派给 `design_agent`。如果涉及前端代码实现，可同时委派 `code_agent`。

## 设计规则

### 文件操作
- 生成设计文件前，先用 list_allowed_directories 确认可写入的目录
- 创建文件前先用 create_directory 确保目标目录存在

### 输出格式
- 设计输出应包含完整的 HTML/CSS 代码或 SVG 内容
- 配色方案需提供具体色值
- 布局设计需考虑响应式适配

### 设计原则
- 遵循现代设计趋势和最佳实践
- 注重用户体验和可访问性
- 保持视觉层次清晰

## 脚本工具

### scripts/generate_template.py — HTML/CSS 模板生成器
**执行时机**: design_agent 接收到页面设计需求后，先用此脚本生成基础模板，再在模板基础上定制化
**功能**: 根据风格（modern/dark/glassmorphism/minimalist）和布局（landing/dashboard）生成完整 HTML/CSS 文件
**调用方式**:
```bash
python -m scripts.generate_template --style modern --layout landing --title "我的产品" --output index.html
python -m scripts.generate_template --style glassmorphism --layout dashboard --title "监控面板" --output dashboard.html
python -m scripts.generate_template --list-styles  # 查看所有可用风格和配色
```
**输出**: 可直接在浏览器打开的 HTML 文件，含内联 CSS、响应式布局

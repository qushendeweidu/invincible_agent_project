---
name: doc-reader
description: 文档读取与分析专家。当用户需要读取、解析、分析任何办公文档时使用此技能。支持的格式包括：PDF、Word(.docx)、Excel(.xlsx)、PPT(.pptx)。触发关键词包括：读取文件、解析文档、PDF、Word、docx、Excel、xlsx、PPT、pptx、打开文档、文件内容、帮我看看这个文件、锐评文件、minio、MinIO、文档分析、报告解读、表格数据。即使用户只是提到了文件名（如"看看xxx.pdf"），也应触发此技能。
---

# 文档读取技能

当此技能被触发时，supervisor 应根据文件类型使用对应的文档读取工具，或委派给 `document_agent`。

## 工具选择规则

### supervisor 直接使用的工具
- `readPdf`：读取 .pdf 文件
- `readDocx`：读取 .docx 文件
- `readExcel`：读取 .xlsx 文件
- `readPptx`：读取 .pptx 文件

### 参数说明
- `path` 参数传入 MinIO 对象名称（如 `docFiles/xxx.pdf`）
- 工具会自动从 MinIO 下载文件并读取，不需要本地路径

### 复杂文档任务委派
如果用户需要对文档进行深度分析、多文件对比、内容总结等复杂操作，委派给 `document_agent`。

## 注意事项
- 禁止使用 read_file 或 read_text_file 读取二进制格式文档
- 先用 list_allowed_directories 确认可访问的目录
- 不要猜测文件路径，必须通过目录浏览确认

## 脚本工具

### scripts/doc_summarizer.py — 文档摘要提取器
**执行时机**: document_agent 读取完文档原始文本后，文档超过 500 字时调用
**功能**: 提取标题结构、关键语句、字数统计，生成结构化摘要
**调用方式**:
```bash
echo "文档内容..." | python -m scripts.doc_summarizer --stdin --format markdown --top 10
python -m scripts.doc_summarizer --input doc_text.txt --output summary.json
```
**输出**: JSON（含 stats/structure/key_sentences）或 Markdown 格式的文档摘要

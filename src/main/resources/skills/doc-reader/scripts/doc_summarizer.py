#!/usr/bin/env python3
"""文档内容摘要提取工具。

接收文档原始文本，生成结构化摘要（标题、章节、关键段落、字数统计）。
被 document_agent 在读取文档原始内容后调用，用于快速呈现文档概览。

用法:
    python -m scripts.doc_summarizer --input doc_text.txt --output summary.json
    echo "文档内容..." | python -m scripts.doc_summarizer --stdin --format markdown
"""

import argparse
import json
import re
import sys
from datetime import datetime


def extract_headings(text: str) -> list[dict]:
    """提取文档中的标题层级结构。"""
    headings = []
    lines = text.split("\n")
    for i, line in enumerate(lines):
        stripped = line.strip()
        # Markdown 标题
        md_match = re.match(r"^(#{1,6})\s+(.+)", stripped)
        if md_match:
            headings.append({
                "level": len(md_match.group(1)),
                "title": md_match.group(2).strip(),
                "line": i + 1
            })
            continue
        # 中文章节标题模式：第X章、一、1.1 等
        cn_match = re.match(
            r"^(第[一二三四五六七八九十\d]+[章节篇]|[一二三四五六七八九十]+[、.]|[\d]+[.\s])\s*(.+)",
            stripped
        )
        if cn_match and len(stripped) < 50:
            headings.append({
                "level": 2,
                "title": stripped,
                "line": i + 1
            })
    return headings


def extract_key_sentences(text: str, top_n: int = 10) -> list[str]:
    """提取文档中的关键句子（基于长度和关键词密度）。"""
    sentences = re.split(r"[。！？\n]", text)
    sentences = [s.strip() for s in sentences if len(s.strip()) > 15]

    key_words = ["重要", "关键", "核心", "总结", "结论", "目的", "方法",
                 "结果", "建议", "注意", "必须", "应该", "主要"]

    def score(s: str) -> float:
        sc = 0.0
        # 适中长度得分更高
        length = len(s)
        if 30 < length < 150:
            sc += 2.0
        elif 15 < length <= 30:
            sc += 1.0
        # 包含关键词加分
        for kw in key_words:
            if kw in s:
                sc += 1.5
        return sc

    scored = sorted(sentences, key=score, reverse=True)
    return scored[:top_n]


def count_stats(text: str) -> dict:
    """统计文档基本信息。"""
    lines = text.split("\n")
    chars = len(text)
    words_cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    words_en = len(re.findall(r"[a-zA-Z]+", text))
    paragraphs = len([p for p in text.split("\n\n") if p.strip()])

    return {
        "total_chars": chars,
        "chinese_chars": words_cn,
        "english_words": words_en,
        "lines": len(lines),
        "paragraphs": paragraphs
    }


def summarize(text: str, top_sentences: int = 10) -> dict:
    """生成文档结构化摘要。"""
    stats = count_stats(text)
    headings = extract_headings(text)
    key_sentences = extract_key_sentences(text, top_sentences)

    # 提取首段作为概览
    paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
    overview = paragraphs[0][:300] + "..." if paragraphs and len(paragraphs[0]) > 300 else (paragraphs[0] if paragraphs else "")

    return {
        "timestamp": datetime.now().isoformat(),
        "stats": stats,
        "overview": overview,
        "structure": headings,
        "key_sentences": key_sentences
    }


def to_markdown(summary: dict) -> str:
    """将摘要转为 Markdown 格式。"""
    lines = ["# 文档摘要\n"]

    stats = summary["stats"]
    lines.append(f"**字数统计**: 中文 {stats['chinese_chars']} 字 | 英文 {stats['english_words']} 词 | "
                 f"共 {stats['lines']} 行 | {stats['paragraphs']} 段\n")

    if summary["overview"]:
        lines.append("## 概览")
        lines.append(summary["overview"] + "\n")

    if summary["structure"]:
        lines.append("## 文档结构")
        for h in summary["structure"]:
            indent = "  " * (h["level"] - 1)
            lines.append(f"{indent}- {h['title']} (行{h['line']})")
        lines.append("")

    if summary["key_sentences"]:
        lines.append("## 关键语句")
        for i, s in enumerate(summary["key_sentences"], 1):
            lines.append(f"{i}. {s}")

    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="文档摘要提取工具")
    parser.add_argument("--input", type=str, help="输入文本文件路径")
    parser.add_argument("--stdin", action="store_true", help="从 stdin 读取")
    parser.add_argument("--output", type=str, help="输出文件路径")
    parser.add_argument("--format", choices=["json", "markdown"], default="json")
    parser.add_argument("--top", type=int, default=10, help="关键句子数量")
    args = parser.parse_args()

    if args.stdin:
        text = sys.stdin.read()
    elif args.input:
        with open(args.input, "r", encoding="utf-8") as f:
            text = f.read()
    else:
        parser.error("必须指定 --input 或 --stdin")
        return

    summary = summarize(text, args.top)

    if args.format == "markdown":
        result = to_markdown(summary)
    else:
        result = json.dumps(summary, ensure_ascii=False, indent=2)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(result)
    else:
        print(result)


if __name__ == "__main__":
    main()

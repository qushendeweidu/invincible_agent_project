#!/usr/bin/env python3
"""搜索结果格式化与去重工具。

接收原始搜索结果 JSON，执行去重、排序、摘要提取，输出结构化结果。
被 search_agent 在获取到 bing_search 原始结果后调用。

用法:
    python -m scripts.search_formatter --input raw_results.json --output formatted.json
    echo '{"results": [...]}' | python -m scripts.search_formatter --stdin
"""

import argparse
import json
import sys
import re
from collections import OrderedDict
from datetime import datetime


def deduplicate(results: list[dict]) -> list[dict]:
    """基于 URL 去重，保留第一次出现的结果。"""
    seen_urls = set()
    unique = []
    for r in results:
        url = r.get("url", "").rstrip("/")
        domain = extract_domain(url)
        # 同域名 + 同标题视为重复
        key = f"{domain}|{r.get('title', '')[:50]}"
        if key not in seen_urls:
            seen_urls.add(key)
            unique.append(r)
    return unique


def extract_domain(url: str) -> str:
    """从 URL 提取域名。"""
    match = re.search(r"https?://([^/]+)", url)
    return match.group(1) if match else url


def rank_results(results: list[dict], query: str) -> list[dict]:
    """基于相关性评分排序。"""
    query_terms = set(query.lower().split())

    def score(r: dict) -> float:
        title = r.get("title", "").lower()
        snippet = r.get("snippet", "").lower()
        s = 0.0
        for term in query_terms:
            if term in title:
                s += 3.0
            if term in snippet:
                s += 1.0
        # 权威域名加分
        domain = extract_domain(r.get("url", ""))
        authority_domains = [
            "wikipedia.org", "github.com", "stackoverflow.com",
            "zhihu.com", "csdn.net", "juejin.cn",
            "docs.spring.io", "baike.baidu.com"
        ]
        if any(d in domain for d in authority_domains):
            s += 2.0
        return s

    return sorted(results, key=score, reverse=True)


def truncate_snippet(snippet: str, max_len: int = 200) -> str:
    """截断摘要到指定长度，保持句子完整。"""
    if len(snippet) <= max_len:
        return snippet
    cut = snippet[:max_len]
    # 尝试在句号/逗号处截断
    for sep in ["。", ".", "，", ",", "；", " "]:
        idx = cut.rfind(sep)
        if idx > max_len // 2:
            return cut[:idx + 1] + "..."
    return cut + "..."


def format_output(results: list[dict], query: str, top_n: int = 5) -> dict:
    """生成最终格式化输出。"""
    deduped = deduplicate(results)
    ranked = rank_results(deduped, query)
    top = ranked[:top_n]

    formatted_items = []
    for i, r in enumerate(top, 1):
        formatted_items.append({
            "rank": i,
            "title": r.get("title", ""),
            "url": r.get("url", ""),
            "snippet": truncate_snippet(r.get("snippet", "")),
            "domain": extract_domain(r.get("url", ""))
        })

    return {
        "query": query,
        "total_raw": len(results),
        "total_after_dedup": len(deduped),
        "top_n": len(formatted_items),
        "timestamp": datetime.now().isoformat(),
        "results": formatted_items
    }


def main():
    parser = argparse.ArgumentParser(description="搜索结果格式化工具")
    parser.add_argument("--input", type=str, help="输入 JSON 文件路径")
    parser.add_argument("--stdin", action="store_true", help="从 stdin 读取")
    parser.add_argument("--output", type=str, help="输出 JSON 文件路径")
    parser.add_argument("--query", type=str, default="", help="原始搜索关键词")
    parser.add_argument("--top", type=int, default=5, help="保留前 N 条结果")
    args = parser.parse_args()

    if args.stdin:
        raw = json.load(sys.stdin)
    elif args.input:
        with open(args.input, "r", encoding="utf-8") as f:
            raw = json.load(f)
    else:
        parser.error("必须指定 --input 或 --stdin")
        return

    results = raw.get("results", raw if isinstance(raw, list) else [])
    query = args.query or raw.get("query", "")

    output = format_output(results, query, args.top)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            json.dump(output, f, ensure_ascii=False, indent=2)
    else:
        print(json.dumps(output, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()

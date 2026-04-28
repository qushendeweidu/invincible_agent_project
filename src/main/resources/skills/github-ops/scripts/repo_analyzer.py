#!/usr/bin/env python3
"""GitHub 仓库结构分析工具。

接收仓库文件树 JSON，分析项目类型、技术栈、目录结构，输出结构化报告。
被 git_agent 在获取到仓库 file tree 后调用。

用法:
    python -m scripts.repo_analyzer --input file_tree.json --output report.json
    echo '{"tree": [...]}' | python -m scripts.repo_analyzer --stdin
"""

import argparse
import json
import re
import sys
from collections import Counter, defaultdict
from datetime import datetime
from pathlib import PurePosixPath


# 文件扩展名 → 语言/技术映射
LANG_MAP = {
    ".java": "Java", ".kt": "Kotlin", ".scala": "Scala",
    ".py": "Python", ".pyx": "Cython",
    ".js": "JavaScript", ".jsx": "React", ".ts": "TypeScript", ".tsx": "React/TS",
    ".vue": "Vue", ".svelte": "Svelte",
    ".go": "Go", ".rs": "Rust", ".c": "C", ".cpp": "C++", ".h": "C/C++ Header",
    ".rb": "Ruby", ".php": "PHP", ".swift": "Swift", ".m": "Objective-C",
    ".dart": "Dart", ".lua": "Lua", ".r": "R",
    ".html": "HTML", ".css": "CSS", ".scss": "SCSS", ".less": "Less",
    ".sql": "SQL", ".graphql": "GraphQL",
    ".sh": "Shell", ".bat": "Batch", ".ps1": "PowerShell",
    ".yml": "YAML", ".yaml": "YAML", ".toml": "TOML", ".json": "JSON",
    ".xml": "XML", ".proto": "Protobuf",
    ".md": "Markdown", ".rst": "reStructuredText",
    ".dockerfile": "Docker", ".tf": "Terraform",
}

# 特征文件 → 框架/工具识别
FRAMEWORK_MARKERS = {
    "pom.xml": "Maven", "build.gradle": "Gradle", "build.gradle.kts": "Gradle/Kotlin",
    "package.json": "Node.js", "yarn.lock": "Yarn", "pnpm-lock.yaml": "pnpm",
    "requirements.txt": "pip", "pyproject.toml": "Python Project", "setup.py": "setuptools",
    "Cargo.toml": "Rust/Cargo", "go.mod": "Go Modules",
    "Gemfile": "Ruby/Bundler", "composer.json": "PHP/Composer",
    "Dockerfile": "Docker", "docker-compose.yml": "Docker Compose",
    ".github": "GitHub Actions", "Jenkinsfile": "Jenkins",
    "next.config.js": "Next.js", "nuxt.config.ts": "Nuxt.js",
    "vite.config.ts": "Vite", "webpack.config.js": "Webpack",
    "tailwind.config.js": "TailwindCSS",
    "application.yml": "Spring Boot", "application.properties": "Spring Boot",
}


def analyze_tree(tree: list[dict]) -> dict:
    """分析文件树结构。"""
    lang_counter = Counter()
    framework_set = set()
    dir_structure = defaultdict(int)
    total_files = 0

    for item in tree:
        path = item.get("path", "")
        item_type = item.get("type", "blob")

        if item_type == "tree":
            # 目录：记录顶层结构
            parts = PurePosixPath(path).parts
            if len(parts) == 1:
                dir_structure[parts[0]] = 0
            continue

        total_files += 1
        filename = PurePosixPath(path).name
        suffix = PurePosixPath(path).suffix.lower()

        # 语言统计
        if suffix in LANG_MAP:
            lang_counter[LANG_MAP[suffix]] += 1

        # 框架识别
        if filename in FRAMEWORK_MARKERS:
            framework_set.add(FRAMEWORK_MARKERS[filename])
        # .github 目录
        if path.startswith(".github"):
            framework_set.add("GitHub Actions")

        # 顶层目录文件计数
        parts = PurePosixPath(path).parts
        if len(parts) > 1:
            dir_structure[parts[0]] = dir_structure.get(parts[0], 0) + 1

    # 主要语言（占比 > 5%）
    primary_langs = []
    if total_files > 0:
        for lang, count in lang_counter.most_common():
            pct = count / total_files * 100
            if pct >= 5:
                primary_langs.append({"language": lang, "files": count, "percentage": round(pct, 1)})

    return {
        "total_files": total_files,
        "primary_languages": primary_langs,
        "frameworks_detected": sorted(framework_set),
        "top_directories": dict(sorted(dir_structure.items(), key=lambda x: -x[1])[:15]),
        "all_languages": dict(lang_counter.most_common())
    }


def infer_project_type(analysis: dict) -> str:
    """推断项目类型。"""
    frameworks = set(analysis.get("frameworks_detected", []))
    langs = {l["language"] for l in analysis.get("primary_languages", [])}

    if "Spring Boot" in frameworks:
        return "Java Spring Boot 后端项目"
    if "Next.js" in frameworks:
        return "Next.js 全栈项目"
    if "Maven" in frameworks and "Java" in langs:
        return "Java Maven 项目"
    if "Node.js" in frameworks and ("React" in langs or "React/TS" in langs):
        return "React 前端项目"
    if "Vue" in langs:
        return "Vue 前端项目"
    if "Go" in langs:
        return "Go 后端项目"
    if "Python" in langs and "pip" in frameworks:
        return "Python 项目"
    if "Rust/Cargo" in frameworks:
        return "Rust 项目"
    return "未识别类型"


def format_report(analysis: dict) -> dict:
    """生成最终报告。"""
    return {
        "timestamp": datetime.now().isoformat(),
        "project_type": infer_project_type(analysis),
        **analysis
    }


def main():
    parser = argparse.ArgumentParser(description="GitHub 仓库结构分析")
    parser.add_argument("--input", type=str, help="输入文件树 JSON 路径")
    parser.add_argument("--stdin", action="store_true", help="从 stdin 读取")
    parser.add_argument("--output", type=str, help="输出报告路径")
    args = parser.parse_args()

    if args.stdin:
        raw = json.load(sys.stdin)
    elif args.input:
        with open(args.input, "r", encoding="utf-8") as f:
            raw = json.load(f)
    else:
        parser.error("必须指定 --input 或 --stdin")
        return

    tree = raw.get("tree", raw if isinstance(raw, list) else [])
    analysis = analyze_tree(tree)
    report = format_report(analysis)

    result = json.dumps(report, ensure_ascii=False, indent=2)
    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(result)
    else:
        print(result)


if __name__ == "__main__":
    main()

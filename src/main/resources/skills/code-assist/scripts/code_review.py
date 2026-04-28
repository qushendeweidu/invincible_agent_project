#!/usr/bin/env python3
"""代码质量审查工具。

对输入的代码文件进行静态分析：复杂度估算、潜在问题检测、风格检查。
被 code_agent 在分析用户提交的代码片段时调用。

用法:
    python -m scripts.code_review --input MyClass.java --lang java
    cat code.py | python -m scripts.code_review --stdin --lang python
"""

import argparse
import json
import re
import sys
from datetime import datetime


# ====== 通用检测规则 ======

def count_lines(code: str) -> dict:
    """统计代码行数。"""
    lines = code.split("\n")
    total = len(lines)
    blank = sum(1 for l in lines if not l.strip())
    comment_patterns = [
        r"^\s*//",    # Java/JS/Go 单行注释
        r"^\s*#",     # Python/Shell 注释
        r"^\s*\*",    # 多行注释中间行
        r"^\s*/\*",   # 多行注释开始
    ]
    comments = sum(1 for l in lines if any(re.match(p, l) for p in comment_patterns))
    return {
        "total": total,
        "code": total - blank - comments,
        "blank": blank,
        "comment": comments,
        "comment_ratio": round(comments / max(total, 1) * 100, 1)
    }


def detect_long_methods(code: str, lang: str, threshold: int = 50) -> list[dict]:
    """检测超长方法/函数。"""
    issues = []
    if lang in ("java", "javascript", "typescript"):
        # 匹配方法签名
        pattern = r"(public|private|protected|static|async)?\s*\w+\s+(\w+)\s*\([^)]*\)\s*\{"
        matches = list(re.finditer(pattern, code))
        for m in matches:
            start = code[:m.start()].count("\n") + 1
            # 简单计算方法体行数（找匹配的 }）
            brace_count = 0
            end_line = start
            for i, ch in enumerate(code[m.start():]):
                if ch == "{":
                    brace_count += 1
                elif ch == "}":
                    brace_count -= 1
                    if brace_count == 0:
                        end_line = code[:m.start() + i].count("\n") + 1
                        break
            length = end_line - start
            if length > threshold:
                issues.append({
                    "type": "long_method",
                    "severity": "warning",
                    "method": m.group(2) if m.group(2) else "anonymous",
                    "line": start,
                    "length": length,
                    "message": f"方法 {m.group(2)} 有 {length} 行，超过阈值 {threshold} 行，建议拆分"
                })
    elif lang == "python":
        pattern = r"def\s+(\w+)\s*\("
        matches = list(re.finditer(pattern, code))
        for idx, m in enumerate(matches):
            start = code[:m.start()].count("\n") + 1
            if idx + 1 < len(matches):
                end = code[:matches[idx + 1].start()].count("\n")
            else:
                end = code.count("\n")
            length = end - start
            if length > threshold:
                issues.append({
                    "type": "long_method",
                    "severity": "warning",
                    "method": m.group(1),
                    "line": start,
                    "length": length,
                    "message": f"函数 {m.group(1)} 有 {length} 行，超过阈值 {threshold} 行，建议拆分"
                })
    return issues


def detect_common_issues(code: str, lang: str) -> list[dict]:
    """检测常见代码问题。"""
    issues = []
    lines = code.split("\n")

    for i, line in enumerate(lines, 1):
        stripped = line.rstrip()

        # 行过长
        if len(stripped) > 120:
            issues.append({
                "type": "line_too_long",
                "severity": "info",
                "line": i,
                "length": len(stripped),
                "message": f"第 {i} 行有 {len(stripped)} 字符，超过 120 字符限制"
            })

        # 硬编码密码/token
        if re.search(r"(password|secret|token|api_key)\s*=\s*[\"'][^\"']+[\"']", stripped, re.IGNORECASE):
            issues.append({
                "type": "hardcoded_secret",
                "severity": "error",
                "line": i,
                "message": f"第 {i} 行疑似硬编码敏感信息（密码/密钥/Token），应使用环境变量或配置中心"
            })

        # TODO/FIXME/HACK
        if re.search(r"\b(TODO|FIXME|HACK|XXX)\b", stripped):
            issues.append({
                "type": "todo_marker",
                "severity": "info",
                "line": i,
                "message": f"第 {i} 行有待处理标记: {stripped.strip()[:80]}"
            })

    # Java 特有检测
    if lang == "java":
        # catch 空块
        for m in re.finditer(r"catch\s*\([^)]+\)\s*\{\s*\}", code):
            line = code[:m.start()].count("\n") + 1
            issues.append({
                "type": "empty_catch",
                "severity": "warning",
                "line": line,
                "message": f"第 {line} 行 catch 块为空，异常被静默吞掉，至少应记录日志"
            })
        # System.out.println
        for m in re.finditer(r"System\.out\.print", code):
            line = code[:m.start()].count("\n") + 1
            issues.append({
                "type": "sysout",
                "severity": "warning",
                "line": line,
                "message": f"第 {line} 行使用了 System.out.print，生产代码应使用日志框架"
            })

    # Python 特有检测
    if lang == "python":
        # bare except
        for m in re.finditer(r"except\s*:", code):
            line = code[:m.start()].count("\n") + 1
            issues.append({
                "type": "bare_except",
                "severity": "warning",
                "line": line,
                "message": f"第 {line} 行使用了裸 except，应指定具体异常类型"
            })

    return issues


def estimate_complexity(code: str) -> dict:
    """估算代码复杂度指标。"""
    # 简单的圈复杂度近似：统计分支关键词
    branch_keywords = ["if", "elif", "else if", "for", "while", "case",
                       "catch", "except", "&&", "||", "?"]
    complexity = 1
    for kw in branch_keywords:
        complexity += len(re.findall(r"\b" + re.escape(kw) + r"\b", code))

    # 嵌套深度估算
    max_indent = 0
    for line in code.split("\n"):
        if line.strip():
            indent = len(line) - len(line.lstrip())
            max_indent = max(max_indent, indent)

    return {
        "cyclomatic_complexity_approx": complexity,
        "max_indent_depth": max_indent // 4,
        "rating": "低" if complexity < 10 else ("中" if complexity < 20 else "高")
    }


def review(code: str, lang: str) -> dict:
    """执行完整代码审查。"""
    line_stats = count_lines(code)
    complexity = estimate_complexity(code)
    issues = detect_common_issues(code, lang)
    issues.extend(detect_long_methods(code, lang))

    # 按严重程度排序
    severity_order = {"error": 0, "warning": 1, "info": 2}
    issues.sort(key=lambda x: severity_order.get(x["severity"], 9))

    error_count = sum(1 for i in issues if i["severity"] == "error")
    warn_count = sum(1 for i in issues if i["severity"] == "warning")
    info_count = sum(1 for i in issues if i["severity"] == "info")

    return {
        "timestamp": datetime.now().isoformat(),
        "language": lang,
        "line_stats": line_stats,
        "complexity": complexity,
        "issues": issues,
        "summary": {
            "errors": error_count,
            "warnings": warn_count,
            "info": info_count,
            "total_issues": len(issues),
            "health": "良好" if error_count == 0 and warn_count < 3 else (
                "一般" if error_count == 0 else "需要修复"
            )
        }
    }


def main():
    parser = argparse.ArgumentParser(description="代码质量审查工具")
    parser.add_argument("--input", type=str, help="输入代码文件路径")
    parser.add_argument("--stdin", action="store_true", help="从 stdin 读取")
    parser.add_argument("--output", type=str, help="输出报告路径")
    parser.add_argument("--lang", type=str, default="java",
                        choices=["java", "python", "javascript", "typescript", "go"],
                        help="编程语言")
    args = parser.parse_args()

    if args.stdin:
        code = sys.stdin.read()
    elif args.input:
        with open(args.input, "r", encoding="utf-8") as f:
            code = f.read()
    else:
        parser.error("必须指定 --input 或 --stdin")
        return

    report = review(code, args.lang)
    result = json.dumps(report, ensure_ascii=False, indent=2)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(result)
    else:
        print(result)


if __name__ == "__main__":
    main()

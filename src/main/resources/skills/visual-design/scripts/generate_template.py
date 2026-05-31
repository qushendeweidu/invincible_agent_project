#!/usr/bin/env python3
"""HTML/CSS 设计模板生成工具。

根据指定的设计风格和布局类型，生成可直接使用的 HTML/CSS 模板文件。
被 design_agent 在接收到视觉设计需求时调用。

用法:
    python -m scripts.generate_template --style modern --layout landing --output index.html
    python -m scripts.generate_template --style glassmorphism --layout dashboard --title "监控面板"
"""

import argparse
import json
import sys
from datetime import datetime
from pathlib import Path

# ====== 配色方案 ======
COLOR_SCHEMES = {
    "modern": {
        "primary": "#6366f1",
        "secondary": "#8b5cf6",
        "bg": "#ffffff",
        "surface": "#f8fafc",
        "text": "#1e293b",
        "text_secondary": "#64748b",
        "border": "#e2e8f0",
        "accent": "#06b6d4"
    },
    "dark": {
        "primary": "#818cf8",
        "secondary": "#a78bfa",
        "bg": "#0f172a",
        "surface": "#1e293b",
        "text": "#f1f5f9",
        "text_secondary": "#94a3b8",
        "border": "#334155",
        "accent": "#22d3ee"
    },
    "glassmorphism": {
        "primary": "#7c3aed",
        "secondary": "#db2777",
        "bg": "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
        "surface": "rgba(255, 255, 255, 0.15)",
        "text": "#ffffff",
        "text_secondary": "rgba(255, 255, 255, 0.7)",
        "border": "rgba(255, 255, 255, 0.2)",
        "accent": "#f472b6"
    },
    "minimalist": {
        "primary": "#111827",
        "secondary": "#374151",
        "bg": "#ffffff",
        "surface": "#f9fafb",
        "text": "#111827",
        "text_secondary": "#6b7280",
        "border": "#e5e7eb",
        "accent": "#ef4444"
    }
}

# ====== 布局模板 ======

def landing_template(colors: dict, title: str) -> str:
    bg_style = f"background: {colors['bg']};" if not colors['bg'].startswith('linear') else f"background: {colors['bg']};"
    return f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title}</title>
    <style>
        * {{ margin: 0; padding: 0; box-sizing: border-box; }}
        body {{ font-family: -apple-system, 'Segoe UI', sans-serif; {bg_style} color: {colors['text']}; }}
        .hero {{
            min-height: 100vh; display: flex; flex-direction: column;
            align-items: center; justify-content: center; text-align: center;
            padding: 2rem;
        }}
        .hero h1 {{ font-size: 3.5rem; font-weight: 800; margin-bottom: 1rem; }}
        .hero p {{ font-size: 1.25rem; color: {colors['text_secondary']}; max-width: 600px; margin-bottom: 2rem; }}
        .btn {{
            padding: 0.875rem 2rem; border-radius: 0.5rem; font-size: 1rem;
            font-weight: 600; cursor: pointer; border: none; transition: all 0.2s;
        }}
        .btn-primary {{ background: {colors['primary']}; color: white; }}
        .btn-primary:hover {{ opacity: 0.9; transform: translateY(-1px); }}
        .features {{
            display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem; padding: 4rem 2rem; max-width: 1200px; margin: 0 auto;
        }}
        .feature-card {{
            background: {colors['surface']}; border: 1px solid {colors['border']};
            border-radius: 1rem; padding: 2rem; transition: transform 0.2s;
        }}
        .feature-card:hover {{ transform: translateY(-4px); }}
        .feature-card h3 {{ font-size: 1.25rem; margin-bottom: 0.75rem; }}
        .feature-card p {{ color: {colors['text_secondary']}; line-height: 1.6; }}
    </style>
</head>
<body>
    <section class="hero">
        <h1>{title}</h1>
        <p>在这里描述你的产品核心价值主张，用一两句话打动用户。</p>
        <button class="btn btn-primary">立即开始</button>
    </section>
    <section class="features">
        <div class="feature-card">
            <h3>功能一</h3>
            <p>描述这个功能如何帮助用户解决问题。</p>
        </div>
        <div class="feature-card">
            <h3>功能二</h3>
            <p>描述这个功能的独特优势和价值。</p>
        </div>
        <div class="feature-card">
            <h3>功能三</h3>
            <p>描述这个功能带来的具体收益。</p>
        </div>
    </section>
</body>
</html>"""


def dashboard_template(colors: dict, title: str) -> str:
    return f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title}</title>
    <style>
        * {{ margin: 0; padding: 0; box-sizing: border-box; }}
        body {{ font-family: -apple-system, 'Segoe UI', sans-serif; background: {colors['bg'] if not colors['bg'].startswith('linear') else colors['bg']}; color: {colors['text']}; }}
        .layout {{ display: grid; grid-template-columns: 240px 1fr; min-height: 100vh; }}
        .sidebar {{
            background: {colors['surface']}; border-right: 1px solid {colors['border']};
            padding: 1.5rem; display: flex; flex-direction: column; gap: 0.5rem;
        }}
        .sidebar h2 {{ font-size: 1.25rem; margin-bottom: 1rem; color: {colors['primary']}; }}
        .nav-item {{
            padding: 0.75rem 1rem; border-radius: 0.5rem; cursor: pointer;
            color: {colors['text_secondary']}; transition: all 0.15s;
        }}
        .nav-item:hover, .nav-item.active {{ background: {colors['primary']}; color: white; }}
        .main {{ padding: 2rem; }}
        .main h1 {{ font-size: 1.75rem; margin-bottom: 1.5rem; }}
        .stats {{ display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.5rem; margin-bottom: 2rem; }}
        .stat-card {{
            background: {colors['surface']}; border: 1px solid {colors['border']};
            border-radius: 0.75rem; padding: 1.5rem;
        }}
        .stat-card .label {{ font-size: 0.875rem; color: {colors['text_secondary']}; }}
        .stat-card .value {{ font-size: 2rem; font-weight: 700; margin: 0.5rem 0; }}
        .stat-card .change {{ font-size: 0.875rem; color: #10b981; }}
        .chart-area {{
            background: {colors['surface']}; border: 1px solid {colors['border']};
            border-radius: 0.75rem; padding: 1.5rem; min-height: 300px;
            display: flex; align-items: center; justify-content: center;
            color: {colors['text_secondary']};
        }}
    </style>
</head>
<body>
    <div class="layout">
        <aside class="sidebar">
            <h2>{title}</h2>
            <div class="nav-item active">仪表盘</div>
            <div class="nav-item">数据分析</div>
            <div class="nav-item">用户管理</div>
            <div class="nav-item">系统设置</div>
        </aside>
        <main class="main">
            <h1>仪表盘概览</h1>
            <div class="stats">
                <div class="stat-card">
                    <div class="label">总用户数</div>
                    <div class="value">12,345</div>
                    <div class="change">↑ 12.5%</div>
                </div>
                <div class="stat-card">
                    <div class="label">活跃用户</div>
                    <div class="value">8,901</div>
                    <div class="change">↑ 8.2%</div>
                </div>
                <div class="stat-card">
                    <div class="label">今日请求</div>
                    <div class="value">45.6K</div>
                    <div class="change">↑ 15.3%</div>
                </div>
                <div class="stat-card">
                    <div class="label">响应时间</div>
                    <div class="value">120ms</div>
                    <div class="change">↓ 5.1%</div>
                </div>
            </div>
            <div class="chart-area">图表区域 — 在此接入 ECharts / Chart.js</div>
        </main>
    </div>
</body>
</html>"""


LAYOUT_GENERATORS = {
    "landing": landing_template,
    "dashboard": dashboard_template,
}


def main():
    parser = argparse.ArgumentParser(description="HTML/CSS 设计模板生成器")
    parser.add_argument("--style", choices=list(COLOR_SCHEMES.keys()), default="modern", help="设计风格")
    parser.add_argument("--layout", choices=list(LAYOUT_GENERATORS.keys()), default="landing", help="布局类型")
    parser.add_argument("--title", type=str, default="我的项目", help="页面标题")
    parser.add_argument("--output", type=str, help="输出 HTML 文件路径")
    parser.add_argument("--list-styles", action="store_true", help="列出所有可用风格")
    args = parser.parse_args()

    if args.list_styles:
        for name, colors in COLOR_SCHEMES.items():
            print(f"\n{name}:")
            for k, v in colors.items():
                print(f"  {k}: {v}")
        return

    colors = COLOR_SCHEMES[args.style]
    generator = LAYOUT_GENERATORS[args.layout]
    html = generator(colors, args.title)

    if args.output:
        Path(args.output).parent.mkdir(parents=True, exist_ok=True)
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(html)
        print(f"✅ 模板已生成: {args.output}")
    else:
        print(html)


if __name__ == "__main__":
    main()

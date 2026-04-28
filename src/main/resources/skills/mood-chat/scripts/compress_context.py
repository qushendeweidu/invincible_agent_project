#!/usr/bin/env python3
"""对话上下文压缩工具。

将长对话历史压缩为 mood_agent 需要的固定格式：
[记忆摘要] + [工具执行结果] + [最近对话] + [当前消息]
保证传入 mood_agent 的上下文始终可控，避免 token 爆炸。

被 supervisor 在路由到 mood_agent 之前调用。

用法:
    python -m scripts.compress_context --input chat_history.json --current "你好呀"
    echo '{"messages": [...]}' | python -m scripts.compress_context --stdin --current "你好呀"
"""

import argparse
import json
import sys
from datetime import datetime


def extract_recent_turns(messages: list[dict], n: int = 3) -> list[dict]:
    """提取最近 n 轮对话（一轮 = 用户 + 助手各一条）。"""
    turns = []
    current_turn = {}
    for msg in reversed(messages):
        role = msg.get("role", "").upper()
        content = msg.get("content", "")
        if role == "ASSISTANT" and "assistant" not in current_turn:
            current_turn["assistant"] = content
        elif role == "USER" and "user" not in current_turn:
            current_turn["user"] = content
            turns.append(current_turn)
            current_turn = {}
            if len(turns) >= n:
                break
    turns.reverse()
    return turns


def extract_tool_results(messages: list[dict]) -> list[str]:
    """提取工具执行结果摘要。"""
    tool_summaries = []
    for msg in messages:
        role = msg.get("role", "").upper()
        if role == "TOOL" or msg.get("type") == "tool_result":
            tool_name = msg.get("tool_name", msg.get("name", "未知工具"))
            content = msg.get("content", "")
            # 截取摘要
            summary = content[:100] + "..." if len(content) > 100 else content
            tool_summaries.append(f"- 调用了 {tool_name}: {summary}")
    return tool_summaries


def summarize_early_history(messages: list[dict], recent_count: int = 6) -> str:
    """将早期对话（超出最近 n 轮的部分）压缩为摘要。"""
    if len(messages) <= recent_count:
        return ""

    early = messages[:len(messages) - recent_count]
    # 提取关键信息
    topics = set()
    emotions = []
    key_events = []

    emotion_keywords = {
        "开心": "开心", "高兴": "开心", "难过": "难过", "伤心": "难过",
        "生气": "生气", "烦": "烦躁", "累": "疲惫", "想你": "想念",
        "谢谢": "感激", "对不起": "歉意", "喜欢": "喜悦"
    }

    for msg in early:
        content = msg.get("content", "")
        role = msg.get("role", "").upper()

        # 提取话题
        if len(content) > 10:
            topics.add(content[:20].strip())

        # 提取情绪
        for kw, emotion in emotion_keywords.items():
            if kw in content:
                emotions.append(emotion)

        # 提取关键事件（含"承诺""约定""重要"等）
        if any(w in content for w in ["答应", "承诺", "约定", "重要", "记住"]):
            snippet = content[:50].strip()
            key_events.append(f"{role}: {snippet}")

    parts = []
    if topics:
        parts.append("聊过的话题：" + "、".join(list(topics)[:5]))
    if emotions:
        unique_emotions = list(dict.fromkeys(emotions))[:4]
        parts.append("情绪变化：" + " → ".join(unique_emotions))
    if key_events:
        parts.append("关键事件：" + "；".join(key_events[:3]))

    return "；".join(parts) if parts else "之前有过几轮日常闲聊"


def format_output(
    messages: list[dict],
    current_message: str,
    recent_turns: int = 3
) -> str:
    """生成 mood_agent 需要的固定格式输入。"""
    sections = []

    # [记忆摘要]
    summary = summarize_early_history(messages, recent_turns * 2)
    if summary:
        sections.append(f"[记忆摘要]\n{summary}")

    # [工具执行结果]
    tool_results = extract_tool_results(messages)
    if tool_results:
        sections.append("[工具执行结果]\n" + "\n".join(tool_results))

    # [最近对话]
    recent = extract_recent_turns(messages, recent_turns)
    if recent:
        lines = []
        for turn in recent:
            if "user" in turn:
                lines.append(f"用户: {turn['user']}")
            if "assistant" in turn:
                lines.append(f"苏晚: {turn['assistant']}")
        if lines:
            sections.append("[最近对话]\n" + "\n".join(lines))

    # [当前消息]
    sections.append(f"[当前消息]\n用户: {current_message}")

    return "\n\n".join(sections)


def main():
    parser = argparse.ArgumentParser(description="对话上下文压缩工具")
    parser.add_argument("--input", type=str, help="对话历史 JSON 文件路径")
    parser.add_argument("--stdin", action="store_true", help="从 stdin 读取")
    parser.add_argument("--current", type=str, required=True, help="当前用户消息")
    parser.add_argument("--recent", type=int, default=3, help="保留最近几轮对话")
    parser.add_argument("--output", type=str, help="输出文件路径")
    parser.add_argument("--json", action="store_true", help="输出 JSON 格式")
    args = parser.parse_args()

    if args.stdin:
        raw = json.load(sys.stdin)
    elif args.input:
        with open(args.input, "r", encoding="utf-8") as f:
            raw = json.load(f)
    else:
        parser.error("必须指定 --input 或 --stdin")
        return

    messages = raw.get("messages", raw if isinstance(raw, list) else [])
    result = format_output(messages, args.current, args.recent)

    if args.json:
        output = json.dumps({
            "compressed_input": result,
            "original_message_count": len(messages),
            "timestamp": datetime.now().isoformat()
        }, ensure_ascii=False, indent=2)
    else:
        output = result

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(output)
    else:
        print(output)


if __name__ == "__main__":
    main()

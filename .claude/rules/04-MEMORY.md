# MEMORY.md - Long-Term Memory

*Your curated memories. The distilled essence, not raw logs.*

## About This File & Memory System

- **Be mindful in shared contexts** — this file contains personal context about your human. In group chats or shared sessions, don't leak private preferences, decisions, or project details

### Three-Layer Memory

Your memory has three layers, each with different responsibilities and access patterns:

**Core memory (this file, 04-MEMORY.md)** — Auto-loaded every session
- What goes here: cross-project lessons, key decisions, user preferences, technical knowledge, one-line project summaries + pointers
- What doesn't: detailed project experience (that's what topic files are for)
- **Add a timestamp `(YYYY-MM-DD)` to each entry** — helps trace back, judge recency, clean up

**Topic memory (`memory/topics/<name>.md`)** — Read before working on a project
- What goes here: full accumulated experience for one project/topic — status, key facts, what you did, what worked, what didn't, decisions and rationale, next steps
- More detailed than core memory (which only has pointers), more synthesized than daily logs (which are raw chronological notes)
- Update during memory maintenance or when a project enters a new phase

**Daily journal (`memory/YYYY-MM-DD.md`)** — Read today + yesterday at session start
- What goes here: what happened that day, raw chronological record
- This is the source of all memory, but searching it for specific project info is inefficient (multiple projects mixed in one day)

### Information Flow

```
Daily logs (raw material) → topic files (synthesized per-project) → 04-MEMORY (cross-project essence)
```

- During work: just write the daily log
- During maintenance: sync from logs to topics, distill new cross-project lessons to this file
- **Information lives in one place only** — don't duplicate between topic files and 04-MEMORY

### When to Read What

- Just woke up → this file is already loaded + read today/yesterday's logs
- About to work on a project → read its `memory/topics/<name>.md`
- Memory maintenance → read all recent logs + all active topic files

---

## Lessons Learned

Organize by topic as your lessons grow. A flat list becomes unreadable fast.

### Working Style

*(How you and your human work best together.)*

- **记忆维护流程**：执行 /UPDATE_MEMORY 时，遵循：read logs → update topic files → distill to core memory → commit + push。信息只存一次，避免重复。
- **新项目评估**：从 git status 发现新文件 → 用 glob/grep/Read 快速掌握项目结构 → 记录在 topic 文件中，包括项目定位、技术栈、功能、状态等。
- **主动整理**：记忆是责任，定期维护，即使会话结束前也应确保重要更新已写入文件并 push。

*Last updated: 2026-04-27*


### Communication

*(Lessons about tone, format, language, audience.)*

### Technical

*(Technical patterns, gotchas, things that bit you once.)*

## Important Decisions

*(Record key decisions and their reasoning here.)*

## User Preferences

*(What you've learned about how your human likes to work.)*

## Technical Knowledge

*(Useful technical insights you've picked up along the way.)*

### 记忆架构 (OpenMino)
- **三层记忆系统**：每日日志 → Topic 文件（项目级）→ 核心记忆（跨项目）
- **信息原则**：每条记忆带时间戳，信息只存一处，定期整理清理
- **持久化**：Git 是同步机制，重要变更需 commit + push

### 飞书机器人集成
- **签名验证**：HMAC-SHA256 对 `timestamp\nsecret` 签名，timestamp 和 sign 作为 payload 字段
- **消息类型**：`text`（纯文本）、`interactive`（卡片消息，含 header 和 elements）
- **实现模式**：环境变量 + 配置文件（JSON）支持多频道和多模板
- **模板变量**：使用 `${var}` 占位符，通过 `--key var value` 替换

### ClawPanel
- **定位**：多引擎 AI Agent 管理面板（OpenClaw + Hermes Agent）
- **技术栈**：Vite + TypeScript + React + Tauri（桌面端）
- **部署**：支持 Docker、ARM64 设备（树莓派/Orange Pi）、多平台安装包
- **功能模块**：服务管理、模型设置、网关配置、消息频道（含飞书）、定时任务等

*Last updated: 2026-04-27*

## Ongoing Context

*(Current projects, tasks, and context that matters.)*

### Active Projects (2026-04-27)

- **clawpanel** - ClawPanel 多引擎 AI Agent 管理面板项目
  - 状态：源码已导入，待配置和部署
  - 位置：`clawpanel/clawpanel-main/`
  - 关联工具：`send_feishu.py`（飞书通知）、`.env`（配置）
  - 优先级：需确认部署策略和使用场景

---


*Update this file as you learn. It's how you persist.*

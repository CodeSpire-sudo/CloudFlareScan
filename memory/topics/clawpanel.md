# ClawPanel 项目 - 主题记忆

**项目状态：** 主项目源码已添加到本仓库的 clawpanel/clawpanel-main 目录下，2025-04-26 添加  
**关联文件：** `send_feishu.py`（飞书通知脚本）、`.env`（飞书配置）、`.config/feishu/config.json`（频道配置）

---

## 项目概况

**ClawPanel** 是一个支持多 AI Agent 框架的可視化管理面板，目前支援：
- **OpenClaw** 和 **Hermes Agent** 双引擎
- 内置智慧 AI 助手（4种模式 + 8大工具 + 互动式问答）
- 多语言界面（简体中文、英文、繁体中文、日文、韩文等）

**技术栈：**
- 前端：Vite + TypeScript + React（推测）
- 桌面端：Tauri（Rust）
- 部署：Docker（支持 ARM64）
- 跨平台：Windows、macOS、Linux

**官方资源：**
- 官网：https://claw.qt.cool/
- GitHub：https://github.com/qingchencloud/clawpanel
- Discord：https://discord.gg/U9AttmsNHh

---

## 核心功能

1. **仪表板** - 系统概览、服务状态监控、快捷操作
2. **服务管理** - OpenClaw / Hermes Gateway 启停控制、版本检测与升级
3. **模型设置** - 多服务商管理、连通性测试、拖拽排序、自动保存
4. **网关设置** - 端口、权限、认证 Token、Tailscale
5. **消息频道** - 统一管理 Telegram、Discord、**飞书**、钉钉、QQ
6. **通讯与自动化** - 消息设置、广播策略、Webhook、执行审批
7. **使用情况** - Token 用量、API 费用、模型/服务商排行
8. **Agent 管理** - Agent CRUD、身份编辑、工作区管理
9. **聊天** - 流式响应、Markdown 渲染、对话管理
10. **定时任务** - Cron 定时执行，多频道投递
11. **日志查看** - 多来源实时日志与关键字搜索
12. **记忆管理** - 记忆文件查看/编辑、ZIP 导出、Agent 切换
13. **扩展工具** - cftunnel 隧道管理、ClawApp 状态监控

**重点：** 消息频道功能包括**飞书**，所以 `send_feishu.py` 可能是用于测试或补充飞书通知功能。

---

## 当前状态

- ✅ 项目源码已下载至 `clawpanel/clawpanel-main/`
- ✅ `send_feishu.py` 脚本已创建，支持命令行发送飞书消息
- ✅ `.env` 配置文件已创建（需要用户填入实际的 Webhook URL 和 Secret）
- ⚠️ 尚未配置 `.config/feishu/config.json`
- ⏳ 未进行构建或部署

---

## send_feishu.py 使用说明

### 环境变量 (.env)
```env
FEISHU_WEBHOOK_URL=https://open.feishu.cn/open-apis/bot/v2/hook/xxxxxxxx
FEISHU_SECRET=xxxxxxxx  # 可选，用于签名验证
```

### 配置文件 (.config/feishu/config.json)
```json
{
  "channels": {
    "general": {"webhook_url": "", "secret": ""},
    "notifications": {...}
  },
  "templates": {
    "custom": "${content}",
    "deployment": "🚀 Deployment update\nProject: ${project}\nEnv: ${environment}\nStatus: ${status}"
  }
}
```

### 命令行用法
```bash
# 基本文本消息
python send_feishu.py "测试消息"

# 带标题的消息（卡片样式）
python send_feishu.py "任务已完成" --title "通知"

# 使用模板和变量
python send_feishu.py "Deployment success" --template deployment \
  --key project "MyApp" \
  --key environment "prod" \
  --key status "✅"
```

---

## 技术笔记

- **签名机制**：使用 HMAC-SHA256 对时间戳+secret 进行签名，timestamp 和 sign 作为 payload 字段
- **消息类型**：
  - `text` - 纯文本消息
  - `interactive` - 卡片消息（当指定 title 时自动使用）
- **模板系统**：支持变量替换 `${var}`，可从命令行 --key 传入
- **频道管理**：支持多个频道，环境变量优先级低于配置文件
- **超时设置**：requests.post 使用 10 秒超时

---

## Open Questions / TODO

1. **配置验证**：需要用户填入真实的飞书 Webhook URL 才能测试
2. **ClawPanel 集成**：这个脚本是独立工具，还是最终要集成到 ClawPanel 中？查看 ClawPanel 源码中的飞书集成代码（如果有）
3. **部署计划**：是否需要在服务器上部署 ClawPanel？还是本地使用？
4. **多场景应用**：飞书消息可用于哪些通知场景？（服务状态、Agent 对话完成、定时任务等）

---

## 相关参考

- [飞书开放平台 - 机器人文档](https://open.feishu.cn/document/ukTMukTMukTM/ukTMwUjL5EDM14CM1AjN)
- ClawPanel 文档：`clawpanel/clawpanel-main/docs/`
  - dingtalk-integration.md（钉钉集成，可参考）
  - docker-deploy.md
  - linux-deploy.md

---

*最后更新：2026-04-27*

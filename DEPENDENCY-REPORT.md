# OpenMino 环境依赖报告

**日期:** 2025-04-26  
**系统:** Windows 11  
**Agent:** 守亿 ⚡

---

## 环境概览

| 组件 | 状态 | 详情 |
|------|------|------|
| **Python** | ✅ 已安装 | 3.14.4 |
| **Node.js** | ✅ 已安装 | v24.15.0 |
| **npm** | ✅ 已安装 | 11.12.1 |
| **pip** | ✅ 已安装 | 26.0.1 |

---

## 缺失依赖总览

目前系统缺少大多数技能所需的外部工具和库。以下是分类清单。

### 🔴 关键缺失：Python 包（19 个技能中的 6 个需要）

这些是处理文档和数据技能的核心依赖。

```bash
# PDF 处理
pip install pypdf pdfplumber reportlab

# 表格数据和 Excel
pip install pandas openpyxl

# PowerPoint 处理
pip install "markitdown[pptx]" Pillow

# OCR 扫描 PDF
pip install pytesseract pdf2image

# 下载和媒体
pip install gallery-dl spotdl
```

---

### 🔴 关键缺失：Node.js 全局包（2 个技能需要）

```bash
# Word 文档创建
npm install -g docx

# PowerPoint 创建
npm install -g pptxgenjs
```

---

### 🔴 关键缺失：命令行工具（多技能需要）

#### GitHub 操作 (github skill)
```bash
# 安装 GitHub CLI
winget install --id GitHub.cli
# 或从 https://cli.github.com/ 下载
```

#### 下载工具 (download-anything skill)
```bash
# 视频/音频下载
pip install yt-dlp

# 多线程下载和 BT
winget install --id aria2formacos.aria2
# 或: choco install aria2

# 图片批量下载 (Python pip)
pip install gallery-dl

# Spotify 下载
pip install spotdl

# 备用下载器
winget install --id GNU.Wget
# 或: choco install wget
```

#### 媒体处理 (pdf, pptx, download-anything)
```bash
# 视频/音频转换
winget install --id Gyan.FFmpeg
# 或: choco install ffmpeg
```

#### 实用工具
```bash
# JSON 处理
winget install --id jqlang.jq
# 或: choco install jq

# 文档转换
winget install --id JohnMacFarlane.Pandoc
# 或: choco install pandoc
```

#### PDF 命令行工具 (pdf skill)
```bash
# Poppler (pdftotext, pdftoppm, pdfimages)
winget install --id yshui.Poppler
# 或: choco install poppler

# qpdf (PDF 操作)
winget install --id qpdf.qpdf
# 或: choco install qpdf

# pdftk (可选，备选方案)
winget install --id IdPandaz.QPDFToolkit
```

---

### ⚠️ macOS 专属技能（Windows 不可用）

以下技能仅在 macOS 上可用，在 Windows 上无法使用：

| 技能 | 工具 | 用途 |
|------|------|------|
| `apple-notes` | `memo` | 管理 Apple 备忘录 |
| `apple-reminders` | `remindctl` | 管理 Apple 提醒事项 |
| `imsg` | `imsg` | 发送 iMessage/SMS |
| `peekaboo` | `peekaboo` | macOS UI 自动化 |

这些技能需要：
- macOS 操作系统
- 相应的应用程序权限（Full Disk Access, Accessibility）
- 通过 Homebrew 安装（例如 `brew install steipete/tap/bird`）

---

### ✅ 无需额外依赖的技能

以下技能仅需核心运行环境（Claude Code）即可使用：

- auto-expert
- task-implement
- task-alignment
- ultra-research
- summarize
- skill-creator
- remotion-best-practices
- pdf（需要 Python 包，但这些是额外依赖）
- xlsx（需要 Python 包）
- docx（需要 Python 和 Node 包）
- pptx（需要 Python 和 Node 包）

---

## 安装建议

根据你的使用场景，推荐安装优先级：

### 优先级 1（高频使用，建议立即安装）
1. **GitHub CLI** (gh) - 如果你使用 GitHub
2. **Python 包**: pypdf, pdfplumber, pandas, openpyxl, markitdown, Pillow
3. **Node 包**: docx, pptxgenjs
4. **Poppler** (pdftotext, pdftoppm) - PDF 处理必备

### 优先级 2（偶尔使用）
5. **ffmpeg** - 媒体处理
6. **yt-dlp** - 视频下载
7. **Gallery-dl & spotdl** - 图片和音乐下载
8. **jq** - JSON 处理
9. **pandoc** - 文档格式转换
10. **qpdf** - PDF 操作

---

## 综合一键安装脚本

我们可以在 Windows 上使用 Winget（Windows Package Manager）或 Chocolatey 批量安装。

**前提条件：**
- 以管理员身份运行 PowerShell 或命令提示符
- 确保 winget 或 choco 已安装

**Winget 一键安装脚本：**

```powershell
# 命令行工具
winget install --id GitHub.cli
winget install --id yshui.Poppler
winget install --id qpdf.qpdf
winget install --id Gyan.FFmpeg
winget install --id jqlang.jq
winget install --id JohnMacFarlane.Pandoc

# 需要手动确认的应用
winget install --id aria2formacos.aria2
winget install --id GNU.Wget
```

**Chocolatey 一键安装脚本：**

```powershell
choco install gh
choco install poppler
choco install qpdf
choco install ffmpeg
choco install jq
choco install pandoc
choco install aria2
choco install wget
```

**Python 包批量安装：**

```powershell
pip install pypdf pdfplumber reportlab pandas openpyxl markitdown[pptx] Pillow pytesseract pdf2image yt-dlp gallery-dl spotdl
```

**Node 全局包：**

```powershell
npm install -g docx pptxgenjs
```

---

## 环境检查命令汇总

```bash
# 验证安装
gh --version
yt-dlp --version
ffmpeg -version
aria2c --version
jq --version
pandoc --version
pdftotext -v

# 验证 Python 包
python -c "import pypdf; print('pypdf OK')"
python -c "import pandas; print('pandas OK')"
```

---

## 其他说明

1. **macOS 专属工具**：如果未来迁移到 macOS，可以通过 Homebrew 安装相关工具（memo, remindctl, imsg, peekaboo, bird）
2. **LibreOffice**：某些技能（docx, pptx）可能需要 LibreOffice 进行 PDF 转换。可从 https://www.libreoffice.org/ 下载安装
3. **OCR 支持**：pytesseract 需要单独安装 Tesseract OCR 引擎：https://github.com/tesseract-ocr/tesseract
4. **配置文件**：技能使用前可能需要配置 credentials（如 GitHub CLI 需 `gh auth login`，bird 需浏览器 cookies）

---

## 下一步

请回复：
1. **Y** - 我要安装推荐的依赖（提供一站式安装脚本）
2. **N** - 暂时不安装，我自己手动配置
3. **CUSTOM** - 我只安装指定的依赖（列出你需要的）

等待你的指示。

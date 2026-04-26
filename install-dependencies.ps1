# OpenMino 依赖一键安装脚本
# 适用于 Windows 11 + Winget + pip + npm
# 使用方法：以管理员身份运行 PowerShell，执行此脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " OpenMino 依赖安装脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否为管理员
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "警告：建议以管理员身份运行此脚本，以便安装系统级工具。" -ForegroundColor Yellow
    Write-Host "   按回车继续（可能某些工具安装会失败），或 Ctrl+C 退出后以管理员身份重新运行。" -ForegroundColor Yellow
    Read-Host
}

Write-Host "正在安装依赖..." -ForegroundColor Green
Write-Host ""

# ============================================================================
# 1. Winget 命令行工具
# ============================================================================
Write-Host "[1/3] 正在通过 Winget 安装命令行工具..." -ForegroundColor Cyan

$wingetApps = @(
    "GitHub.cli",           # GitHub CLI (gh)
    "yshui.Poppler",        # Poppler (pdftotext, pdftoppm, pdfimages)
    "qpdf.qpdf",            # qpdf
    "Gyan.FFmpeg",          # FFmpeg (视频音频处理)
    "jqlang.jq",            # jq (JSON 处理)
    "JohnMacFarlane.Pandoc",# Pandoc (文档转换)
    "aria2formacos.aria2",  # aria2 (多线程下载)
    "GNU.Wget"              # wget (备用下载器)
)

foreach ($app in $wingetApps) {
    Write-Host "   安装 $app ..." -NoNewline
    try {
        winget install --id $app -e --silent --accept-package-agreements --accept-source-agreements | Out-Null
        Write-Host " 完成" -ForegroundColor Green
    } catch {
        Write-Host " 可能需要手动安装或已安装" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "[2/3] 正在通过 pip 安装 Python 包..." -ForegroundColor Cyan

# ============================================================================
# 2. Python 包
# ============================================================================
$pythonPackages = @(
    "pypdf",
    "pdfplumber",
    "reportlab",
    "pandas",
    "openpyxl",
    "markitdown[pptx]",
    "Pillow",
    "pytesseract",
    "pdf2image",
    "yt-dlp",
    "gallery-dl",
    "spotdl"
)

foreach ($pkg in $pythonPackages) {
    Write-Host "   安装 $pkg ..." -NoNewline
    try {
        pip install $pkg --quiet
        Write-Host " 完成" -ForegroundColor Green
    } catch {
        Write-Host " 失败" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "[3/3] 正在通过 npm 安装全局包..." -ForegroundColor Cyan

# ============================================================================
# 3. Node.js 全局包
# ============================================================================
$npmPackages = @(
    "docx",
    "pptxgenjs"
)

foreach ($npmPkg in $npmPackages) {
    Write-Host "   安装 $npmPkg ..." -NoNewline
    try {
        $env:NPM_CONFIG_LOGLEVEL = 'silent'
        npm install -g $npmPkg 2>$null
        Write-Host " 完成" -ForegroundColor Green
    } catch {
        Write-Host " 失败" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 安装流程完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 验证
Write-Host "正在验证安装..." -ForegroundColor Yellow
Write-Host ""

$toolsToCheck = @(
    @{name="gh (GitHub CLI)"; command="gh --version"},
    @{name="pandoc"; command="pandoc --version"},
    @{name="jq"; command="jq --version"},
    @{name="ffmpeg"; command="ffmpeg -version"},
    @{name="yt-dlp"; command="yt-dlp --version"},
    @{name="aria2c"; command="aria2c --version"},
    @{name="pdftotext"; command="pdftotext -v"}
)

foreach ($tool in $toolsToCheck) {
    $result = Invoke-Expression "$($tool.command) 2>`$null" 2>&1
    if ($LASTEXITCODE -eq 0 -or $result) {
        Write-Host "   [OK] $($tool.name)" -ForegroundColor Green
    } else {
        Write-Host "   [?] $($tool.name) - 未找到或安装失败" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Python 包验证："
$pyPackagesToCheck = @("pypdf", "pdfplumber", "pandas", "openpyxl")
foreach ($pkg in $pyPackagesToCheck) {
    try {
        python -c "import $pkg" 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   [OK] $pkg" -ForegroundColor Green
        } else {
            Write-Host "   [FAIL] $pkg" -ForegroundColor Red
        }
    } catch {
        Write-Host "   [FAIL] $pkg" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "后续步骤："
Write-Host " 1. 如果需要使用 GitHub，运行: gh auth login"
Write-Host " 2. 如果需要 OCR 功能，从 https://github.com/tesseract-ocr/tesseract 安装 Tesseract OCR 引擎"
Write-Host " 3. 如果某些工具安装失败，请手动从官网下载或使用 Chocolatey: https://chocolatey.org/install"
Write-Host ""
Write-Host "OpenMino 环境搭建完成！" -ForegroundColor Green

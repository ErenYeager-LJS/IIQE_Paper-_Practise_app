# IIQE Paper I & III Practice App

[English](#english) | [中文](#中文)

An offline Android practice app for the Hong Kong Insurance Intermediaries Qualifying Examination (IIQE) Paper I and Paper III.

## 中文

### 功能

- **分卷学习**：Paper I（保险原理与实务）和 Paper III（长期保险）独立记录学习进度。
- **全部过一遍**：每天分别设置每卷的新题数量和温习数量，按题库顺序推进，并优先复习已做过的题目。
- **错题回顾**：答错后自动保存于本机，可按试卷集中重新练习。
- **模拟考试**：每次随机抽取题目，内置倒计时、自动评分和本地成绩记录。
- **本地优先**：题库、答题记录、每日进度和历史成绩均保存在设备 SQLite 数据库中。没有账号、广告、分析 SDK 或网络权限。

### 题库

应用包含以下离线题库数据：

| 试卷 | 题目数 |
| --- | ---: |
| Paper I | 2525 |
| Paper III | 1999 |

题库由 `Paper1+3_line.xlsx` 导入生成，导入脚本位于 `tools/import_questions.ps1`。原始 Excel 不会提交到此仓库。

### 模拟考试默认值

| 试卷 | 题数 | 倒计时 | 模拟及格线 |
| --- | ---: | ---: | ---: |
| Paper I | 75 | 120 分钟 | 70% |
| Paper III | 50 | 75 分钟 | 70% |

题数已由随附学习资料的章节比重表核对。考试安排可能调整，请在报名前以 IIQE 最新考生通知为准。规则集中在 [EXAM_RULES.md](EXAM_RULES.md)，方便后续更新。

### 安装 APK

1. 在 Android 设备下载 `app-debug.apk`。
2. 如系统询问，允许浏览器或文件管理器安装“未知应用”。
3. 安装后打开 **IIQE 练习**，选择 Paper I 或 Paper III 即可开始。

本项目生成的是 debug 签名 APK，只适合个人安装和测试。正式发布前请使用自己的发布密钥签名。

### 从源码构建

要求：JDK 17、Android SDK Platform 33、Android Build Tools 33.0.2。

```powershell
$env:ANDROID_SDK_ROOT = 'D:\Android\Sdk'
.\gradlew.bat assembleDebug
```

构建产物位于：

```text
app\build\outputs\apk\debug\app-debug.apk
```

### 更新题库

将新的横向格式工作簿作为输入，运行：

```powershell
.\tools\import_questions.ps1 `
  -Workbook 'C:\path\to\Paper1+3_line.xlsx' `
  -Output '.\app\src\main\assets\questions.json'
```

脚本会验证 Paper I 为 2525 题、Paper III 为 1999 题，避免意外导入不完整题库。修改题库后请重新构建 APK。

### 隐私与数据

- 所有学习数据仅保存在设备本机。
- 卸载应用会删除本机学习记录，除非设备系统另行备份应用数据。
- 不会上传答题、错题或成绩数据。

### 目录结构

```text
app/src/main/assets/questions.json     离线题库
app/src/main/java/com/iiqe/study/      Android 应用逻辑
app/src/main/res/                      Android 资源与主题
tools/import_questions.ps1             Excel 题库导入工具
EXAM_RULES.md                          模拟考试规则说明
tokens.css                             界面设计令牌
```

## English

### Features

- **Separate papers**: Paper I (Principles and Practice of Insurance) and Paper III (Long Term Insurance) maintain independent study progress.
- **Complete the bank**: Set a per-paper daily quota for new questions and review questions, then work through the bank in sequence.
- **Wrong-answer review**: Incorrect responses are stored locally and can be practised again by paper.
- **Mock exams**: Each session randomly draws a paper, runs a countdown, scores the attempt, and saves the result locally.
- **Offline by design**: The question bank, progress, incorrect answers, and results are stored in a device-local SQLite database. There are no accounts, ads, analytics SDKs, or network permissions.

### Question bank

| Paper | Questions |
| --- | ---: |
| Paper I | 2525 |
| Paper III | 1999 |

`questions.json` is generated from `Paper1+3_line.xlsx` by `tools/import_questions.ps1`. The original Excel workbook is intentionally not committed to this repository.

### Mock-exam defaults

| Paper | Questions | Timer | Practice pass line |
| --- | ---: | ---: | ---: |
| Paper I | 75 | 120 minutes | 70% |
| Paper III | 50 | 75 minutes | 70% |

The bundled study guides confirm the question totals. Confirm the latest IIQE candidate notice before taking the live examination, because examination arrangements may change. Centralised defaults are documented in [EXAM_RULES.md](EXAM_RULES.md).

### Install the APK

1. Download the generated `app-debug.apk` onto an Android device.
2. Permit the browser or file manager to install unknown apps when Android asks.
3. Open **IIQE Practice** and choose Paper I or Paper III.

The generated APK uses a debug certificate and is intended for personal installation and testing. Sign it with your own release key before public distribution.

### Build from source

Prerequisites: JDK 17, Android SDK Platform 33, and Android Build Tools 33.0.2.

```powershell
$env:ANDROID_SDK_ROOT = 'D:\Android\Sdk'
.\gradlew.bat assembleDebug
```

The debug APK is written to:

```text
app\build\outputs\apk\debug\app-debug.apk
```

### Refresh the question bank

```powershell
.\tools\import_questions.ps1 `
  -Workbook 'C:\path\to\Paper1+3_line.xlsx' `
  -Output '.\app\src\main\assets\questions.json'
```

The import script validates 2525 Paper I questions and 1999 Paper III questions before writing the Android asset. Rebuild the APK after updating the asset.

### Privacy

- All study data stays on the device.
- Uninstalling the app removes study records unless the device restores app data from a system backup.
- The app does not upload answers, errors, or score history.

### Project layout

```text
app/src/main/assets/questions.json     Offline question bank
app/src/main/java/com/iiqe/study/      Android application code
app/src/main/res/                      Android resources and theme
tools/import_questions.ps1             Excel import utility
EXAM_RULES.md                          Mock-exam rule notes
tokens.css                             UI design tokens
```

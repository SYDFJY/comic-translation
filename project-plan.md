# 漫画翻译器 — 项目计划书

**MangaTranslator · 基于 JavaFX 的桌面漫画翻译应用**

> 南昌航空大学 · 软件工程 2025 级暑期实训  
> 版本 2.0 · 2026 年 7 月 10 日

---

## 目录

1. [项目概述与目标](#1-项目概述与目标)
2. [需求分析](#2-需求分析)
3. [系统架构设计](#3-系统架构设计)
4. [核心处理管线详细设计](#4-核心处理管线详细设计)
5. [百度 API 集成方案](#5-百度-api-集成方案)
6. [UI 界面详细设计](#6-ui-界面详细设计)
7. [数据模型与接口定义](#7-数据模型与接口定义)
8. [项目结构与代码组织](#8-项目结构与代码组织)
9. [开发计划与里程碑](#9-开发计划与里程碑)
10. [测试方案](#10-测试方案)
11. [风险评估与应对](#11-风险评估与应对)
12. [交付清单](#12-交付清单)

---

## 1. 项目概述与目标

### 一句话定义

一款基于 **JavaFX** 的桌面漫画翻译应用，通过调用 **百度 OCR + 翻译 API**，实现漫画图片中文字的自动识别、翻译、擦除和回填渲染，输出翻译后的漫画图片。参考 DeepL / Google Translate 等主流翻译软件的交互范式，提供清晰的双栏对比、实时进度反馈和人工修正闭环。

### 核心目标

| 维度 | 目标 | 验收标准 |
|------|------|---------|
| 功能 | 自动翻译漫画文字 | 单张图片翻译完成率 ≥ 90%，文字无溢出气泡框 |
| 体验 | 人机协同修正 | 译文可逐条编辑，修正后一键重新渲染 |
| 效率 | 批量处理能力 | 10 张图片批量翻译 ≤ 5 分钟 |
| 技术 | 百度 API 稳定集成 | API 调用成功率 ≥ 95%，限流自动重试 |
| 可用 | 开箱即用 | 双击 JAR 即可运行，首次使用有引导配置 |

### 技术栈总览

| 类别 | 技术选型 |
|------|---------|
| **语言与框架** | JDK 17 (LTS), JavaFX 17.0.8, Maven 3.9+, Gson 2.10.1, Apache HttpClient 5.3 |
| **AI 服务** | 百度 OCR API (通用 + 高精度版), 百度翻译 API (标准版), 鉴权: API Key + access_token |
| **图片处理** | Java ImageIO / Graphics2D, OpenCV Java 可选(气泡检测), 站酷快乐体字体(回填) |

---

## 2. 需求分析

### 2.1 功能需求（18 项）

#### P0 — 核心基础（7 项）

| 编号 | 功能模块 | 功能描述 | 交互细节 |
|------|---------|---------|---------|
| F01 | **图片导入** | 打开单张/多张漫画图片 (JPG/PNG/BMP) | Ctrl+O 打开文件选择器；支持拖拽导入到画布区域；拖入文件夹自动识别其中所有图片 |
| F02 | **OCR 识别** | 调用百度 OCR 检测文字区域和内容 | 点击"翻译"按钮后自动触发；识别完成后文字区域在原图上用蓝色半透明框高亮标注 |
| F03 | **文字翻译** | 调用百度翻译 API 将识别文字翻译为中文 | 翻译进度在底部进度条实时更新；翻译完成后画布自动显示结果图 |
| F04 | **原文擦除** | 覆盖原图文字区域（白色/取色填充） | 修补策略在设置中可选：白色覆盖（默认）/ 取色填充 |
| F05 | **译文回填** | 将翻译文字渲染到原图对应位置 | 字号自适应算法自动计算；竖排文字逐字符竖排渲染 |
| F06 | **导出结果** | 导出翻译后图片为 JPG/PNG | Ctrl+S 保存当前图片；批量导出保存到指定文件夹；文件名规则：原名_translated.png |
| F07 | **双栏对比** | 原图与翻译结果左右对比显示 | 画布区域两个 Tab：原图 / 翻译结果；可切换也可同时显示（拆分视图模式） |

#### P1 — 体验提升（7 项）

| 编号 | 功能模块 | 功能描述 | 交互细节 |
|------|---------|---------|---------|
| F08 | **文字区域高亮** | 鼠标悬停时高亮对应气泡框 | 悬停画布上的文字区域，半透明蓝色矩形 (#60a5fa@0.3) 覆盖标注；同步在下方文本面板中高亮对应行 |
| F09 | **翻译修正** | 逐条修正机器翻译结果 | 双击文本面板中的译文行，弹出修正对话框；修正后自动重新渲染该区域；支持 Ctrl+Z 撤销修正 |
| F10 | **批量翻译** | 选择多张图片一键批量翻译 | 侧栏文件列表全选后点击"批量翻译"；进度面板显示总进度和当前文件；并发最多 3 个 API 请求 |
| F11 | **批量进度** | 批量翻译时显示进度和日志 | 底部进度条 + 百分比 + 日志区；日志支持展开/折叠；错误行红色标记 |
| F12 | **文件列表** | 侧栏显示已导入图片缩略图列表 | 侧栏文件列表带缩略图 (32×40px)；已翻译文件绿色 ✓ 标记；翻译中蓝色进度标记；右键菜单：删除/重翻译/导出 |
| F13 | **缩放与导航** | 画布区域支持缩放、拖动 | 鼠标滚轮缩放 (10%-500%)；缩放比例显示在画布右上角；Ctrl+0 恢复 100%；拖拽移动画布 |
| F14 | **拆分视图** | 原图和翻译结果同时并排显示 | 画布区域可切换为拆分视图（左原图右结果）；两视图缩放联动同步 |

#### P2 — 进阶完善（4 项）

| 编号 | 功能模块 | 功能描述 | 交互细节 |
|------|---------|---------|---------|
| F15 | **语言方向设置** | 选择源语言和目标语言 | 设置对话框中下拉选择；支持：日语→中文、英语→中文、韩语→中文、中文→英语 |
| F16 | **字体与字号** | 选择回填字体和字号策略 | 设置对话框中选择：站酷快乐体(默认)/微软雅黑/楷体；字号策略：自适应(默认)/固定大小 |
| F17 | **竖排文字** | 支持竖排漫画文字渲染 | OCR direction 字段为 1 时自动竖排渲染；从右到左逐列，每列从上到下逐字符 |
| F18 | **首次引导** | 首次启动时引导配置 API Key | 首次启动弹出引导对话框，填写百度 API Key；填完后验证 API 连通性；验证成功后进入主界面 |

### 2.2 非功能需求

| 类别 | 指标 | 目标值 | 测量方法 |
|------|------|-------|---------|
| 性能 | 单张图片翻译时间 | ≤ 10 秒 | 从点击翻译到结果图显示 |
| 性能 | UI 响应时间 | ≤ 200ms | 按钮点击到视觉反馈 |
| 可靠性 | API 调用成功率 | ≥ 95% | 含重试后的最终成功率 |
| 可靠性 | API 限流恢复 | ≤ 30 秒 | 限流后自动重试成功 |
| 兼容性 | 图片格式 | JPG/PNG/BMP | 各格式都能正常导入和翻译 |
| 安全 | API Key 存储 | 本地加密，不硬编码 | AES-128 加密存储到 config.json |

---

## 3. 系统架构设计

### 3.1 整体架构 — 4 层分层

```
┌─────────────────────────────────────────────────────────┐
│                     UI 层 (JavaFX)                      │
│  MainWindow / CanvasPanel / TextPanel / FileListPanel   │
│  SettingsDialog / CorrectionDialog                      │
├─────────────────────────────────────────────────────────┤
│                  Pipeline 层 (编排与进度)                 │
│  TranslationPipeline / PipelineContext / PipelineEventBus│
├─────────────────────────────────────────────────────────┤
│                 Service 层 (业务逻辑)                    │
│  OcrService / TranslateService / InpaintService          │
│  RenderService + Impl 实现类                             │
├─────────────────────────────────────────────────────────┤
│              Client 层 (百度 API HTTP 封装)               │
│  BaiduOcrClient / BaiduTranslateClient / BaiduAuthManager│
└─────────────────────────────────────────────────────────┘
```

### 3.2 分层职责

| 层级 | 职责 | 核心类 | 关键设计原则 |
|------|------|--------|------------|
| **UI** | 界面展示、用户交互、事件响应、进度可视化 | MainWindow, CanvasPanel, TextPanel, FileListPanel, SettingsDialog, CorrectionDialog | 所有 UI 操作通过事件总线通知 Pipeline；UI 不直接调用 Service |
| **Pipeline** | 编排 5 步翻译流程、管理上下文数据、处理异常和重试、发出进度事件 | TranslationPipeline, PipelineContext, PipelineEventBus | 每个步骤独立可替换；步骤间通过 PipelineContext 传递数据；异常在某步骤失败时停止 |
| **Service** | 各步骤的具体业务逻辑实现 | OcrService, TranslateService, InpaintService, RenderService | 接口 + 实现分离；可切换不同实现（如修补策略） |
| **Client** | 封装百度 API 的 HTTP 调用、鉴权签名、JSON 解析、错误码映射 | BaiduOcrClient, BaiduTranslateClient, BaiduAuthManager | 统一重试策略（最多 3 次，间隔 2s）；限流自动排队 |

---

## 4. 核心处理管线详细设计

### 4.1 管线流程

```
Step 1 [OCR 检测] → Step 2 [文本清洗] → Step 3 [AI 翻译] → Step 4 [图片修补] → Step 5 [文字回填]
```

### 4.2 Step 1 — OCR 检测

- **输入：** BufferedImage（漫画原图）
- **输出：** List\<TextRegion\> — 每个区域含坐标、尺寸、原文、方向

**处理流程：**
1. 将 BufferedImage 编码为 Base64 字符串
2. 构建百度 OCR POST 请求：`image=Base64, language_type=auto, detect_direction=true, detect_language=true`
3. 调用百度 OCR API（通用文字识别 / 高精度版，可在设置中切换）
4. 解析返回 JSON：提取 `words_result` 数组中每个元素的 words、location、direction
5. 对每个文字区域，将 location 坐标向外扩展 `expandMargin`（默认 12px）作为气泡覆盖区域
6. 过滤置信度过低的结果（words_probability < 0.5 的忽略）

**参数规格：**

| 参数 | 值 |
|------|-----|
| 气泡框扩展量 | expandMargin = 12px（可调 0-30px） |
| 置信度阈值 | minProbability = 0.5 |
| OCR 版本选择 | general_basic（默认）/ accurate_basic |
| 语言检测 | detect_language=true |

### 4.3 Step 2 — 文本清洗与合并

- **输入：** List\<TextRegion\>（来自 Step 1）
- **输出：** List\<TextRegion\>（清洗后，相邻区域已合并）

**处理流程：**
1. 去除噪声字符：正则过滤特殊符号 `[\u2000-\u206F\uFF00-\uFFEF]`
2. 合并相邻区域：如果两个区域的 y 坐标差 < 8px 且 x 轴重叠 > 60%，视为同一行，合并文字和坐标
3. 计算排版参数：横排区域 → `horizontal=true, textAlign=CENTER`；竖排区域 → `vertical=true`，列方向从右到左
4. 记录像素面积：`areaWidth × areaHeight`，用于字号自适应计算

**参数规格：**

| 参数 | 值 |
|------|-----|
| 同行合并阈值 | yDiffThreshold = 8px |
| 重叠判定阈值 | xOverlapRatio = 0.6 |
| 噪声字符正则 | `[\u2000-\u206F\uFF00-\uFFEF]` |

### 4.4 Step 3 — AI 翻译

- **输入：** List\<TextRegion\>（原文内容 + 语言信息）
- **输出：** List\<TextRegion\>（添加 translatedText 字段）

**翻译策略（根据文字区域数量）：**
- ≤ 5 条：逐条翻译（保证对齐关系）
- 6-20 条：按 5 条一组分批翻译
- > 20 条：按 10 条一组分批翻译

**参数规格：**

| 参数 | 值 |
|------|-----|
| 默认源语言 | jp（日语→中文） |
| 默认目标语言 | zh（中文） |
| 批量分隔符 | `\n` |
| 最大单次字符数 | 6000 |
| 失败处理 | 标记 ERROR + 保留原文 |

### 4.5 Step 4 — 图片修补（原文擦除）

- **输入：** 原图 BufferedImage + List\<TextRegion\>
- **输出：** 修补后的 BufferedImage

**策略 A — 白色覆盖（默认 MVP）：**
- 创建原图副本
- 遍历每个 TextRegion，用 Graphics2D 填充白色圆角矩形
- 圆角半径 8px，贴合漫画气泡框形状

**策略 B — 取色填充（进阶）：**
- 对每个覆盖区域，采样边缘环（上下左右各 2px）的所有像素
- 计算 RGB 三通道均值作为填充颜色
- 适用于彩色背景漫画（非白底气泡框）

**参数规格：**

| 参数 | 白色覆盖 | 取色填充 |
|------|---------|---------|
| 填充颜色 | #FFFFFF | RGB 三通道均值 |
| 圆角半径 | 8px | 8px |
| 采样宽度 | — | 2px |

### 4.6 Step 5 — 文字回填渲染

- **输入：** 修补后 BufferedImage + List\<TextRegion\>
- **输出：** 最终翻译后 BufferedImage

**横排文字渲染算法：**
```
calculateFontSize(areaW, areaH, text):
    baseSize = areaH * 0.65f          // 基础字号 = 框高 × 65%
    textW = fm.stringWidth(text)
    if textW > areaW * 0.9f:          // 文字溢出则缩小
        baseSize *= (areaW * 0.9f) / textW
    return max(baseSize, 10f)         // 最低字号 10px
```

**竖排文字渲染算法：**
```
drawVertical(g, text, region):
    fontSize = min(expandW * 0.7f, expandH / text.length * 0.85f)
    colW = fontSize * 1.3f            // 列宽
    charH = fontSize * 1.2f           // 字符高度
    // 从右到左逐列，每列从上到下逐字符
```

**参数规格：**

| 参数 | 值 |
|------|-----|
| 横排字号基数 | 框高 × 0.65 |
| 竖排字号基数 | min(框宽×0.7, 框高/字数×0.85) |
| 最低字号 | 10px |
| 文字对齐 | CENTER（居中对齐） |
| 文字颜色 | #000000（纯黑） |
| 文字描边 | 无 |

---

## 5. 百度 API 集成方案

### 5.1 百度 OCR API

| 参数 | 值 |
|------|-----|
| 标准版 URL | `https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic` |
| 高精度版 URL | `https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic` |
| 请求方式 | POST |
| 鉴权 | access_token（通过 API Key + Secret Key 获取） |
| 标准版免费额度 | 每月 1000 次（50 次/天） |
| 高精度版免费额度 | 每天 50 次 |
| 输入 | image（Base64 编码） |

**OCR 返回 JSON 关键字段：**
```json
{
  "words_result_num": 3,
  "words_result": [
    {
      "words": "こんにちは",
      "location": { "left": 120, "top": 85, "width": 160, "height": 28 },
      "direction": 0,        // 0=横排, 1=竖排
      "probability": { "average": 0.92 }
    }
  ],
  "language": "JAP"
}
```

### 5.2 百度翻译 API

| 参数 | 值 |
|------|-----|
| 请求 URL | `https://fanyi-api.baidu.com/api/trans/vip/translate` |
| 请求方式 | GET / POST |
| 鉴权 | appid + salt + sign（MD5 签名） |
| 免费额度 | 标准版每月 5 万字符 |
| 批量翻译 | 多条文本用 `\n` 拼接 |

**签名算法：**
```java
String salt = String.valueOf(System.currentTimeMillis());
String sign = MD5(appid + queryText + salt + secretKey);
```

### 5.3 鉴权管理 — BaiduAuthManager

- **OCR API 鉴权：** 用 API Key + Secret Key 请求 access_token → 缓存到本地（AES-128 加密）→ 有效期 30 天 → 过期自动刷新
- **翻译 API 鉴权：** 每次请求即时生成 appid + salt + sign，无需缓存 token
- **启动流程：** 检查缓存 → 未过期直接用 → 过期自动获取 → 获取失败弹窗提示
- **首次引导：** 首次启动弹出引导对话框，逐项填写 API Key 并验证连通性

### 5.4 API 错误处理与重试策略

| 错误码 | 含义 | 处理策略 |
|--------|------|---------|
| 18 / 19 | QPS 超限 | 等待 2s 后重试，最多 3 次 |
| 17 | 日调用量超限 | 弹窗提示用户，当日停止翻译 |
| 100 / 101 | 参数错误 | 检查参数后重试 1 次，仍失败则标记 ERROR |
| 110 / 111 | access_token 无效 | 自动刷新 token 后重试 |
| Network timeout | 网络超时 | 等待 5s 后重试，最多 3 次 |

| 重试参数 | 值 |
|---------|-----|
| 最大重试次数 | 3 |
| 重试间隔 | 2s / 4s / 8s（指数退避） |
| 限流排队 | 并发 ≤ 3 |
| 日用量计数 | 本地累计，每天 0 点重置 |

---

## 6. UI 界面详细设计

### 6.1 设计参考理念

| 参考对象 | 借鉴点 |
|---------|--------|
| **DeepL** | 简洁顶部工具栏 + 居中大面板 + 左右对比 |
| **Google Translate** | 清晰输入输出分区 → 双 Tab + 拆分视图 |
| **Photoshop / Figma** | 左侧文件面板 + 中间画布 + 底部属性面板 |
| **Comic Translate** | 原文/译文对照文本面板 |

### 6.2 全局 UI 参数

#### 窗口与布局

| 参数 | 值 |
|------|-----|
| 窗口标题 | MangaTranslator — 漫画翻译器 |
| 窗口默认尺寸 | 1280 × 720 px |
| 窗口最小尺寸 | 960 × 540 px |
| 窗口背景色 | #1E1E2E（深色主题） |
| 组件间距 (gap) | 4 px |
| 圆角半径 | 8 px（统一） |
| 字体族 | "Microsoft YaHei UI", "Segoe UI", sans-serif |
| 正文字号 | 13 px |
| 标题字号 | 14 px / font-weight: 500 |
| 辅助字号 | 11 px |

#### 颜色系统

| 用途 | 色值 | 说明 |
|------|------|------|
| 主背景色 | #1E1E2E | 窗口背景 |
| 面板/卡片背景 | #252540 | 所有面板容器 |
| 输入/内容区背景 | #2A2A4A | 输入框/画布内容区 |
| 悬停/选中背景 | #3A3A5A | hover/active 状态 |
| 主文字色 | #E8E8E8 | 正文/标题 |
| 次要文字色 | #A0A0B0 | 辅助说明/状态 |
| 禁用文字色 | #606070 | 不可用状态 |
| 强调色（主操作） | #E94560 | 翻译按钮/主操作 |
| 强调色悬停 | #FF6B81 | hover 变亮 |
| 信息色 | #60A5FA | 链接/选中标签/信息 |
| 成功色 | #4ADE80 | 完成标记/成功提示 |
| 警告色 | #FBBF24 | 译文/进行中 |
| 错误色 | #F87171 | 错误日志/失败 |
| 边框色 | #404058 | 所有组件边框 |
| 分隔线色 | #353548 | 面板间分隔 |

#### 按钮规格

| 参数 | 普通按钮 | 主操作按钮 |
|------|---------|-----------|
| 内边距 | 5px 14px | 7px 20px |
| 背景 | #252540 | #E94560 |
| 文字色 | #A0A0B0 | #FFFFFF |
| 边框 | 1px solid #404058 | none |
| hover | 文字→#E8E8E8, 边框→#60A5FA | background→#FF6B81 |
| 圆角 | 6px | 6px |
| 图标尺寸 | 14 × 14 px | 14 × 14 px |
| 过渡动画 | background-color 150ms ease | background-color 150ms ease |

### 6.3 主窗口布局

```
┌──────────────────────────────────────────────────────────────────┐
│ 导航栏 (NavBar) — 高度 52px                                     │
│ [MangaTranslator] [翻译] [打开] [批量导入] [导出] │ [拆分视图] [设置] │ ● API 已连接   │
├──────┬───────────────────────────────────────────────────────────┤
│侧栏   │ 中间区域                                                 │
│260px │ ┌──────────────────────────────────────────────────────┐  │
│      │ │ 画布区域 (Canvas Area)                              │  │
│文件  │ │ [原图] [翻译结果]                          100%     │  │
│列表  │ │ ┌────────────────────────────────────────────────┐  │  │
│      │ │ │   🖼 漫画图片展示区                            │  │  │
│✓完成  │ │ │   Ctrl+O 打开图片 · 或拖拽文件到此区域        │  │  │
│⏳翻译 │ │ └────────────────────────────────────────────────┘  │  │
│待翻译 │ └──────────────────────────────────────────────────────┘  │
│待翻译 │ ┌──────────────────────────────────────────────────────┐  │
│      │ │ 文本对照面板 (Text Panel) — 高度 160px              │  │
│统计   │ │ 原文（日语）         │ 译文（中文）                 │  │
│      │ │ #1 こんにちは        │ #1 你好                      │  │
│      │ │ #2 冒険者さん！      │ #2 冒险者！                  │  │
│      │ └──────────────────────────────────────────────────────┘  │
├──────┴───────────────────────────────────────────────────────────┤
│ 底部状态栏 (BottomBar) — 高度 44px                               │
│ [████████░░░░░░░] 65% — OCR 完成                    [日志 ▾]    │
└──────────────────────────────────────────────────────────────────┘
```

### 6.4 各区域详细参数

#### 导航栏 (NavBar)

| 参数 | 值 |
|------|-----|
| 高度 | 52px |
| 内边距 | 8px 16px |
| 背景色 | #2A2A4A |
| 圆角 | 8px |
| 元素排列 | flex, align-items:center, gap:8px |

**导航栏按钮：**

| 按钮 | 类型 | 快捷键 | 行为 |
|------|------|--------|------|
| 翻译 | primary | Ctrl+T | 开始翻译/取消 |
| 打开 | normal | Ctrl+O | 文件选择器 |
| 批量导入 | normal | Ctrl+B | 选择文件夹 |
| 导出 | normal | Ctrl+S | 保存结果 |
| 拆分视图 | normal | Ctrl+D | 切换拆分模式 |
| 设置 | normal | Ctrl+, | 打开设置对话框 |

#### 侧栏 (Sidebar)

| 参数 | 值 |
|------|-----|
| 宽度 | 260px（固定） |
| 面板占比 | 文件列表 flex:2, 统计 flex:1 |

**文件列表参数：**

| 参数 | 值 |
|------|-----|
| 缩略图尺寸 | 32 × 40px |
| 文件项高度 | 约 48px |
| 已完成状态 | "✓ 已完成" · #4ADE80 |
| 翻译中状态 | "⏳ 翻译中" · #60A5FA |
| 待翻译状态 | "待翻译" · #FBBF24 |
| 失败状态 | "✗ 失败" · #F87171 |
| 右键菜单 | 删除 · 重新翻译 · 导出此图 · 查看翻译详情 |

#### 画布区域 (Canvas Area)

| 参数 | 值 |
|------|-----|
| 内容区背景 | #1E1E2E |
| 缩放范围 | 10% - 500% |
| 缩放步长 | 10% |
| 文字区域高亮色 | #60A5FA @ opacity 0.3 |
| 文字区域选中色 | #E94560 @ opacity 0.4 |

#### 文本对照面板 (Text Panel)

| 参数 | 值 |
|------|-----|
| 高度 | 160px（固定） |
| 列数 | 2 列（原文 / 译文） |
| 原文列字号 | 12px · color: #A0A0B0 |
| 译文列字号 | 12px · color: #FBBF24 |
| 译文列交互 | 双击弹出修正对话框 |

#### 底部状态栏 (BottomBar)

| 参数 | 值 |
|------|-----|
| 高度 | 44px |
| 进度条高度 | 6px |
| 进度条填充 | 渐变 #533483 → #E94560 |
| 进度条动画 | width transition 300ms ease |

### 6.5 对话框设计

#### 翻译修正对话框 (CorrectionDialog)

- **类型：** modal，阻塞主窗口
- **宽度：** 440px
- **布局：** 原文展示 → 机器翻译展示 → 修正编辑框 → 操作按钮
- **按钮：** 取消 / 确认修正(primary) / 重新翻译

#### 设置对话框 (SettingsDialog)

- **类型：** modal
- **宽度：** 560px
- **分区：**
  1. 百度 API 配置（OCR API Key, OCR Secret Key, 翻译 App ID, 翻译密钥）
  2. 翻译选项（OCR 版本, 源语言, 目标语言）
  3. 渲染与导出（气泡框扩展量, 修补策略, 回填字体, 导出格式）

#### 首次引导对话框 (OnboardingDialog)

- **触发：** config.json 不存在或 API Key 为空
- **宽度：** 480px
- **步骤：** 欢迎介绍 → 填写 API Key → 验证连通性 → 进入主界面

### 6.6 交互状态流转

```
空闲 → 已导入 → 翻译中 → 已完成 → 修正中 → 已导出
```

| 状态 | 画布区 | 文本面板 | 导航栏按钮 |
|------|--------|---------|-----------|
| 空闲 | 占位提示 | 空 | 翻译=禁用, 导出=禁用 |
| 已导入 | 显示原图 | 空 | 翻译=激活, 导出=禁用 |
| 翻译中 | 原图+蓝色高亮 | 逐条填充 | 翻译=变"取消" |
| 已完成 | 翻译结果图 | 对照可双击 | 导出=激活, 拆分=激活 |
| 修正中 | 修正区域高亮 | 编辑态 | 其他禁用 |
| 已导出 | 不变 | 不变 | Toast 提示 |

### 6.7 Toast 提示

| 参数 | 值 |
|------|-----|
| 类型 | overlay · 非阻塞 |
| 位置 | 窗口右上角 |
| 宽度 | max-width: 320px |
| 显示时长 | 3s 自动消失 |
| 入场动画 | opacity 0→1 200ms ease + translateY -10px→0 |

---

## 7. 数据模型与接口定义

### 7.1 核心数据模型

```java
public class TextRegion {
    private int id;                          // 区域编号，从 1 开始
    private int left, top, width, height;     // OCR 检测的原始坐标
    private int expandLeft, expandTop;        // 扩展后的覆盖区域坐标
    private int expandWidth, expandHeight;    // 扩展后的覆盖区域尺寸
    private String originalText;              // OCR 识别原文
    private String translatedText;            // 百度翻译译文
    private String correctedText;             // 用户修正后的译文（优先使用）
    private int direction;                    // 0=横排, 1=竖排
    private String language;                  // 检测到的语言代码 (JAP/ENG/KOR)
    private double probability;               // OCR 置信度
    private RegionStatus status;              // PENDING/TRANSLATED/CORRECTED/ERROR
}

public class MangaPage {
    private String fileName;                  // 文件名
    private String filePath;                  // 文件绝对路径
    private BufferedImage originalImage;      // 原图
    private BufferedImage translatedImage;    // 翻译后的图
    private List<TextRegion> textRegions;     // 文字区域列表
    private PageStatus status;                // IDLE/LOADED/TRANSLATING/COMPLETED/CORRECTING/EXPORTED
    private long timestamp;                   // 导入时间戳
}

public class TranslationConfig {
    private String ocrApiKey;                 // 百度 OCR API Key
    private String ocrSecretKey;              // 百度 OCR Secret Key
    private String translateAppId;            // 百度翻译 App ID
    private String translateSecretKey;        // 百度翻译密钥
    private String sourceLanguage;            // 源语言 (jp/en/kor)
    private String targetLanguage;            // 目标语言 (zh)
    private int expandMargin;                 // 气泡框扩展量 (默认12)
    private InpaintStrategy inpaintStrategy;  // WHITE_FILL / AVG_COLOR_FILL
    private OcrVersion ocrVersion;            // GENERAL / ACCURATE
    private String fontName;                  // 回填字体名
    private String exportFormat;              // png / jpg
}
```

### 7.2 Service 接口定义

```java
public interface OcrService {
    List<TextRegion> detectTextRegions(BufferedImage image, TranslationConfig config);
}

public interface TranslateService {
    void translateTexts(List<TextRegion> regions, TranslationConfig config);
}

public interface InpaintService {
    BufferedImage inpaint(BufferedImage image, List<TextRegion> regions, TranslationConfig config);
}

public interface RenderService {
    BufferedImage renderTranslatedText(BufferedImage inpaintedImage,
        List<TextRegion> regions, TranslationConfig config);
}
```

---

## 8. 项目结构与代码组织

```
manga-translator/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/manga/translator/
│   │   │   ├── App.java                         // 入口
│   │   │   ├── config/
│   │   │   │   ├── TranslationConfig.java
│   │   │   │   └── ConfigManager.java           // JSON 读写
│   │   │   ├── model/
│   │   │   │   ├── TextRegion.java
│   │   │   │   ├── MangaPage.java
│   │   │   │   ├── PageStatus.java
│   │   │   │   ├── RegionStatus.java
│   │   │   │   ├── InpaintStrategy.java
│   │   │   │   └── OcrVersion.java
│   │   │   ├── pipeline/
│   │   │   │   ├── TranslationPipeline.java     // 管线编排
│   │   │   │   ├── PipelineContext.java          // 上下文数据
│   │   │   │   └── PipelineEventBus.java         // 事件总线
│   │   │   ├── service/
│   │   │   │   ├── OcrService.java
│   │   │   │   ├── TranslateService.java
│   │   │   │   ├── InpaintService.java
│   │   │   │   ├── RenderService.java
│   │   │   │   └── impl/
│   │   │   │       ├── BaiduOcrServiceImpl.java
│   │   │   │       ├── BaiduTranslateServiceImpl.java
│   │   │   │       ├── WhiteInpaintServiceImpl.java
│   │   │   │       ├── AvgColorInpaintServiceImpl.java
│   │   │   │       └── Graphics2DRenderServiceImpl.java
│   │   │   ├── client/
│   │   │   │   ├── BaiduOcrClient.java
│   │   │   │   ├── BaiduTranslateClient.java
│   │   │   │   ├── BaiduAuthManager.java
│   │   │   │   └── HttpUtil.java
│   │   │   ├── ui/
│   │   │   │   ├── MainWindow.java              // 主窗口
│   │   │   │   ├── CanvasPanel.java             // 画布区
│   │   │   │   ├── TextPanel.java               // 文本对照
│   │   │   │   ├── FileListPanel.java           // 文件侧栏
│   │   │   │   ├── NavBar.java                  // 导航栏
│   │   │   │   ├── BottomBar.java               // 底部状态
│   │   │   │   ├── LogPanel.java                // 日志面板
│   │   │   │   ├── CorrectionDialog.java
│   │   │   │   ├── SettingsDialog.java
│   │   │   │   ├── OnboardingDialog.java
│   │   │   │   └── Toast.java                   // 提示气泡
│   │   │   └── util/
│   │   │       ├── ImageUtil.java
│   │   │       ├── FontLoader.java
│   │   │       ├── Md5Util.java
│   │   │       ├── Base64Util.java
│   │   │       └── AesEncryptUtil.java          // API Key 加密
│   │   └── resources/
│   │       ├── fonts/ZhanKuKuaiLeTi.ttf
│   │       ├── icons/                           // SVG 图标
│   │       ├── config.json
│   │       └── app.css                          // JavaFX CSS
│   └── test/
```

### Maven 依赖

```xml
<dependencies>
    <!-- JavaFX -->
    <dependency>org.openjfx:javafx-controls:17.0.8</dependency>
    <dependency>org.openjfx:javafx-fxml:17.0.8</dependency>
    <dependency>org.openjfx:javafx-swing:17.0.8</dependency>
    <!-- HTTP -->
    <dependency>org.apache.httpcomponents.client5:httpclient5:5.3</dependency>
    <!-- JSON -->
    <dependency>com.google.code.gson:gson:2.10.1</dependency>
    <!-- Log -->
    <dependency>org.slf4j:slf4j-simple:2.0.9</dependency>
    <!-- Crypto -->
    <dependency>org.bouncycastle:bcprov-jdk18on:1.78</dependency>
</dependencies>
```

---

## 9. 开发计划与里程碑

### 总体时间线（4 周）

| 阶段 | 时间 | 内容 | 交付物 |
|------|------|------|--------|
| **P0 搭建** | 第1周 7/11-7/17 | 项目骨架、API 鉴权、基础 UI、首次引导 | 可启动空壳 + API 连通验证 |
| **P0 核心** | 第2周 7/18-7/24 | OCR→翻译→修补→回填完整实现 | 单张图片翻译 demo |
| **P1 完善** | 第3周 7/25-7/31 | 翻译修正、批量翻译、进度日志、文件列表、拆分视图 | 完整可用应用 |
| **P2 打磨** | 第4周 8/1-8/7 | 竖排文字、取色修补、Toast、CSS 美化、测试 | 最终交付版本 |

### 15 个里程碑

| 日期 | 里程碑 | 内容 | 交付物 |
|------|--------|------|--------|
| 7/11 | **M1** | 项目初始化 | Maven 项目 + JavaFX 主窗口骨架 + pom.xml |
| 7/13 | **M2** | API 鉴权连通 | BaiduAuthManager + HTTP 框架 |
| 7/15 | **M3** | UI 三区布局 | 导航栏 + 侧栏 + 画布区 + 文本面板 + 底部栏 |
| 7/17 | **M4** | 设置 + 首次引导 | SettingsDialog + OnboardingDialog + ConfigManager |
| 7/18 | **M5** | OCR 步骤 | BaiduOcrServiceImpl 完整实现 |
| 7/19 | **M6** | 翻译步骤 | BaiduTranslateServiceImpl + 签名 + 批量分批 |
| 7/21 | **M7** | 修补 + 回填 | WhiteInpaintServiceImpl + Graphics2DRenderServiceImpl |
| 7/24 | **M8** | 管线集成 | TranslationPipeline 串联 + 进度事件 |
| 7/25 | **M9** | 翻译修正 | CorrectionDialog + 修正后重新渲染 |
| 7/27 | **M10** | 批量翻译 | 多图片导入 + 批量管线 + 并发控制 |
| 7/29 | **M11** | 导出 + Toast | 单张/批量导出 + Toast 提示 |
| 7/31 | **M12** | 拆分视图 + 日志 | SplitView 模式 + LogPanel 展开折叠 |
| 8/1 | **M13** | 竖排文字 | 竖排渲染逻辑 + direction 检测 |
| 8/3 | **M14** | 取色修补 | AvgColorInpaintServiceImpl |
| 8/5-8/7 | **M15** | 测试 + 美化 + 交付 | CSS 打磨 + 单元测试 + 文档整理 |

---

## 10. 测试方案

### 10.1 单元测试

| 测试类 | 覆盖内容 | 方法数 |
|--------|---------|--------|
| BaiduOcrClientTest | API 调用、JSON 解析、参数构建、异常处理 | 6 |
| BaiduTranslateClientTest | 签名生成、批量翻译、分隔符处理、异常 | 7 |
| BaiduAuthManagerTest | token 获取、缓存、过期刷新、加密存储 | 5 |
| InpaintServiceTest | 白色覆盖、取色填充、圆角绘制、边界情况 | 6 |
| RenderServiceTest | 字号计算、横排渲染、竖排渲染、溢出处理 | 7 |
| PipelineTest | 管线串联、中间数据传递、步骤失败中断、重试 | 5 |
| ConfigManagerTest | JSON 读写、加密存储、首次引导触发 | 4 |

### 10.2 集成测试

- 端到端：从图片导入到导出的完整流程（3 张真实漫画）
- 修正闭环：修改译文 → 重新渲染 → 结果正确
- 批量翻译：10 张图片批量处理，验证进度和并发
- 拆分视图：切换拆分模式后缩放联动正确
- API 限流：模拟限流错误，验证重试和排队
- 首次引导：首次启动弹出引导 → 填写 Key → 验证 → 进入主界面

### 10.3 测试素材

| 编号 | 类型 | 用途 |
|------|------|------|
| T01 | 白底横排文字漫画 | 基础功能测试 |
| T02 | 白底竖排文字漫画 | 竖排渲染测试 |
| T03 | 彩色背景漫画 | 取色填充测试 |
| T04 | 多文字区域 (5+) | 批量翻译对齐 |
| T05 | 无文字漫画 | OCR 空结果边界 |
| T06 | 10 张漫画合集 | 批量翻译性能 |

---

## 11. 风险评估与应对

| 风险等级 | 风险描述 | 应对策略 |
|---------|---------|---------|
| 🔴 **高** | 百度 OCR 对漫画文字识别率不理想 | 先用通用版测试，效果差则切换高精度版/手写版；修正功能作为兜底；OCR 参数可调 |
| 🔴 **高** | 百度 API 免费额度不够（高精度 OCR 50 次/天） | 开发阶段用通用版 (1000 次/月)；答辩用高精度版；UI 显示用量计数；超出弹窗提示 |
| 🟡 **中** | 白色覆盖修补在非白底漫画效果差 | 实现取色填充备选策略；设置中可切换 |
| 🟡 **中** | 竖排文字渲染风格与原漫画不一致 | Phase 4 专门迭代竖排逻辑；提供字体选择；字号自适应算法针对竖排优化 |
| 🟢 **低** | JavaFX 在部分系统启动问题 | Maven JavaFX 插件自动配 JVM 参数；pom.xml 指定模块路径 |
| 🟢 **低** | API Key 安全存储 | AES-128 加密存储 config.json；首次引导填写；不硬编码 |

---

## 12. 交付清单

| 类别 | 交付物 | 格式 |
|------|--------|------|
| 源码 | 完整项目源码 | Git 仓库 + Maven |
| 可执行 | manga-translator-1.0.jar | 双击可运行 JAR |
| 文档 | 项目计划书 | Markdown / HTML |
| 文档 | API 集成说明 | Markdown |
| 文档 | 用户操作手册 | Markdown |
| 测试 | 单元测试代码 | JUnit 5 |
| 测试 | 测试漫画素材 | 6 张 JPG/PNG |
| 演示 | 答辩演示 PPT | PPTX |
| 演示 | 现场演示录屏 | MP4 |

---

> 漫画翻译器项目计划书 · v2.0 · 南昌航空大学软件工程 2025 级暑期实训

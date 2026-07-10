# 漫画翻译器 — 项目进度日志

> 项目：漫画翻译器 (MangaTranslator)  
> 版本：v1.0 · 最后更新：2026-07-10 19:53  
> 团队：南昌航空大学 · 软件工程 2025 级暑期实训

---

## 总体进度概览

| 阶段 | 计划时间 | 状态 | 完成度 |
|------|---------|------|--------|
| **Phase 0 — 项目初始化** | 7/10 | ✅ 已完成 | 100% |
| **Phase 1 — MVP 核心管线** | 第1-2周 (7/11-7/24) | 🟡 进行中 | 20% |
| **Phase 2 — 品质提升** | 第3周 (7/25-7/31) | ⚪ 未开始 | 0% |
| **Phase 3 — 高级特性** | 第4周 (8/1-8/7) | ⚪ 未开始 | 0% |

### 功能完成状态

| 编号 | 功能模块 | 优先级 | 状态 | 完成日期 | 备注 |
|------|---------|--------|------|---------|------|
| F01 | 图片导入 | P0 | ⚪ 待开发 | — | — |
| F02 | OCR 识别 | P0 | ⚪ 待开发 | — | — |
| F03 | 文字翻译 | P0 | ⚪ 待开发 | — | — |
| F04 | 原文擦除 | P0 | ⚪ 待开发 | — | — |
| F05 | 译文回填 | P0 | ⚪ 待开发 | — | — |
| F06 | 导出结果 | P0 | ⚪ 待开发 | — | — |
| F07 | 双栏对比 | P0 | ⚪ 待开发 | — | — |
| F08 | 文字区域高亮 | P1 | ⚪ 待开发 | — | — |
| F09 | 翻译修正 | P1 | ⚪ 待开发 | — | — |
| F10 | 批量翻译 | P1 | ⚪ 待开发 | — | — |
| F11 | 批量进度 | P1 | ⚪ 待开发 | — | — |
| F12 | 文件列表 | P1 | ⚪ 待开发 | — | — |
| F13 | 缩放与导航 | P1 | ⚪ 待开发 | — | — |
| F14 | 拆分视图 | P2 | ⚪ 待开发 | — | — |
| F15 | 语言方向设置 | P2 | ⚪ 待开发 | — | — |
| F16 | 字体与字号 | P2 | ⚪ 待开发 | — | — |
| F17 | 竖排文字 | P2 | ⚪ 待开发 | — | — |
| F18 | 首次引导 | P2 | ⚪ 待开发 | — | — |

### 里程碑状态

| 编号 | 里程碑 | 计划日期 | 状态 | 实际日期 |
|------|--------|---------|------|---------|
| M1 | 项目初始化 | 7/11 | ✅ 已完成 | 7/10 |
| M2 | API 鉴权连通 | 7/13 | ⚪ 待开始 | — |
| M3 | UI 三区布局 | 7/15 | ⚪ 待开始 | — |
| M4 | 设置对话框 + 首次引导 | 7/17 | ⚪ 待开始 | — |
| M5 | OCR 步骤 | 7/18 | ⚪ 待开始 | — |
| M6 | 翻译步骤 | 7/19 | ⚪ 待开始 | — |
| M7 | 修补 + 回填 | 7/21 | ⚪ 待开始 | — |
| M8 | 管线集成 | 7/24 | ⚪ 待开始 | — |
| M9 | 翻译修正 | 7/25 | ⚪ 待开始 | — |
| M10 | 批量翻译 | 7/27 | ⚪ 待开始 | — |
| M11 | 导出 + Toast | 7/29 | ⚪ 待开始 | — |
| M12 | 拆分视图 + 日志 | 7/31 | ⚪ 待开始 | — |
| M13 | 竖排文字 | 8/1 | ⚪ 待开始 | — |
| M14 | 取色修补 | 8/3 | ⚪ 待开始 | — |
| M15 | 测试 + 美化 + 交付 | 8/5-8/7 | ⚪ 待开始 | — |

---

## 开发记录

### [2026-07-10] Phase 1 Step 1 — 项目骨架搭建

**完成内容：**
- 创建 `pom.xml`（JDK 17, JavaFX 17.0.8, 所有依赖锁定）
- 创建完整包结构（8 个子包）
- 创建 `MangaTranslatorApp.java` 主入口（JavaFX 窗口 1280×720）
- 创建 `logback.xml` 日志配置
- 创建 model 层：7 个数据类（TextRegion, MangaPage, TranslationConfig, 4 个枚举）
- 创建异常体系：1 个基类 + 5 个子类（OcrException, TranslationException, AuthException, RateLimitException, ConfigException）
- 创建 config 层：`ConfigManager.java`（JSON 读写，存储到 ~/.manga-translator/）
- 创建 util 层：4 个工具类（ImageUtil, Md5Util, Base64Util, AesEncryptUtil）
- 配置 JDK 17 + Maven 3.9.6 环境
- `mvn compile` 编译成功（19 个源文件，0 错误）

**修改文件：**
| 文件 | 说明 |
|------|------|
| `pom.xml` | 新增 — Maven 构建配置 |
| `src/main/java/com/manga/translator/MangaTranslatorApp.java` | 新增 — 主入口 |
| `src/main/resources/logback.xml` | 新增 — 日志配置 |
| `src/main/java/com/manga/translator/model/*.java` | 新增 — 7 个 model 类 |
| `src/main/java/com/manga/translator/*Exception.java` | 新增 — 6 个异常类 |
| `src/main/java/com/manga/translator/config/ConfigManager.java` | 新增 — 配置管理 |
| `src/main/java/com/manga/translator/util/*.java` | 新增 — 4 个工具类 |

**遇到的问题：**
- 环境问题：系统中只有 JDK 8，需要 JDK 17
- Maven 未在 PATH 中配置

**解决方式：**
- 发现 D 盘已有 JDK 17.0.19（Eclipse Temurin）和 Maven 3.9.6
- 通过 export JAVA_HOME 和 PATH 临时配置，编译成功

**待办事项：**
- [x] 下一步：实现 Client 层（BaiduAuthManager, BaiduOcrClient, BaiduTranslateClient）
- [ ] 将 JDK 17 + Maven 环境变量持久化配置

**Git 提交：** `feat: 创建 Maven 项目骨架和基础代码结构`

---

### [2026-07-10] Phase 1 Step 2 — 实现 Client 层

**完成内容：**
- 实现 `HttpUtil.java` — HTTP 请求工具类（Apache HttpClient 5.3 封装，10s/30s 超时）
- 实现 `BaiduAuthManager.java` — 百度鉴权管理器（access_token 获取/缓存/自动刷新/连通验证）
- 实现 `TokenBucketRateLimiter.java` — 令牌桶限速器（OCR 1.5QPS/翻译 8QPS，Semaphore 实现）
- 实现 `CircuitBreaker.java` — 熔断器（3状态：CLOSED/OPEN/HALF_OPEN，5次失败→60s熔断→3次锁定）
- 实现 `BaiduOcrClient.java` — OCR API 客户端（通用版/高精度版，置信度门控，JSON 解析）
- 实现 `BaiduTranslateClient.java` — 翻译 API 客户端（MD5 签名，单条/批量翻译，6000字符限制）

**修改文件：**
| 文件 | 说明 |
|------|------|
| `src/main/java/com/manga/translator/client/HttpUtil.java` | 新增 — HTTP 工具类 |
| `src/main/java/com/manga/translator/client/BaiduAuthManager.java` | 新增 — 鉴权管理 |
| `src/main/java/com/manga/translator/client/TokenBucketRateLimiter.java` | 新增 — 限速器 |
| `src/main/java/com/manga/translator/client/CircuitBreaker.java` | 新增 — 熔断器 |
| `src/main/java/com/manga/translator/client/BaiduOcrClient.java` | 新增 — OCR 客户端 |
| `src/main/java/com/manga/translator/client/BaiduTranslateClient.java` | 新增 — 翻译客户端 |

**遇到的问题：**
- `EntityUtils.toString` 在 HttpClient 5 中会抛出 `ParseException`（不在 `IOException` 继承链中），需要显式捕获

**解决方式：**
- 在 post 和 postJson 方法中添加 `catch (ParseException e)` → 包装为 IOException 抛出

**待办事项：**
- [ ] 下一步：实现 Service 层（OcrService, TranslateService, InpaintService, RenderService）

**Git 提交：** `feat: 实现 Client 层（HTTP工具/鉴权/限速/熔断/OCR客户端/翻译客户端）`

---

### [2026-07-10] Phase 1 Step 3 — 实现 Service 层

**完成内容：**
- 实现 `OcrService` 接口 + `BaiduOcrServiceImpl`（限速1.5QPS、熔断保护、Base64编码、置信度门控）
- 实现 `TranslateService` 接口 + `BaiduTranslateServiceImpl`（限速8QPS、逐条/分批策略、部分失败处理）
- 实现 `InpaintService` 接口 + `WhiteInpaintServiceImpl`（白色圆角矩形覆盖，半径8px）
- 实现 `InpaintService` 接口 + `AvgColorInpaintServiceImpl`（取色填充，边缘2px采样 RGB 均值）
- 实现 `RenderService` 接口 + `Graphics2DRenderServiceImpl`（横排65%/竖排自适应、10px下限、居中对齐）

**修改文件：**
| 文件 | 说明 |
|------|------|
| `src/main/java/com/manga/translator/service/OcrService.java` | 新增 — OCR接口 |
| `src/main/java/com/manga/translator/service/TranslateService.java` | 新增 — 翻译接口 |
| `src/main/java/com/manga/translator/service/InpaintService.java` | 新增 — 修补接口 |
| `src/main/java/com/manga/translator/service/RenderService.java` | 新增 — 渲染接口 |
| `src/main/java/com/manga/translator/service/impl/BaiduOcrServiceImpl.java` | 新增 — OCR实现 |
| `src/main/java/com/manga/translator/service/impl/BaiduTranslateServiceImpl.java` | 新增 — 翻译实现 |
| `src/main/java/com/manga/translator/service/impl/WhiteInpaintServiceImpl.java` | 新增 — 白色修补 |
| `src/main/java/com/manga/translator/service/impl/AvgColorInpaintServiceImpl.java` | 新增 — 取色修补 |
| `src/main/java/com/manga/translator/service/impl/Graphics2DRenderServiceImpl.java` | 新增 — 渲染实现 |

**遇到的问题：**
- `Graphics2DRenderServiceImpl` 缺少 `import java.util.List` 导致编译失败

**解决方式：**
- 添加缺失的 import 语句

**待办事项：**
- [ ] 下一步：实现 Pipeline 层（PipelineContext、TranslationStep、TranslationPipeline）

**Git 提交：** `feat: 实现 Service 层（OCR/翻译/修补/渲染 4 接口+实现）`

---

### [2026-07-10] Phase 1 Step 4 — 实现 Pipeline 层

**完成内容：**
- 实现 `PipelineContext.java` — 管线上下文（数据传递 + 进度管理 + 取消标记）
- 实现 `PipelineEvent.java` / `PipelineListener.java` / `PipelineEventBus.java` — 事件体系（进度/错误/完成）
- 实现 `OcrStep.java` — OCR 步骤（权重 20%）
- 实现 `CleanStep.java` — 文本清洗步骤（权重 10%，去噪声 + 相邻合并）
- 实现 `TranslateStep.java` — 翻译步骤（权重 30%）
- 实现 `InpaintStep.java` — 修补步骤（权重 20%）
- 实现 `RenderStep.java` — 回填步骤（权重 20%）
- 实现 `TranslationPipeline.java` — 管线编排器（5 步顺序执行 + 异常中止 + 取消支持 + 事件通知）

**修改文件：**
| 文件 | 说明 |
|------|------|
| `src/main/java/com/manga/translator/pipeline/PipelineContext.java` | 新增 |
| `src/main/java/com/manga/translator/pipeline/TranslationStep.java` | 新增 — 步骤接口 |
| `src/main/java/com/manga/translator/pipeline/PipelineEvent.java` | 新增 — 事件类 |
| `src/main/java/com/manga/translator/pipeline/PipelineListener.java` | 新增 — 监听器接口 |
| `src/main/java/com/manga/translator/pipeline/PipelineEventBus.java` | 新增 — 事件总线 |
| `src/main/java/com/manga/translator/pipeline/OcrStep.java` | 新增 |
| `src/main/java/com/manga/translator/pipeline/CleanStep.java` | 新增 |
| `src/main/java/com/manga/translator/pipeline/TranslateStep.java` | 新增 |
| `src/main/java/com/manga/translator/pipeline/InpaintStep.java` | 新增 |
| `src/main/java/com/manga/translator/pipeline/RenderStep.java` | 新增 |
| `src/main/java/com/manga/translator/pipeline/TranslationPipeline.java` | 新增 — 管线编排器 |

**遇到的问题：**
- `PipelineEvent` 是 public class 需要独立文件，不能和 PipelineEventBus 放在同一个 .java 中
- `PipelineListener` 接口同理

**解决方式：**
- 拆分为 3 个独立文件：PipelineEvent.java、PipelineListener.java、PipelineEventBus.java

**Git 提交：** `feat: 实现 Pipeline 层（管线上下文/5 步步骤/编排器/事件总线）`

---

### [2026-07-10] Phase 0 — 项目初始化

**完成内容：**
- 初始化 Git 仓库
- 创建 `.gitignore`（Java/Maven/IDE/OS 过滤 + config.json 排除）
- 创建 `project-plan.md` — 项目计划书（基于 HTML 设计文档整理，12章完整内容）
- 创建 `ai-behavior-spec.md` — AI 操作行为规范（基于 HTML 规范文档整理，12章）
- 创建 `claude-code-spec.md` — Claude Code 操作规范（包含 25 条红线 + 18 项验收清单 + 项目进度记录规范）
- 创建 `CLAUDE.md` — Claude Code 项目上下文文件
- 创建 `project-log.md` — 项目进度日志（实时记录模板）

**项目文档清单：**
| 文件 | 说明 | 状态 |
|------|------|------|
| `.gitignore` | Git 忽略规则 | ✅ 已创建 |
| `project-plan.md` | 项目计划书 v2.0（36KB, 841行） | ✅ 已创建 |
| `ai-behavior-spec.md` | AI 操作行为规范 v1.0（29KB, 613行） | ✅ 已创建 |
| `claude-code-spec.md` | Claude Code 操作规范 v1.0（32KB） | ✅ 已创建 |
| `CLAUDE.md` | 项目上下文（Claude Code 自动读取） | ✅ 已创建 |
| `project-log.md` | 项目进度日志（持续维护） | ✅ 已创建 |

**遇到的问题：** 无

**Git 提交历史：**
| 提交 | 说明 |
|------|------|
| `9486226` | `chore: 初始化仓库，添加项目计划书 v2.0` |
| `4b3b227` | `docs: 添加 AI 操作行为规范 v1.0` |
| *(待提交)* | `docs: 添加 Claude Code 操作规范 v1.0 和项目日志` |

---

## 错误记录

| 日期 | 错误描述 | 影响范围 | 严重程度 | 状态 | 解决方案 |
|------|---------|---------|---------|------|---------|
| — | 暂无错误记录 | — | — | — | — |

---

## 待办清单

### Phase 1 — MVP 核心管线

- [x] **Step 1：** 创建 Maven 项目骨架
  - [x] 创建 `pom.xml`（含 JavaFX 插件、已锁定依赖版本）
  - [x] 创建包结构（config/model/pipeline/service/client/ui/util）
  - [x] 创建 model 层数据类（TextRegion, MangaPage, TranslationConfig, PageStatus, RegionStatus, InpaintStrategy, OcrVersion）
  - [x] 创建自定义异常体系（MangaTranslatorException 及其子类）
  - [x] 创建 `MangaTranslatorApp.java` 主入口
- [x] **Step 2：** 实现 Client 层
  - [x] 实现 `HttpUtil.java`（HTTP 请求工具类）
  - [x] 实现 `BaiduAuthManager.java`（access_token 管理、加密存储）
  - [x] 实现 `BaiduOcrClient.java`（OCR API 调用封装）
  - [x] 实现 `BaiduTranslateClient.java`（翻译 API 调用封装 + MD5 签名）
  - [x] 实现 `TokenBucketRateLimiter.java`（令牌桶限速）
  - [x] 实现 `CircuitBreaker.java`（熔断器）
- [x] **Step 3：** 实现 Service 层
  - [x] 实现 `OcrService` / `BaiduOcrServiceImpl`
  - [x] 实现 `TranslateService` / `BaiduTranslateServiceImpl`
  - [x] 实现 `InpaintService` / `WhiteInpaintServiceImpl`
  - [x] 实现 `RenderService` / `Graphics2DRenderServiceImpl`
- [x] **Step 4：** 实现 Pipeline 层
  - [x] 实现 `PipelineContext.java`
  - [x] 实现各 `TranslationStep` 步骤类
  - [x] 实现 `TranslationPipeline.java` 编排
- [ ] **Step 5：** 实现 UI 层（最简版）
  - [ ] 实现 `MainWindow.java` 主窗口
  - [ ] 实现 `NavBar.java` 导航栏
  - [ ] 实现 `CanvasPanel.java` 画布面板
  - [ ] 实现 `TextPanel.java` 文本面板
  - [ ] 实现 `FileListPanel.java` 文件列表
  - [ ] 实现 `BottomBar.java` 底部栏
  - [x] 实现 `ConfigManager.java` 配置管理
- [ ] **集成测试：** Phase 1 端到端验证

### Phase 2 — 品质提升

- [ ] SLF4J 日志替换（System.out → SLF4J）
- [ ] UI CSS 美化（深色主题）
- [ ] 文字区域高亮（F08）
- [ ] 翻译修正对话框（F09）
- [ ] 批量翻译（F10）
- [ ] 批量进度 + 日志面板（F11）
- [ ] 文件列表缩略图 + 右键菜单（F12）
- [ ] 缩放与导航（F13）
- [ ] 熔断器 + 令牌桶正式接入

### Phase 3 — 高级特性

- [ ] 拆分视图（F14）
- [ ] 语言方向设置（F15）
- [ ] 字体与字号设置（F16）
- [ ] 竖排文字渲染（F17）
- [ ] 首次引导（F18）
- [ ] 测试覆盖率完善
- [ ] 答辩准备

---

> 记录规则：每个功能完成后立即更新此文件，记录做了什么、改了哪些文件、遇到什么问题、如何解决，然后执行 Git 提交与推送。

# 漫画翻译器 — Claude Code 操作规范

**MangaTranslator · Claude Code 开发助手协作协议**

> 南昌航空大学 · 软件工程 2025 级暑期实训 · 漫画翻译器项目  
> 版本 v1.0 · 2026 年 7 月 10 日  
> 使用 Claude Code 作为开发助手时的操作边界、约束规则与协作协议

---

## 目录

1. [使用概述与定位](#1-使用概述与定位)
2. [项目上下文注入规范](#2-项目上下文注入规范)
3. [文件操作边界](#3-文件操作边界)
4. [代码生成规范](#4-代码生成规范)
5. [架构约束](#5-架构约束)
6. [依赖管理边界](#6-依赖管理边界)
7. [API 集成约束](#7-api-集成约束)
8. [构建与运行规范](#8-构建与运行规范)
9. [Git 操作规范](#9-git-操作规范)
10. [交互协议](#10-交互协议)
11. [分阶段开发约束](#11-分阶段开发约束)
12. [禁止行为总清单](#12-禁止行为总清单)
13. [验收检查清单](#13-验收检查清单)
14. [项目进度与质量记录规范](#14-项目进度与质量记录规范)

---

## 1. 使用概述与定位

### 1.1 Claude Code 在项目中的角色

Claude Code 是**开发辅助工具**，不是项目架构师或最终决策者。它的职责是：在用户给出的明确指令下，按照已确定的项目计划书和 AI 操作行为规范，**生成、修改、调试代码**。所有架构决策、技术选型、UI 设计已在项目计划书 v2.0 中锁定，Claude Code 不得擅自更改。

| 维度 | Claude Code 可以做 | Claude Code 不可以做 |
|------|-------------------|---------------------|
| 架构 | 按照计划书 4 层架构实现代码 | 自行发明架构、合并/拆分层级 |
| 功能 | 实现计划书中列出的 18 项功能 | 擅自添加计划书外的功能 |
| UI | 按照 UI 设计规格（颜色/尺寸/间距）实现界面 | 自行更改颜色值、布局结构、组件尺寸 |
| API | 按照百度 API 文档实现调用逻辑 | 更换 API 提供商、添加未规划的 API |
| 依赖 | 使用允许列表中的 Maven 依赖 | 自行引入未批准的第三方库 |
| 决策 | 在技术实现细节上做出合理选择 | 推翻已确定的技术选型或架构决策 |

> 🔴 **核心约束：** Claude Code 生成的所有代码必须与"漫画翻译器项目计划书 v2.0"和"AI 操作行为规范 v1.0"完全一致。如遇冲突，以这两份文档为准，Claude Code 必须提示用户并按文档修正。

### 1.2 工作交付规范

每完成一个功能模块后，必须执行以下步骤：

```
功能开发完成
  ↓ mvn compile 确认编译通过
  ↓ 确认功能正确运行
  ↓ 更新 project-log.md（记录进度、修改、问题）
  ↓ git add + git commit（符合提交信息格式）
  ↓ git push（推送到远程仓库）
  ↓ 进入下一个功能的开发
```

---

## 2. 项目上下文注入规范

### 2.1 CLAUDE.md — 项目上下文文件

在项目根目录创建 `CLAUDE.md` 文件，Claude Code 每次启动时自动读取。该文件是 Claude Code 理解项目约束的核心入口。

详见项目根目录实际 `CLAUDE.md` 文件内容。

### 2.2 上下文注入检查清单

每次会话开始前确认：

- [ ] CLAUDE.md 存在且内容为最新版本
- [ ] 项目计划书文档在项目目录中可访问
- [ ] AI 操作行为规范文档同上
- [ ] pom.xml 存在且依赖版本与计划书一致
- [ ] .gitignore 包含 config.json、target/、*.log
- [ ] project-log.md 存在且已有上阶段进度记录

---

## 3. 文件操作边界

### 3.1 目录结构与操作权限

```
manga-translator/
├── pom.xml                         [可写] Maven 构建配置
├── CLAUDE.md                       [可写] Claude Code 上下文
├── .gitignore                      [可写] Git 忽略规则
├── README.md                       [可写] 项目说明
├── project-log.md                  [可写] 项目进度日志（持续更新）
├── project-plan.md                 [可写] 项目计划书 Markdown 版
├── ai-behavior-spec.md             [可写] AI 行为规范 Markdown 版
├── src/
│   ├── main/
│   │   ├── java/com/manga/translator/
│   │   │   ├── MangaTranslatorApp.java       [可写] 主入口
│   │   │   ├── pipeline/                     [可写] 管线编排层
│   │   │   ├── service/                      [可写] 业务逻辑层
│   │   │   ├── client/                       [可写] API客户端层
│   │   │   ├── model/                        [可写] 数据模型
│   │   │   ├── ui/                           [可写] UI界面层
│   │   │   ├── config/                       [可写] 配置管理
│   │   │   └── util/                         [可写] 工具类
│   │   └── resources/
│   │       ├── fonts/                        [可写] 字体文件
│   │       └── styles/                       [可写] CSS样式
│   └── test/java/com/manga/translator/       [可写] 测试代码
├── docs/                                     [谨慎] 文档目录，修改前确认
│   ├── 漫画翻译器项目计划书.html               [只读] 架构权威文档
│   └── 漫画翻译器AI操作行为规范.html            [只读] 行为规范文档
├── ~/.manga-translator/                      [禁止] 用户配置目录
│   └── config.json                           [禁止] 含真实API密钥
├── target/                                   [禁止] 构建输出，自动生成
└── .git/                                     [禁止] Git内部数据
```

### 3.2 文件操作权限矩阵

| 操作 | 创建 | 修改 | 删除 | 约束 |
|------|------|------|------|------|
| src/main/java/ 源码 | ✅ | ✅ | ❌ | 删除源码文件必须用户确认 |
| src/test/ 测试代码 | ✅ | ✅ | ⚠️ | 仅允许删除自己创建的测试文件 |
| pom.xml | — | ⚠️ | ❌ | 仅允许在"依赖允许列表"范围内添加依赖 |
| resources/ 资源文件 | ✅ | ✅ | ❌ | 字体文件需用户提供 |
| docs/ 文档 | ✅ | ❌ | ❌ | 计划书和规范文档为只读 |
| project-log.md | ✅ | ✅ | ❌ | 持续更新，不可删除 |
| CLAUDE.md | ✅ | ✅ | ❌ | 更新上下文时修改 |
| .gitignore | — | ✅ | ❌ | 只增不删忽略规则 |
| config.json (用户配置) | ❌ | ❌ | ❌ | 完全禁止操作 |
| target/ (构建输出) | ❌ | ❌ | ⚠️ | 仅允许 mvn clean 触发删除 |

> 🔴 **绝对禁止的文件操作：**
> 1. 不得读取、创建、修改或删除 `~/.manga-translator/config.json`（含真实 API 密钥）
> 2. 不得修改 `docs/` 下的计划书和规范 HTML 文档
> 3. 不得删除已有源码文件（除非用户明确指令）
> 4. 不得在源码中创建新的顶层包（包名根必须是 `com.manga.translator`）
> 5. 不得创建 .sh/.bat 脚本文件（构建用 Maven 命令）
> 6. 不得创建 .sql/.db 数据库文件（本项目不使用数据库）

---

## 4. 代码生成规范

### 4.1 命名规范

| 类型 | 规范 | 示例 | 违反处理 |
|------|------|------|---------|
| 包名 | 全小写，`com.manga.translator` 开头 | `com.manga.translator.pipeline` | 🔴 拒绝 |
| 类名 | PascalCase，名词 | `TranslationPipeline` | 🔴 拒绝 |
| 接口名 | PascalCase，不加 `I` 前缀 | `OcrService`（非 `IOcrService`） | 🔴 拒绝 |
| 实现类名 | 接口名 + `Impl` 后缀 | `OcrServiceImpl`, `BaiduOcrClient` | 🔴 拒绝 |
| 方法名 | camelCase，动词开头 | `executeOcr()`, `translateText()` | 🔴 拒绝 |
| 常量名 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_QPS` | 🔴 拒绝 |
| 枚举值 | UPPER_SNAKE_CASE | `OcrVersion.GENERAL_BASIC` | 🔴 拒绝 |
| 局部变量 | camelCase | `textRegion`, `translatedText` | 🟡 警告 |
| 布尔变量 | `is/has/can/should` 前缀 | `isVertical`, `hasTranslation` | 🟡 警告 |

### 4.2 类结构规范

```java
/**
 * OCR 检测服务接口。
 * 负责调用百度 OCR API 识别图片中的文字区域。
 */
public interface OcrService {

    /**
     * 对指定图片执行 OCR 识别。
     *
     * @param image 漫画原图（非 null）
     * @return 识别到的文字区域列表（可能为空，不为 null）
     * @throws OcrException 当 API 调用失败时抛出
     */
    List<TextRegion> recognize(BufferedImage image) throws OcrException;
}

/**
 * 百度 OCR 服务实现。
 * 使用百度通用文字识别 API。
 */
public class BaiduOcrServiceImpl implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(BaiduOcrServiceImpl.class);
    private static final int MAX_RETRY_COUNT = 3;
    private static final double MIN_PROBABILITY = 0.5;

    private final BaiduOcrClient client;
    private final TokenBucketRateLimiter rateLimiter;

    // 构造器注入，不用字段注入
    public BaiduOcrServiceImpl(BaiduOcrClient client, TokenBucketRateLimiter rateLimiter) {
        this.client = Objects.requireNonNull(client);
        this.rateLimiter = Objects.requireNonNull(rateLimiter);
    }

    @Override
    public List<TextRegion> recognize(BufferedImage image) throws OcrException {
        // 实现逻辑...
    }
}
```

### 4.3 代码生成约束

> 🔴 **必须遵守：**
> 1. 每个类/接口/公有方法必须有 JavaDoc 注释（中文）
> 2. 依赖注入使用**构造器注入**，不使用字段注入（@Autowired on field）
> 3. 所有外部 API 调用必须有 try-catch，不得向上层抛出未处理异常
> 4. 常量用 `static final` 定义，不使用魔法数字
> 5. 所有日志使用 SLF4J（`LoggerFactory.getLogger`），不使用 `System.out.println`
> 6. 返回集合不返回 null，用 `Collections.emptyList()` 代替
> 7. **方法行数 ≤ 50 行**，超过必须拆分

> 🟡 **禁止的代码模式：**
> 1. 禁止使用 `System.out.println` / `System.err.println` 做日志输出
> 2. 禁止使用 `e.printStackTrace()`，必须用 `log.error("描述", e)`
> 3. 禁止在 UI 线程（JavaFX Application Thread）中执行网络请求
> 4. 禁止使用 `new Thread()` 裸线程，必须用 `ExecutorService` 线程池
> 5. 禁止使用 `@Autowired` 字段注入，用构造器注入
> 6. 禁止使用魔法数字（如 0.5），必须定义常量
> 7. 禁止在源码中出现 API Key / Secret Key 字面量
> 8. 禁止使用 `var` 关键字（Java 10+），显式声明类型以提高可读性

### 4.4 异常处理规范

```java
// 异常基类
public class MangaTranslatorException extends RuntimeException {
    public MangaTranslatorException(String message) { super(message); }
    public MangaTranslatorException(String message, Throwable cause) { super(message, cause); }
}

// OCR 异常
public class OcrException extends MangaTranslatorException { ... }

// 翻译异常
public class TranslationException extends MangaTranslatorException { ... }

// API 鉴权异常
public class AuthException extends MangaTranslatorException { ... }

// 限流异常
public class RateLimitException extends MangaTranslatorException { ... }

// 配置异常
public class ConfigException extends MangaTranslatorException { ... }
```

> 🟢 **异常处理原则：**
> 1. Client 层捕获 HTTP/IO 异常，转换为 MangaTranslatorException 子类
> 2. Service 层捕获自身业务异常，决定重试或向上抛出
> 3. Pipeline 层捕获步骤异常，决定级联中断或降级
> 4. UI 层捕获所有未处理异常，显示 Toast 提示，不得让异常导致应用崩溃
> 5. 熔断器和限速器异常不得被吞掉，必须记录日志并触发对应机制

---

## 5. 架构约束

### 5.1 分层依赖规则（不可违背）

```
允许的依赖方向：
UI 层    → Pipeline 层
Pipeline 层 → Service 层
Service 层  → Client 层
Service 层  → Model 层
Client 层   → Model 层

禁止的依赖方向：
🔴 UI 层    → Service 层    [禁止！UI 不直接调用 Service]
🔴 UI 层    → Client 层    [禁止！UI 不直接调用 API]
🔴 Pipeline 层 → Client 层 [禁止！Pipeline 不直接调用 API]
🔴 Client 层 → Service 层  [禁止！底层不依赖上层]
🔴 Client 层 → Pipeline 层 [禁止！]
🔴 Model 层 → 任何上层     [禁止！Model 是纯数据类]
```

### 5.2 接口与实现分离规则

| 层 | 接口（必须存在） | 实现（可以有多个） | 规则 |
|----|----------------|-------------------|------|
| Service | `OcrService`, `TranslateService`, `InpaintService`, `RenderService` | `BaiduOcrServiceImpl`, `BaiduTranslateServiceImpl`, ... | UI/Pipeline 只依赖接口，不依赖实现 |
| Client | `BaiduOcrClient`, `BaiduTranslateClient` | 具体的 HTTP 调用实现 | Service 只依赖 Client 接口 |
| Pipeline | `TranslationStep`（步骤接口） | `OcrStep`, `TranslateStep`, `InpaintStep`, `RenderStep` | 每个步骤实现接口，可替换 |
| UI | — | `MainWindow`, `CanvasPanel`, `TextPanel` 等 | UI 类不需要接口（JavaFX 控制器） |

### 5.3 管线编排约束

> 🔵 **管线步骤不可乱序：** 5 步管线必须按固定顺序执行：**OCR → 文本清洗 → 翻译 → 修补 → 回填**。不得跳过步骤、不得调换顺序。每个步骤的输入是上一个步骤的输出，通过 `PipelineContext` 传递。

```java
public class PipelineContext {
    private BufferedImage originalImage;       // Step 1 前：原图
    private List<TextRegion> textRegions;      // Step 1 后：OCR结果
    private List<TextRegion> cleanedRegions;   // Step 2 后：清洗结果
    private BufferedImage inpaintedImage;      // Step 4 后：修补图
    private BufferedImage resultImage;         // Step 5 后：最终图
    private TranslationConfig config;          // 翻译配置
    private List<LogEntry> logs;               // 操作日志
    // getter/setter ...
}
```

---

## 6. 依赖管理边界

### 6.1 Maven 依赖允许列表

| 依赖 | 版本（锁定） | 用途 | 可替换 |
|------|------------|------|--------|
| `org.openjfx:javafx-controls` | 17.0.8 | JavaFX UI 控件 | ❌ |
| `org.openjfx:javafx-fxml` | 17.0.8 | JavaFX FXML 支持 | ❌ |
| `org.openjfx:javafx-swing` | 17.0.8 | JavaFX Swing 互操作 | ❌ |
| `com.google.code.gson:gson` | 2.10.1 | JSON 解析（百度 API 返回） | ❌ |
| `org.apache.httpcomponents.client5:httpclient5` | 5.3 | HTTP 客户端（调用百度 API） | ❌ |
| `org.slf4j:slf4j-api` | 2.0.9 | 日志接口 | ❌ |
| `ch.qos.logback:logback-classic` | 1.4.11 | 日志实现 | ❌ |
| `org.junit.jupiter:junit-jupiter` | 5.10.0 | 单元测试 | ❌ |
| `org.mockito:mockito-core` | 5.5.0 | 测试 Mock | ❌ |

> 🔴 **依赖管理红线：**
> 1. 不得引入允许列表之外的任何第三方依赖
> 2. 不得修改已锁定依赖的版本号
> 3. 不得引入 Lombok（本项目手写 getter/setter）
> 4. 不得引入 Spring/Spring Boot（本项目不使用 IoC 容器，手动 new 对象）
> 5. 不得引入任何数据库 ORM 框架（MyBatis/Hibernate）
> 6. 如需引入新依赖，必须在 Prompt 中向用户说明理由并获得确认
> 7. 不得使用 SNAPSHOT 版本依赖

### 6.2 pom.xml 约束

```xml
<!-- pom.xml 必须包含的配置 -->

<!-- JDK 版本锁定 -->
<maven.compiler.source>17</maven.compiler.source>
<maven.compiler.target>17</maven.compiler.target>

<!-- JavaFX 模块配置 -->
<modules>
  <module>javafx.controls</module>
  <module>javafx.fxml</module>
</modules>

<!-- JavaFX 运行插件 -->
<plugin>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-maven-plugin</artifactId>
  <version>0.0.8</version>
</plugin>

<!-- 编码锁定 -->
<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
```

---

## 7. API 集成约束

### 7.1 百度 API 调用约束

| 参数 | 值 |
|------|-----|
| OCR URL（标准版） | `https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic` |
| OCR URL（高精度版） | `https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic` |
| 翻译 URL | `https://fanyi-api.baidu.com/api/trans/vip/translate` |
| Token URL | `https://aip.baidubce.com/oauth/2.0/token` |
| 请求方式 | POST（application/x-www-form-urlencoded） |
| 超时设置 | 连接超时 10s，读取超时 30s |
| 重试策略 | 最多3次，间隔 2/4/8s 指数退避 |
| 限速 | OCR ≤1.5 QPS，翻译 ≤8 QPS |
| 熔断 | 连续5次失败 → 60s 熔断 → 半开探测 |

### 7.2 密钥处理约束

> 🔴 **密钥安全红线（零容忍）：**
> 1. 生成代码中不得出现任何 API Key / Secret Key 的字面量
> 2. 不得在注释、JavaDoc、变量名中包含真实密钥值
> 3. 密钥从 config.json 读取，config.json 由用户手动配置
> 4. 日志中不得输出密钥值（包括 sign 计算过程中的 secret_key）
> 5. 密钥在内存中使用后不得被序列化到磁盘
> 6. UI 中显示密钥时必须掩码（仅显示前4位）
> 7. 代码中不得硬编码 access_token，必须动态获取

### 7.3 API 响应处理约束

> 🟡 **响应解析规则：**
> 1. 必须检查 HTTP 状态码，非 200 视为失败
> 2. 必须检查百度返回 JSON 中的 error_code 字段，非 0 视为失败
> 3. 必须检查 words_result / trans_result 是否为 null
> 4. 解析 JSON 失败时不得吞掉异常，必须记录日志并抛出
> 5. 不得假设 API 返回字段一定存在，所有字段用 optXXX 而非 getXXX
> 6. OCR probability 字段可能为 null（某些版本不返回），需做空值判断

---

## 8. 构建与运行规范

### 8.1 构建命令

| 操作 | 命令 | 说明 |
|------|------|------|
| 编译 | `mvn compile` | 编译 src/main/java 到 target/classes |
| 测试 | `mvn test` | 运行 src/test/java 下的所有测试 |
| 打包 | `mvn package -DskipTests` | 打包为 JAR（跳过测试） |
| 运行 | `mvn javafx:run` | 通过 JavaFX Maven 插件运行 |
| 清理 | `mvn clean` | 删除 target/ 目录 |

> 🔴 **构建红线：**
> 1. 不得使用 `mvn install` 将项目安装到本地 Maven 仓库（实训环境不需要）
> 2. 不得使用 `-Dmaven.javadoc.skip=true` 跳过 JavaDoc 生成
> 3. 打包后的 JAR 文件名必须为 `manga-translator-{version}.jar`
> 4. 构建失败时不得修改 pom.xml 绕过错误，必须修复根本原因

### 8.2 测试规范

| 维度 | 规则 |
|------|------|
| 测试框架 | JUnit 5 + Mockito（仅限允许列表中的版本） |
| 测试类命名 | 被测类名 + `Test`（如 `OcrServiceImplTest`） |
| 测试方法命名 | `should_期望行为_when_前置条件`（如 `shouldReturnTextRegions_whenImageValid`） |
| 测试覆盖范围 | Service 层和 Client 层必须写单元测试；UI 层不写自动化测试 |
| Mock 使用 | API 调用必须 Mock，不得在测试中真实调用百度 API |
| 测试数据 | 使用项目内的测试图片（`src/test/resources/`） |
| 断言 | 使用 JUnit 5 Assertions，不使用 JUnit 4 Assert |

---

## 9. Git 操作规范

### 9.1 分支策略

| 分支 | 用途 | 创建方式 | 合并方式 |
|------|------|---------|---------|
| `main` | 稳定版本，可运行 | — | — |
| `feature/{name}` | 开发新功能 | 从 main 创建 | 合并到 main（需测试通过） |
| `fix/{name}` | 修复 Bug | 从 main 创建 | 合并到 main |

### 9.2 提交信息规范

```bash
# 格式：type: 简述

feat: 实现百度OCR客户端调用逻辑
fix: 修复翻译结果为空时未触发重试的问题
refactor: 重构PipelineContext为不可变对象
docs: 更新project-log.md项目进度日志
test: 添加OcrServiceImpl单元测试
chore: 更新.gitignore排除config.json
```

### 9.3 功能完成后的提交流程

每完成一个功能模块后，必须严格按照以下流程操作：

```bash
# Step 1: 编译确认
mvn compile

# Step 2: 确认功能正常运行（手动测试）

# Step 3: 更新 project-log.md
# 记录：完成的功能、修改的文件、遇到的问题、解决方式

# Step 4: 检查 .gitignore 确保不含敏感文件
git status

# Step 5: 暂存并提交
git add -A
git commit -m "feat: 实现XXX功能"

# Step 6: 推送到远程
git push origin main
```

> 🟡 **Git 操作约束：**
> 1. 不得使用 `git push --force`（除非用户明确要求）
> 2. 不得使用 `git commit --no-verify`（跳过钩子）
> 3. 不得使用 `git reset --hard`（除非用户明确要求）
> 4. 提交前必须确保 `mvn compile` 通过
> 5. 不得在提交中包含 config.json、target/、*.log
> 6. 提交信息必须使用上述格式，不得使用 "update"、"fix bug" 等模糊描述
> 7. 不得将 .gitignore 中的已有规则删除

### 9.4 .gitignore 必须包含的条目

```gitignore
# Maven
target/

# IDE
.idea/
*.iml
.vscode/
.settings/
.classpath
.project

# 用户配置（含API密钥）
config.json
~/.manga-translator/

# 日志
*.log
logs/

# 操作系统
.DS_Store
Thumbs.db

# 临时文件
*.tmp
*.bak
```

---

## 10. 交互协议

### 10.1 Prompt 编写原则

> 🔵 **好的 Prompt 应该：**
> 1. 指明要实现的具体功能（对应计划书中的功能编号）
> 2. 指明要操作的文件路径和类名
> 3. 指明要遵守的规范文件
> 4. 指明输入输出格式
> 5. 指明异常处理要求

### 10.2 Prompt 示例对比

#### ✅ 好的 Prompt 示例

```
实现 OcrService 接口的百度 OCR 实现类 BaiduOcrServiceImpl。

要求：
1. 按照「AI操作行为规范」第3章的参数规范调用百度OCR API
2. 请求参数：image=Base64, language_type=JAP, detect_direction=true,
   detect_language=true, probability=true, vertex_location=true
3. 置信度门控：probability < 0.5 的结果丢弃
4. 坐标扩展：location 向外扩展 12px
5. 重试策略：最多3次，间隔2/4/8秒指数退避
6. 限速：使用 TokenBucketRateLimiter，QPS ≤ 1.5
7. 日志：使用SLF4J，记录识别条目数、平均置信度
8. 异常：API失败抛出 OcrException，不吞掉异常
9. 密钥：从 ConfigManager 获取，不硬编码
```

#### ❌ 坏的 Prompt 示例

```
- "帮我写一个OCR的代码"
- "实现翻译功能，用百度API"
- "写一个漫画翻译器的完整代码"
- "帮我搭项目骨架，所有代码都写出来"
- "修复bug"
```

### 10.3 推荐的分步 Prompt 策略

不要让 Claude Code 一次生成所有代码。按模块分步推进：

| 步骤 | 内容 | 阶段 |
|------|------|------|
| Step 1 | 创建 pom.xml + .gitignore + CLAUDE.md + 包结构 + model 数据类 | Phase 1 |
| Step 2 | 实现 Client 层（BaiduAuthManager、BaiduOcrClient、BaiduTranslateClient、TokenBucketRateLimiter） | Phase 1 |
| Step 3 | 实现 Service 层（OcrServiceImpl、TranslateServiceImpl、InpaintServiceImpl、RenderServiceImpl） | Phase 1 |
| Step 4 | 实现 Pipeline 层（PipelineContext、各 TranslationStep、TranslationPipeline、CircuitBreaker） | Phase 1 |
| Step 5 | 实现 UI 层（MainWindow、CanvasPanel、TextPanel、FileListPanel、各 Dialog） | Phase 2 |
| Step 6 | 编写单元测试 + 集成测试 + 端到端验证 | Phase 2 |

---

## 11. 分阶段开发约束

### Phase 1 — MVP 核心管线（第1-2周）

实现 F01-F07（P0 功能）：图片导入、OCR识别、文字翻译、原文擦除、译文回填、导出结果、双栏对比。

| 允许 | 不允许 | 说明 |
|------|--------|------|
| 简陋的 JavaFX 界面 | CSS 美化、动画 | Phase 1 先跑通管线 |
| 白色覆盖修补 | 取色填充修补 | 取色填充是 Phase 2 |
| 横排文字渲染 | 竖排文字渲染 | 竖排是 Phase 2 |
| 单张翻译 | 批量翻译 | 批量是 Phase 2 |
| 基础重试逻辑 | 熔断器、令牌桶 | 容灾机制是 Phase 2 |
| 控制台日志输出 | 日志面板 UI | Phase 1 用 System.out 临时替代 |

> 🟡 **Phase 1 临时放宽：** Phase 1 期间临时允许使用 `System.out.println` 做调试输出（但必须标注 `TODO` 注释，Phase 2 替换为 SLF4J）。其他规范（命名、架构、密钥安全、异常处理）不允许放宽。

### Phase 2 — 品质提升（第3周）

实现 F08-F13（P1 功能）：文字区域高亮、翻译修正、批量翻译、批量进度、文件列表、缩放与导航。

| Phase 2 必须完成 | 说明 |
|-----------------|------|
| SLF4J 日志替换 | 所有 System.out → log.info/warn/error |
| 熔断器实现 | CircuitBreaker 类，连续5次失败触发 |
| 令牌桶限速 | TokenBucketRateLimiter，OCR≤1.5QPS，翻译≤8QPS |
| UI CSS 美化 | 按照计划书颜色系统/尺寸/间距规格实现 |
| 竖排文字渲染 | direction=1 时逐字符竖排 |
| 人工修正闭环 | 双击文本面板修正译文，AI不覆盖用户修正 |

### Phase 3 — 高级特性（第4周，可选）

实现 F14-F18（P2 功能）：拆分视图、语言方向设置、字体与字号设置、首次引导。完善测试覆盖率，准备答辩演示。

---

## 12. 禁止行为总清单

以下 **25 条**为 Claude Code 操作的绝对红线，任何情况下都不得违反：

| 编号 | 红线内容 |
|------|---------|
| 01 | 🔴 禁止在源码中硬编码 API Key / Secret Key / access_token |
| 02 | 🔴 禁止读取或修改 `~/.manga-translator/config.json` |
| 03 | 🔴 禁止修改 `docs/` 下的计划书和规范 HTML 文档 |
| 04 | 🔴 禁止引入允许列表之外的 Maven 依赖 |
| 05 | 🔴 禁止修改已锁定依赖的版本号 |
| 06 | 🔴 禁止引入 Lombok / Spring / 数据库 ORM 框架 |
| 07 | 🔴 禁止 UI 层直接调用 Service 层或 Client 层 |
| 08 | 🔴 禁止在 JavaFX Application Thread 中执行网络请求 |
| 09 | 🔴 禁止使用 `new Thread()` 裸线程，必须用线程池 |
| 10 | 🔴 禁止使用 `@Autowired` 字段注入（用构造器注入） |
| 11 | 🔴 禁止使用 `System.out.println` 做日志输出（Phase 2 起） |
| 12 | 🔴 禁止使用 `e.printStackTrace()`（用 `log.error`） |
| 13 | 🔴 禁止使用魔法数字（必须定义常量） |
| 14 | 🔴 禁止返回 null 集合（用 `Collections.emptyList()`） |
| 15 | 🔴 禁止在日志中输出密钥明文或图片 Base64 数据 |
| 16 | 🔴 禁止在 .gitignore 中删除 config.json 忽略规则 |
| 17 | 🔴 禁止使用 `git push --force` / `git reset --hard`（除非用户明确要求） |
| 18 | 🔴 禁止使用 `git commit --no-verify` |
| 19 | 🔴 禁止删除已有源码文件（除非用户明确指令） |
| 20 | 🔴 禁止创建 .sh / .bat 脚本文件 |
| 21 | 🔴 禁止创建 .sql / .db 数据库文件 |
| 22 | 🔴 禁止擅自更改已确定的 UI 颜色值、布局结构、组件尺寸 |
| 23 | 🔴 禁止擅自添加计划书外的功能 |
| 24 | 🔴 禁止使用 `var` 关键字 |
| 25 | 🔴 禁止方法行数超过 50 行 |

---

## 13. 验收检查清单

每个功能模块开发完成后，在交付前必须逐条检查以下 **18 项**：

| 编号 | 检查项 | 状态 |
|------|--------|------|
| 01 | 源码中无任何硬编码 API Key / Secret Key | ☐ |
| 02 | .gitignore 包含 config.json、target/、*.log | ☐ |
| 03 | 代码符合 4 层架构（UI → Pipeline → Service → Client） | ☐ |
| 04 | Service 层接口与实现分离，UI/Pipeline 依赖接口 | ☐ |
| 05 | 构造器注入，无 @Autowired 字段注入 | ☐ |
| 06 | 所有日志使用 SLF4J（Phase 2 起） | ☐ |
| 07 | 无魔法数字，常量已定义 | ☐ |
| 08 | 集合返回不使用 null | ☐ |
| 09 | 异常使用自定义异常体系（MangaTranslatorException 子类） | ☐ |
| 10 | API 调用有 try-catch，异常不向上层裸抛 | ☐ |
| 11 | 网络请求不在 JavaFX Application Thread 中执行 | ☐ |
| 12 | 线程使用 ExecutorService，无裸 Thread | ☐ |
| 13 | API 调用参数与「AI操作行为规范」一致 | ☐ |
| 14 | 重试策略：最多3次，间隔 2/4/8s | ☐ |
| 15 | 限速：OCR ≤1.5QPS，翻译 ≤8QPS | ☐ |
| 16 | UI 颜色/尺寸/间距与计划书规格一致 | ☐ |
| 17 | `mvn compile` 编译通过 | ☐ |
| 18 | 提交信息符合格式规范 | ☐ |

---

## 14. 项目进度与质量记录规范

### 14.1 project-log.md — 项目进度日志

项目根目录必须维护 `project-log.md` 文件，用于**实时记录**项目的全部开发过程。每次功能完成后、提交 Git 前，必须更新此文件。

### 14.2 日志模板结构

```markdown
# 漫画翻译器 — 项目进度日志

> 版本：v1.0
> 最后更新：2026-07-10

---

## 总体进度概览

| 阶段 | 计划时间 | 状态 | 完成度 |
|------|---------|------|--------|
| Phase 1 — MVP 核心管线 | 第1-2周 | 🟡 进行中 | 0% |
| Phase 2 — 品质提升 | 第3周 | ⚪ 未开始 | 0% |
| Phase 3 — 高级特性 | 第4周 | ⚪ 未开始 | 0% |

### 功能完成状态

| 编号 | 功能 | 优先级 | 状态 | 完成日期 | 备注 |
|------|------|--------|------|---------|------|
| F01 | 图片导入 | P0 | ⚪ 待开发 | — | — |
| F02 | OCR 识别 | P0 | ⚪ 待开发 | — | — |
| F03 | 文字翻译 | P0 | ⚪ 待开发 | — | — |
| ... | ... | ... | ... | ... | ... |

---

## 开发记录

### [2026-07-10] 项目初始化

**完成内容：**
- 初始化 Git 仓库
- 创建 .gitignore
- 编写项目计划书 project-plan.md
- 编写 AI 操作行为规范 ai-behavior-spec.md
- 编写 Claude Code 操作规范 claude-code-spec.md
- 创建 CLAUDE.md 项目上下文文件
- 创建 project-log.md 项目进度日志

**修改文件：**
- `.gitignore` — 初始化 Git 忽略规则
- `project-plan.md` — 完整项目计划书（12章）
- `ai-behavior-spec.md` — AI 操作行为规范（12章）
- `claude-code-spec.md` — Claude Code 操作规范（14章）
- `CLAUDE.md` — 项目上下文文件
- `project-log.md` — 项目进度日志

**遇到的问题：** 无

**解决方式：** —

**待办事项：**
- [ ] 创建 Maven 项目骨架（pom.xml）
- [ ] 创建包结构和空的接口/类
- [ ] 创建 model 层数据类

**Git 提交：** `chore: 初始化仓库，添加项目文档和规范文件`

---

### [2026-07-XX] 功能名称（示例）

**完成内容：**
- 实现了 xxx 功能（对应 FXX）
- ...

**修改文件：**
- `src/main/java/XXX.java` — 新增 xxx 实现
- ...

**遇到的问题：**
- 问题 1：xxx
- 问题 2：xxx

**解决方式：**
- 方案 1：xxx
- 方案 2：xxx

**待办事项：**
- [ ] 下一步计划

**Git 提交：** `feat: 实现XXX功能`

---

## 错误记录

| 日期 | 错误描述 | 影响范围 | 严重程度 | 状态 | 解决方案 |
|------|---------|---------|---------|------|---------|
| 2026-07-XX | xxx | xxx | 🟡 中 | ✅ 已解决 | xxx |

---

## 里程碑检查

| 里程碑 | 计划日期 | 实际日期 | 状态 | 备注 |
|--------|---------|---------|------|------|
| M1 项目初始化 | 7/11 | — | ⚪ 未开始 | — |
| M2 API 鉴权连通 | 7/13 | — | ⚪ 未开始 | — |
| ... | ... | ... | ... | ... |
```

### 14.3 更新日志的时机

必须在以下时机更新 `project-log.md`：

| 时机 | 必须记录的内容 |
|------|--------------|
| **每个功能完成时** | 功能编号、完成内容、修改的文件列表 |
| **遇到 Bug 时** | 错误描述、影响范围、严重程度、解决方式 |
| **每次 Git 提交前** | 本次提交的汇总、Git commit message |
| **每个里程碑达成时** | 里程碑编号、实际完成日期、交付物清单 |
| **架构/设计变更时** | 变更原因、变更内容、影响范围 |
| **API 配置变更时** | 变更的 API、参数、原因 |

### 14.4 记录规范要求

1. **实时性：** 功能完成后立即记录，不积压
2. **完整性：** 每个条目包含"做了什么、改了哪些文件、遇到什么问题、如何解决"
3. **可追溯：** 每个记录条目对应一个 Git commit，方便追溯代码变更
4. **错误记录：** 所有 Bug 和异常必须记录，无论是否已修复
5. **清晰度：** 使用 Markdown 表格和列表，保持结构化

---

## 版本信息

| 项目 | 内容 |
|------|------|
| 规范版本 | v1.0 |
| 制定日期 | 2026-07-10 |
| 适用项目 | 漫画翻译器（南昌航空大学 暑期实训） |
| 配套文档 | [project-plan.md](project-plan.md) · [ai-behavior-spec.md](ai-behavior-spec.md) |
| 维护者 | 项目开发团队 |
| 下次复审 | Phase 1 骨架搭建完成后 |

---

> 漫画翻译器 — Claude Code 操作规范 v1.0 · 南昌航空大学软件工程 2025 级暑期实训

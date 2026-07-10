# 漫画翻译器 - Claude Code 项目上下文

## 项目信息
- 名称: 漫画翻译器 (Manga Translator)
- 技术栈: JDK 17 + JavaFX 17.0.8 + Maven 3.9+
- 外部API: 百度OCR API + 百度翻译API
- 构建工具: Maven
- 目标平台: Windows / macOS / Linux (跨平台桌面应用)

## 核心规范文件 (必须遵守)
- project-plan.md — 架构/功能/UI 规格 (12章)
- ai-behavior-spec.md — AI 模块行为边界 (12章)
- claude-code-spec.md — 开发操作边界 (14章)
- 本文件 (CLAUDE.md) — 快速参考上下文

## 架构约束 (不可更改)
- 4层分层架构: UI层 → Pipeline层 → Service层 → Client层
- UI层不直接调用Service层, 必须通过Pipeline层
- Service层接口与实现分离 (接口: OcrService, 实现: BaiduOcrServiceImpl)
- Client层封装所有外部API调用
- 5步管线固定顺序: OCR → 文本清洗 → 翻译 → 修补 → 回填

## 包结构
- 包名根: com.manga.translator
- 子包: config, model, pipeline, service(.impl), client, ui, util
- 测试: test/java/com/manga/translator/

## 代码规范
- 语言: Java 17
- 命名: 类名PascalCase, 方法名camelCase, 常量UPPER_SNAKE
- 接口不加 I 前缀 (OcrService, 非 IOcrService)
- 注释: 中文JavaDoc, 类/接口/公有方法必须有
- 日志: SLF4J (LoggerFactory.getLogger), 无 System.out (Phase2起)
- 异常: 自定义异常继承MangaTranslatorException
- 集合: 返回空用 Collections.emptyList(), 不用 null
- 注入: 构造器注入, 无 @Autowired 字段注入
- 线程: ExecutorService 线程池, 无裸 new Thread()
- UI线程: 网络请求不在 JavaFX Application Thread 中执行
- 禁止: var 关键字, 魔法数字, e.printStackTrace(), Lombok

## Maven依赖 (已锁定, 不可改版本)
- org.openjfx:javafx-controls:17.0.8
- org.openjfx:javafx-fxml:17.0.8
- org.openjfx:javafx-swing:17.0.8
- com.google.code.gson:gson:2.10.1
- org.apache.httpcomponents.client5:httpclient5:5.3
- org.slf4j:slf4j-api:2.0.9
- ch.qos.logback:logback-classic:1.4.11
- org.junit.jupiter:junit-jupiter:5.10.0
- org.mockito:mockito-core:5.5.0

## API 集成
- OCR (标准版): POST https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic
- OCR (高精度版): POST https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic
- 翻译: POST https://fanyi-api.baidu.com/api/trans/vip/translate
- Token: POST https://aip.baidubce.com/oauth/2.0/token
- 超时: 连接10s, 读取30s
- 重试: 最多3次, 2/4/8s 指数退避
- OCR限速: ≤1.5 QPS, 翻译限速: ≤8 QPS
- 熔断: 连续5次失败 → 60s熔断 → 半开探测

## API密钥安全
- 密钥存储在 ~/.manga-translator/config.json (AES-128加密)
- 源码中严禁硬编码API Key
- .gitignore 必须排除 config.json
- 日志中禁止输出密钥明文
- UI 显示密钥必须掩码 (显示前4位)

## 构建命令
- 编译: mvn compile
- 测试: mvn test
- 打包: mvn package -DskipTests
- 运行: mvn javafx:run
- 清理: mvn clean

## Git规范
- 主分支: main
- 功能分支: feature/{功能名}
- 提交格式: feat/fix/refactor/docs/test/chore: 简述
- 提交前需要: mvn compile 通过 + 更新 project-log.md + git push

## 功能完成流程
每个功能完成后必须执行:
1. mvn compile 确认编译通过
2. 确认功能正常运行
3. 更新 project-log.md (记录进度/修改/问题)
4. git add + git commit (格式规范)
5. git push

## 项目进度记录
- project-log.md 持续维护, 记录每次开发的内容/修改/错误
- 每个功能完成/每个 Bug 修复/每个里程碑达成时更新

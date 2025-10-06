# Repository Guidelines

## 项目结构与模块组织
- **app 模块**：入口位于 `app/src/main/java/com/rapid/android`，下设 `init/`、`ui/`、`network/`、`utils/` 等包，负责应用启动、界面挂载与依赖注入。
- **core 多模块**：`core:common`、`core:log`、`core:datastore`、`core:network`、`core:ui`、`core:webview`、`core:data`、`core:domain` 按能力拆分，源文件在各自 `src/main/java` 下并遵循 `data/`、`di/`、`ui/` 等子目录；资源命名统一添加模块前缀（示例：`core_ui_*`）。
- **构建与配置支撑**：通用 Gradle 插件存于 `build-logic`，根级脚本位于 `build.gradle`、`settings.gradle`；环境配置集中在 `gradle/` 与 `gradle.properties`，各模块产物写入其 `build/` 目录。

## 构建、测试与开发命令
- `./gradlew assembleDebug`：编译全部模块并生成调试 APK。
- `./gradlew lint`：执行 Android Lint，提交前需确保无新增警告。
- `./gradlew test`：运行全部 JVM 单元测试，可使用 `./gradlew :module:test` 精确到模块。
- `./gradlew :app:connectedDebugAndroidTest`：在连接设备或模拟器上跑仪器化测试。

## 代码风格与命名规范
- **语言与版本**：仅使用 Java 11，禁止 Kotlin 与 Jetpack Compose。
- **格式**：缩进 4 空格，方法括号与声明同行，遵循现有格式化。
- **命名**：类/枚举用 UpperCamelCase，方法与变量用 lowerCamelCase，常量全大写并以 `_` 分隔；网络接口以 `Api` 结尾，拦截器以 `Interceptor` 结尾，持久化类放入 `storage` 或 `datastore` 包。

## 测试指南
- **框架**：单元测试使用 JUnit4，仪器化测试使用 Espresso。
- **覆盖率**：核心逻辑建议 ≥70% 覆盖率。
- **命名约定**：单元测试类以 `*Test` 结尾并 mirror 目标包结构，仪器化测试以 `*InstrumentedTest` 结尾。
- **执行建议**：先针对改动模块运行 `./gradlew :module:test`，再执行全量测试。

## 提交与拉取请求规范
- **提交信息**：采用 `type(scope): summary` 形式，例如 `feat(core-network): add oauth interceptor`，每次提交聚焦单一变更。
- **拉取请求**：描述需包含背景、解决方案要点、风险评估与验证步骤；涉及 UI 改动需附截图或录屏，并勾选已执行的命令（如 `./gradlew lint`, `./gradlew test`）。
- **审查要求**：合并前确保 CI 通过、评论解决，并链接相关 Issue/任务。

## 安全与配置提示
- **配置同步**：修改 `gradle.properties` 中的 SDK、命名空间或 `enableViewBinding` 设置时需同步团队。
- **签名与密钥**：仓库附带的 `app/release.jks` 仅供示例，真实证书应在本地安全存储；网络敏感配置通过 `NetworkConfig` 注入，请勿硬编码密钥。

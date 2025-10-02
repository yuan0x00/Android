# Repository Guidelines

- **模块分层**
  - `app`：应用壳工程，持有入口 `MainApplication` 与全部界面 `feature/*`；仅负责组装、导航与依赖注入。
  - `core/*`：平台能力层，当前包含 `core:common`（日志、会话工具）、`core:network`、`core:datastore`、`core:ui`、`core:webview`。每个模块内保持 `data/`, `di/`, `ui/` 等子目录简单明晰，资源命名统一使用前缀（如 `core_ui_*`）。
  - `data/repository`：对外暴露 Repository、数据源实现，依赖 `core/*` 并聚合远程/本地实现。
  - `domain/model`：只包含领域模型与接口定义，供上层依赖；不持有 Android 依赖。
  - `build-logic`：Gradle convention 插件；模块公共配置由此注入其它子模块。
- 临时输出存放于各模块 `build/`，IDE 配置在 `.idea/`，统一忽略不入库。

## 构建、测试与开发命令
- `./gradlew assembleDebug`：编译所有模块并产出调试 APK。
- `./gradlew lint`：运行 Android Lint，提交前确保无新警告。
- `./gradlew test`：触发所有 JVM 单元测试；可针对模块使用 `:module:test` 精确执行。
- `./gradlew :app:connectedDebugAndroidTest`：在已连接设备或模拟器上执行仪器化测试。
- `./gradlew clean`：清除构建产物，解决异常编译状态时使用。

## 编码风格与命名规范
- 仅使用 Java 语言开发，禁止引入 Kotlin 源码或 Jetpack Compose 相关依赖；发现 Kotlin 文件需在评审前移除。
- 统一使用 Java 11，源代码缩进 4 空格，括号与方法声明同列对齐。
- 类、枚举采用 UpperCamelCase，方法与变量采用 lowerCamelCase；常量使用全大写并以 `_` 分隔。
- 网络层保持 Retrofit 接口文件以 `Api` 结尾，拦截器以 `Interceptor` 结尾，数据持久化类放置在对应模块的 `storage` 或 `datastore` 包内。
- 新增公共能力优先放入 `core/*`，业务数据访问放入 `data/repository`，领域协议放入 `domain/model`，避免在 `app` 堆积实现代码。
- 提交前运行 `./gradlew lint` 并使用 Android Studio/IDE 自动格式化保持风格统一。

## 测试指南
- 单元测试使用 JUnit4（`libs.junit`），建议核心逻辑覆盖率保持在 70% 以上。
- 仪器化测试使用 Espresso（`libs.androidx-espresso-core`），命名以 `*InstrumentedTest` 结尾并放于 `module/src/androidTest/java`。
- 单元测试类放在对应模块 `src/test/java`，文件命名以 `*Test` 结尾，测试目标类与包结构保持一致。
- 数据层依赖网络或存储时，使用假实现或本地模拟服务器，避免对真实端点造成影响。
- 在 PR 中附上关键测试指令与通过截图或文本输出，便于复核。

## 提交与拉取请求要求
- 当前历史以 "update" 为主，请改用 `type(scope): summary` 形式，示例：`feat(core-network): add oauth interceptor`。
- 每个提交聚焦单一变更，包含必要的本地化或资源更新，并同步更新相关文档。
- 创建 PR 时，请填写变更背景、解决方案要点、风险评估与验证步骤，若涉及 UI 改动附上截图或录屏。
- 在 PR 描述中链接相关 Issue/任务，标记影响模块，并勾选已执行的命令（如 `./gradlew lint`, `./gradlew test`）。
- 合并前确保 CI 通过且无未解决的 review 评论，必要时请求对应模块负责人审批。

## 配置与安全提示
- `gradle.properties` 存放 SDK 版本、命名空间及 `enableViewBinding`，修改后需同步给团队成员。
- `app/release.jks` 为示例签名文件，勿用于生产；真实证书请在本地安全存储，不要提交仓库。
- 网络配置通过 `NetworkConfig` 构建，敏感地址与密钥请改为从安全存储或 CI 注入。
- 调试模式启用 StrictMode 与日志，请勿在生产变更中关闭相关保护措施。

# FakeMap 2.0.0 发布包

- `FakeMap-release-2.0.0-legacy.apk`：适用于原版 Xposed（仅 Android 6）及支持 legacy API 的 LSPosed、EdXposed、Vector 等框架。
- `FakeMap-release-2.0.0-modern.apk`：适用于支持 libxposed API 102 的现代 root/system 框架，最低 Android 8.1。

两个 APK 都不包含 native 库，是兼容 arm64 与 arm32 的通用纯 Java 产物。普通系统模拟位置模式使用任一构建均可；同一设备只需安装其中一个。

安装、框架选择、地图配置和故障排查请阅读 `../fakemap/docs/使用帮助.md` 与 `../fakemap/docs/兼容性矩阵.md`。

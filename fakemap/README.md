# FakeMap

FakeMap 是一个用于 Android 开发、自动化测试和隐私测试的位置模拟工具。项目提供两条相互独立的工作路径：

- Android 系统标准模拟位置：使用公开的 `LocationManager` Test Provider，不需要 Root 或 Xposed。
- 按应用 Xposed 替换：只在框架作用域和应用内白名单同时命中的第三方应用中，替换公开位置回调边界的 `Location` 数据。

项目为纯 Java Android 应用，最低支持 Android 6（API 23），编译和目标版本为 Android 17（API 37）。不使用 Kotlin、AndroidX、MFC 或本地 SO。

> 本工具不会隐藏 Android 的 Mock 标记，不伪造 GNSS 原始测量，不修改 Wi-Fi、基站、SIM 或系统完整性状态，也不绕过反滥用检测。请只在自己拥有或获准测试的设备、账号和应用中使用。

## 主要能力

- Android 6～17 的系统标准模拟位置。
- `legacy` 与 `modern` 两种 Xposed 构建，避免把互不兼容的入口混在同一个 APK 中。
- legacy Xposed API 89 入口；Android 6 可使用原版 Xposed，较新系统使用支持 New XSharedPreferences 的 EdXposed、LSPosed/Vector legacy 接口或兼容变种。
- modern libxposed API 102 入口，使用框架远程偏好服务同步配置。
- 明确的目标包名白名单和系统包保护规则，任何配置或 IPC 异常都失败关闭。
- 国内高德 JavaScript API 与国际 Leaflet 栅格地图双方案；自动模式按目标坐标选择，而不是按 SIM 或系统语言猜测。
- 内部统一保存 WGS-84；只在高德显示边界进行 GCJ-02 转换。
- 高德仅用于中国大陆目标点；无效凭据、网络故障或超时后自动回退国际地图。
- 地图版权区不被界面遮挡；自定义栅格服务必须同时配置署名文字和许可链接。
- 不含统计、崩溃上报、广告 SDK、硬编码地图 Key 或后台定位采集。

## APK 选择

| 文件 | 最低系统 | 用途 |
| --- | ---: | --- |
| `FakeMap-release-2.0.0-legacy.apk` | Android 6 / API 23 | 系统标准模拟；legacy Xposed 框架 |
| `FakeMap-release-2.0.0-modern.apk` | Android 8.1 / API 27 | 系统标准模拟；libxposed API 101/102 框架 |

应用没有本地库，因此 APK 不携带 ABI，等价支持 arm64-v8a 与 armeabi-v7a，不需要生成没有内容差异的 ABI 分包。

## 快速使用

1. 安装与框架匹配的 APK。
2. 输入 WGS-84 坐标，或点击“在地图上选点”。
3. 使用系统标准模式时，在开发者选项的“选择模拟位置信息应用”中选择 FakeMap，然后返回应用点击“开始模拟”。
4. 使用 Xposed 模式时，把 FakeMap 和目标应用都加入框架作用域；在 FakeMap 中填写相同的目标包名、启用位置替换并保存，然后强行停止并重开目标应用。
5. 完成测试后点击“停止模拟”。

完整步骤见[使用帮助](docs/使用帮助.md)，版本和框架边界见[兼容性矩阵](docs/兼容性矩阵.md)。

## 构建

需要 JDK 17、Android SDK Platform 37 和 Build Tools 37。项目使用 AGP 9.3、Gradle 9.5.1，依赖仓库优先使用国内镜像。

```powershell
.\gradlew.bat clean test lintLegacyRelease lintModernRelease assembleLegacyRelease assembleModernRelease
```

构建完成后，发布产物会自动复制到项目根目录的 `release`：

- `release/FakeMap-release-2.0.0-legacy.apk`
- `release/FakeMap-release-2.0.0-modern.apk`

详细环境、签名和验证方式见[构建与测试](docs/构建与测试.md)。仓库内签名只用于测试，不得用于正式发布。

## 设计资料

- [技术方案](docs/技术方案.md)
- [使用帮助](docs/使用帮助.md)
- [兼容性矩阵](docs/兼容性矩阵.md)
- [构建与测试](docs/构建与测试.md)
- [第三方组件与服务声明](THIRD_PARTY_NOTICES.md)

## 隐私与安全

- 地图 Key、安全密钥和瓦片配置只保存在应用私有偏好中，不进入 Xposed 跨进程配置。
- Xposed 共享配置只包含启用状态、目标包名、坐标、精度、海拔、速度和方向。
- 禁止系统包、Google Play 服务和 FakeMap 自身成为替换目标。
- Android 备份与设备迁移规则显式排除全部应用数据域。
- WebView 禁止文件访问、内容访问、地理定位、明文混合内容和证书错误继续加载。
- 项目未申请 `QUERY_ALL_PACKAGES`，也不会扫描已安装应用。

## 上游资料

- [Android 17 SDK 配置](https://developer.android.com/about/versions/17/setup-sdk)
- [Android LocationManager](https://developer.android.com/reference/android/location/LocationManager)
- [libxposed API](https://github.com/libxposed/api)
- [Leaflet](https://leafletjs.com/)
- [OpenStreetMap 瓦片使用策略](https://operations.osmfoundation.org/policies/tiles/)
- [高德地图 JavaScript API 2.0](https://lbs.amap.com/api/javascript-api-v2/summary)

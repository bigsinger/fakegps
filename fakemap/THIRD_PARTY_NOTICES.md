# 第三方组件与服务声明

## Leaflet 1.9.4

项目将 Leaflet 1.9.4 的 JavaScript、CSS 和图片资源打包到 APK，用于国际栅格地图显示。

- 项目主页：[Leaflet](https://leafletjs.com/)
- 源代码：[Leaflet GitHub](https://github.com/Leaflet/Leaflet)
- 许可证：BSD 2-Clause
- 随包许可证：`app/src/main/assets/leaflet/LICENSE`

## OpenStreetMap

项目不打包 OpenStreetMap 数据。默认配置会在用户打开地图时请求 OpenStreetMap 公共标准瓦片，页面显示并链接 `© OpenStreetMap contributors`。

- [版权与许可](https://www.openstreetmap.org/copyright)
- [瓦片使用策略](https://operations.osmfoundation.org/policies/tiles/)

公共瓦片服务没有 SLA，只用于轻量开发测试。生产部署者必须自行配置有授权的服务，并填写正确的署名文字与许可链接。

## 高德地图 JavaScript API 2.0

项目不打包高德 SDK，也不提供或提交高德 Key。用户选择高德 Provider 后，WebView 会从高德官方 HTTPS 地址加载 JavaScript API，使用用户在本机配置的 Web Key 与 JS 安全密钥。

- [高德地图 JavaScript API](https://lbs.amap.com/api/javascript-api-v2/summary)
- [高德服务条款](https://lbs.amap.com/pages/terms/)

使用者负责申请合法凭据、配置安全限制、购买所需权限并遵守配额、Logo、版权和审图要求。

## Natural Earth

项目使用 Natural Earth 1:10m Admin-0 数据离线生成中国大陆及其离散岛屿的工程分类掩码，来源快照为上游提交 `ca96624a56bd078437bca8184e78163e5039ad19`；香港、澳门和台湾使用独立要素作排除。运行时不联网请求该数据。

- [Natural Earth 数据下载](https://www.naturalearthdata.com/downloads/10m-cultural-vectors/)
- [Natural Earth 使用条款](https://www.naturalearthdata.com/about/terms-of-use/)
- 授权：公有领域

## Xposed API 89

legacy flavor 仅在编译时引用 Xposed API 89，APK 不把 XposedBridge 实现打包进运行产物。运行时实现由用户安装的 Xposed 兼容框架提供。

## libxposed API 与 service 102

modern flavor 编译时引用 libxposed API 102，并打包官方 service 客户端接口以连接框架远程偏好服务。

- [libxposed API](https://github.com/libxposed/api)
- [libxposed service](https://github.com/libxposed/service)
- 许可证：Apache License 2.0

运行时 Hook 能力和远程存储由兼容框架提供。

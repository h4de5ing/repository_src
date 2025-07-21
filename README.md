# 基础框架源代码仓库

## 项目概述
这是一个Android基础框架库集合，包含各种常用功能的封装，具有业务无关性和可复用性。

## 核心模块

### Base 基础公共库
- 常用代码封装,UI无关

### BaseUI UI相关的基础公共库
- 与UI相关的公共库

### NetLib 网络请求相关
- WSClient 基于`org.java-websocket:Java-WebSocket:1.5.3` 的WebSocket封装
- OkhttpUtil 基于`com.squareup.okhttp3:okhttp:4.10.0` 的HTTP请求封装
- HttpRequest 基于Java原生的网络请求库

### filepicker
- 文件选择器相关功能封装

### libcommon / libuvccamera / usbCameraCommon
- USB摄像头相关功能封装

### OTALibrary
- OTA(空中升级)相关功能

### SerialPortLib / SerialPortLibrary
- 串口通信相关功能封装

### zxing
- 二维码扫描相关功能封装

### libcommon
- 其他公共功能封装

## 构建和发布方式

```bash
Terminal -> gradlew publish
```

## 参考和依赖

[usbSerialForAndroid_573c7e4](https://github.com/mik3y/usb-serial-for-android) 
[usbserial_7ad6c9f](https://github.com/felHR85/UsbSerial)

参考
https://github.com/kymjs/Common/blob/master/Common/common/src/main/java/com/kymjs/kotlin/common/extension/ExtensionDensity.kt

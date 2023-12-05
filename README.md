# 基础框架源代码仓库
- 业务无关性
- 重复代码块
- 常用业务模块

# 生成aar方式并发布

```
Terminal -> gradlew publish
```


# Base 基础公共库
- 常用代码封装,UI无关

# BaseUI UI相关的基础公共库
- 与UI相关的公共库

# GsonCommon
- gson 库的基础封装 null判断 json解析 生成json字符串

# NetLib 网络请求相关
- WSClient 基于`org.java-websocket:Java-WebSocket:1.5.3` 的WebSocket封装
- OkhttpUtil 基于`com.squareup.okhttp3:okhttp:4.10.0` 的HTTP请求封装
- HttpRequest 基于Java原生的网络请求库


[usbSerialForAndroid_573c7e4](https://github.com/mik3y/usb-serial-for-android) 
[usbserial_7ad6c9f](https://github.com/felHR85/UsbSerial)

参考
https://github.com/kymjs/Common/blob/master/Common/common/src/main/java/com/kymjs/kotlin/common/extension/ExtensionDensity.kt
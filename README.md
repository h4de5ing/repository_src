# 基础框架源代码仓库
- 业务无关性
- 重复代码块
- 常用业务模块

# 生成aar方式并发布

```
方法一:gradle -> Base -> Tasks -> upload -> uploadArchives
方法二：Terminal -> gradlew uploadArchive
```


# Base 基础公共库
- 常用代码封装,UI无关

# BaseUI UI相关的基础公共库
- 与UI相关的公共库

# GsonCommon
- gson 库的基础封装 null判断 json解析 生成json字符串

# WSClientLib WebSocket 封装
- WSClient 基于`org.java-websocket:Java-WebSocket:1.5.1` 封装
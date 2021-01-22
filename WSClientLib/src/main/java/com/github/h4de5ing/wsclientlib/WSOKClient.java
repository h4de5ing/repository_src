//package com.github.h4de5ing.wsclientlib;
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.concurrent.TimeUnit;
//
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import okhttp3.WebSocket;
//import okhttp3.WebSocketListener;
//import okio.ByteString;
//
///**
// * 基于OkHTTP 的websocket
// */
//public class WSOKClient {
//    private static OkHttpClient okHttpClient;
//    private static WebSocket webSocket;
//    private static WSOKClient wsokClient;
//    private static boolean isConnected = false;
//    private static long counter = 0;
//    private static boolean mIsRetry = false;
//
//    public static WSOKClient getInstance() {
//        if (wsokClient == null) {
//            wsokClient = new WSOKClient();
//        }
//        return wsokClient;
//    }
//
//    private WSMessageUpdateListener wsMessageUpdateListener;
//
//    public void setWSMessageListener(WSMessageUpdateListener listener) {
//        wsMessageUpdateListener = listener;
//    }
//
//    private void connect(boolean isRetry, String url) {
//        this.mIsRetry = isRetry;
//        if (okHttpClient != null) {
//            okHttpClient.dispatcher();
//            okHttpClient = null;
//        }
//        okHttpClient = new OkHttpClient.Builder()
//                .writeTimeout(5, TimeUnit.SECONDS)
//                .readTimeout(5, TimeUnit.SECONDS)
//                .connectTimeout(5, TimeUnit.SECONDS)
//                .retryOnConnectionFailure(true)
//                .build();
//        if (webSocket != null) {
//            webSocket.close(1001, "reset");
//            webSocket.cancel();
//            webSocket = null;
//        }
//        webSocket = okHttpClient.newWebSocket(new Request.Builder().url(url).build(), new WebSocketListener() {
//            @Override
//            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
//                super.onOpen(webSocket, response);
//                isConnected = true;
//                println("WebSocket 打开成功");
//                counter = 0;
//            }
//
//            @Override
//            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
//                super.onClosed(webSocket, code, reason);
//                isConnected = false;
//                println("WebSocket 已经关闭");
//            }
//
//            @Override
//            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
//                super.onClosing(webSocket, code, reason);
//                isConnected = false;
//                println("WebSocket 已经关闭");
//            }
//
//            @Override
//            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
//                super.onFailure(webSocket, t, response);
//                isConnected = false;
//                println("WebSocket 打开失败" + t.getMessage());
//            }
//
//            @Override
//            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
//                super.onMessage(webSocket, text);
//                isConnected = true;
//                println("接收到消息:" + text);
//                counter = 0;
//                if (wsMessageUpdateListener != null) wsMessageUpdateListener.update(text);
//            }
//
//            @Override
//            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
//                super.onMessage(webSocket, bytes);
//            }
//        });
//    }
//
//    public void sendMessage(String message) {
//        try {
//            webSocket.send(message);
//            System.out.println("发送消息" + message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void println(String message) {
//        System.out.println(message);
//    }
//}

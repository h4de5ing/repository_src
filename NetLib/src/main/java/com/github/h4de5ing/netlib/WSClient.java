package com.github.h4de5ing.netlib;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class WSClient {
    private boolean isConnected = false;
    private static WSClient wsClient;
    private String mUrl = "";
    private WebSocketClient webSocketClient;
    private long counter = 0;

    public static WSClient getInstance() {
        if (wsClient == null) wsClient = new WSClient();
        return wsClient;
    }


    public void retry(String url) {
        try {
            mUrl = url;
            if (webSocketClient != null) {
                System.out.println("重试");
                webSocketClient.close();
                webSocketClient = null;
            }
            connect(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void close() {
        try {
            if (null != webSocketClient) {
                println("关闭WebSocket终端");
                webSocketClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            webSocketClient = null;
        }
    }

    private WSMessageUpdateListener wsMessageUpdateListener;
    private WSStatusUpdateListener wsStatusUpdateListener;

    public void setWSMessageListener(WSMessageUpdateListener listener) {
        wsMessageUpdateListener = listener;
    }

    public void setWsStatusUpdateListener(WSStatusUpdateListener listener) {
        wsStatusUpdateListener = listener;
    }

    private void updateStatus() {
        if (wsStatusUpdateListener != null) {
            wsStatusUpdateListener.update(isConnected);
        }
    }

    private void connect(String url) {
        try {
            System.out.println("初始化" + url);
            webSocketClient = new WebSocketClient(URI.create(url)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    isConnected = true;
                    println("WebSocket 打开成功");
                    counter = 0;
                    updateStatus();
                }

                @Override
                public void onMessage(String message) {
                    isConnected = true;
                    println("接收到消息1:" + message);
                    counter = 0;
                    updateStatus();
                    if (wsMessageUpdateListener != null) wsMessageUpdateListener.update(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    println("WebSocket 已经关闭");
                    updateStatus();
                }

                @Override
                public void onError(Exception ex) {
                    isConnected = false;
                    updateStatus();
                    println("WebSocket 打开失败" + ex.getMessage());
                }
            };
            webSocketClient.connectBlocking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delay(String message) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendMessage(message);
            }
        }, 2 * 1000);
    }

    public void sendMessage(String message) {
        try {
            if (webSocketClient != null) {
                System.out.println("sendMessage:" + message);
                webSocketClient.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void println(String message) {
        System.out.println(message);
    }
}

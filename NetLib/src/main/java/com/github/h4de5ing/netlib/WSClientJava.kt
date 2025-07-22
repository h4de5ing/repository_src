package com.github.h4de5ing.netlib

import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.Timer
import java.util.TimerTask

class WSClientJava(
    val url: String,
    val delay: Long = 10000L,
    val onOpen: () -> Unit = {},
    val onClose2: (code: Int, reason: String) -> Unit = { _, _ -> },
    val onError2: (Throwable) -> Unit = {},
    val onPing: () -> Unit = {},
    val onPong: () -> Unit = {},
    val onMessage2: ((String) -> Unit),
) : WSClient {
    private var client: WebSocketClient? = null
    private var isConnect = false
    private var delayReconnect = delay

    init {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (!isConnect) connect()
            }
        }, 0, delayReconnect)
    }

    override fun isOpen(): Boolean = isConnect && client?.isOpen == true
    override fun send(text: String) {
        client?.send(text)
    }

    override fun send(bytes: ByteArray) {
        client?.send(bytes)
    }

    override fun setReconnectDelay(delayByUser: Long) {
        delayReconnect = delayByUser
    }

    override fun connect() {
        try {
            client = object : WebSocketClient(URI(url)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    isConnect = true
                    onOpen()
                }

                override fun onMessage(message: String) {
                    onMessage2(message)
                    isConnect = true
                }

                override fun onClose(code: Int, reason: String, remote: Boolean) {
                    onClose2(code, reason)
                    isConnect = false
                }

                override fun onError(ex: Exception) {
                    isConnect = false
                    onError2(ex)
                }

                override fun onWebsocketPing(conn: WebSocket, f: Framedata) {
                    super.onWebsocketPing(conn, f)
                    onPing()
                }

                override fun onWebsocketPong(conn: WebSocket, f: Framedata) {
                    super.onWebsocketPong(conn, f)
                    onPong()
                }
            }
            client?.connect()
        } catch (e: Exception) {
            isConnect = false
            e.printStackTrace()
        }
    }

    override fun disconnect() {
        try {
            client?.closeConnection(1000, "连接已正常关闭 WSClientJava")
            client = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
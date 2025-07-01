package com.github.h4de5ing.netlib

import android.os.Handler
import android.os.Looper
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WSClient(
    val ws: String,
    val reconnect: Boolean = false,
    val delay: Long = 10000,
    val onOpen: () -> Unit = {},
    val onClose2: (code: Int, reason: String?, remote: Boolean) -> Unit = { _, _, _ -> },
    val onError: () -> Unit = {},
    val onPing: () -> Unit = {},
    val onPong: () -> Unit = {},
    val onMessage2: ((String) -> Unit),
) {
    private var client: WebSocketClient? = null
    private var activeDisconnect = false
    private var delayReconnect = 0L

    init {
        create()
        delayReconnect = delay
    }

    fun isOpen(): Boolean = client?.isOpen == true
    fun send(text: String) = client?.send(text)
    fun send(bytes: ByteArray) = client?.send(bytes)
    fun setReconnectDelay(delayByUser: Long) = client?.apply { delayReconnect = delayByUser }

    private fun create() {
        try {
            client = object : WebSocketClient(URI(ws)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    onOpen()
                }

                override fun onMessage(message: String?) {
                    message?.apply { onMessage2(this) }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    onClose2(code, reason, remote)
                    reConnect()
                }


                override fun onWebsocketPing(conn: WebSocket?, f: Framedata?) {
                    super.onWebsocketPing(conn, f)
                    onPing()
                }

                override fun onWebsocketPong(conn: WebSocket?, f: Framedata?) {
                    super.onWebsocketPong(conn, f)
                    onPong()
                }

                override fun onError(ex: Exception?) {
                    onError()
                }
            }
            client?.connect()
        } catch (e: Exception) {
            reConnect()
            e.printStackTrace()
        }
    }

    private fun reConnect() {
        if (!activeDisconnect && reconnect) Handler(Looper.getMainLooper()).postDelayed(
            { client?.reconnect() },
            delayReconnect
        )
        activeDisconnect = false
    }

    fun closeWebSocket() {
        try {
            activeDisconnect = true
            client?.apply { if (isOpen) closeConnection(1000, "Goodbye!") }
            client = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
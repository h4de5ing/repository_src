package com.github.h4de5ing.netlib

import android.os.Handler
import android.os.Looper
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WSClientJava(
    val url: String,
    val reconnect: Boolean = false,
    val delay: Long = 10000L,
    val onOpen: () -> Unit = {},
    val onClose2: (code: Int, reason: String) -> Unit = { _, _ -> },
    val onError2: (Throwable) -> Unit = {},
    val onPing: () -> Unit = {},
    val onPong: () -> Unit = {},
    val onMessage2: ((String) -> Unit),
) : WSClient {
    private var client: WebSocketClient? = null
    private var activeDisconnect = reconnect
    private var delayReconnect = delay

    init {
        connect()
    }

    override fun isOpen(): Boolean = client?.isOpen == true
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
                    onOpen()
                }

                override fun onMessage(message: String) {
                    onMessage2(message)
                }

                override fun onClose(code: Int, reason: String, remote: Boolean) {
                    onClose2(code, reason)
                    reConnect()
                }


                override fun onWebsocketPing(conn: WebSocket, f: Framedata) {
                    super.onWebsocketPing(conn, f)
                    onPing()
                }

                override fun onWebsocketPong(conn: WebSocket, f: Framedata) {
                    super.onWebsocketPong(conn, f)
                    onPong()
                }

                override fun onError(ex: Exception) {
                    onError2(ex)
                    reConnect()
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
            { connect() },
            delayReconnect
        )
        activeDisconnect = false
    }

    override fun disconnect() {
        try {
            activeDisconnect = true
            client?.closeConnection(1000, "Goodbye!")
            client = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
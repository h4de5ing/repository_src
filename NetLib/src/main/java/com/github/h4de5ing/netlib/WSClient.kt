package com.github.h4de5ing.netlib

import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class WSClient(
    val ws: String,
    val onOpen: () -> Unit = {},
    val onClose: (code: Int, reason: String?, remote: Boolean) -> Unit = {_,_,_->},
    val onError: () -> Unit = {},
    val onPing: () -> Unit = {},
    val onPong: () -> Unit = {},
    val onMessage2: ((String) -> Unit),
) {
    private var client: WebSocketClient? = null
    private var tryReconnect: Boolean = false

    init {
        create()
        circlePing()
    }

    fun isOpen(): Boolean = client?.isOpen ?: false
    fun send(text: String) = client?.apply { send(text) }

    private fun create() {
        try {
            client = object : WebSocketClient(URI(ws)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    onOpen()
                    tryReconnect = false
                }

                override fun onMessage(message: String?) {
                    tryReconnect = false
                    message?.apply { onMessage2(this) }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    onClose(code,reason,remote)
                    tryReconnect = true
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
                    tryReconnect = true
                    onError()
                }
            }
            client?.connectBlocking(10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            tryReconnect = true
            e.printStackTrace()
        }
    }

    private fun needReconnect() {
        try {
            if (tryReconnect) {
                client?.apply {
                    if (isOpen) closeConnection(2, "reconnect stop")
                }
                client = null
                create()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun circlePing() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                //client?.apply { if (isOpen) sendPing() }
                if (tryReconnect) needReconnect()
            }
        }, 0, 5000)
    }
}
package com.github.h4de5ing.netlib

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import java.util.concurrent.TimeUnit

class WSClientOK2(
    val url: String,
    val delay: Long = 10000L,
    val onOpen: () -> Unit = {},
    val onClose2: (code: Int, reason: String) -> Unit = { _, _ -> },
    val onError2: (Throwable) -> Unit = { },
    val onPing: () -> Unit = {},
    val onPong: () -> Unit = {},
    val onMessage2: (String) -> Unit = {}
) : WSA() {
    private var webSocket: WebSocket? = null
    override fun connect() {
        val okHttp = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build()
        val request = Request.Builder().url(url).build()
        webSocket = okHttp.newWebSocket(request, InnerListener())
    }

    override fun disconnect() {
        webSocket?.close(1000, "连接已正常关闭 WSClientOK2")
        webSocket = null
    }

    override fun send(text: String) {
        webSocket?.send(text) ?: Log.w("gh0st", "send: socket==null")
    }

    override fun send(bytes: ByteArray) {
        webSocket?.send(bytes.toByteString()) ?: Log.w("gh0st", "send: socket==null")
    }

    private inner class InnerListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            onOpen()
            isConnect = true
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            onMessage2(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            isConnect = false
            Log.e("gh0st", "onClosing: ${code},${reason}")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            isConnect = false
            onClose2(code, reason)
            Log.e("gh0st", "onClosing: ${code},${reason}")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("gh0st", "onFailure: ${t.message}")
            isConnect = false
            onError2(t)
        }
    }
}
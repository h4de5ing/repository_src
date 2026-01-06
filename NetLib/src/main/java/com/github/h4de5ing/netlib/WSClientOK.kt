package com.github.h4de5ing.netlib

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import java.util.concurrent.TimeUnit

class WSClientOK(
    val url: String,
    val delay: Long = 10000L,
    val onOpen: () -> Unit = {},
    val onClose2: (String) -> Unit = { },
    val onError2: (String) -> Unit = { },
    val onPing: () -> Unit = {},
    val onPong: () -> Unit = {},
    val onMessage2: (String) -> Unit = {}
) : WSA() {
    private var webSocket: WebSocket? = null

    override fun connect() {
        try {
            val request = Request.Builder().url(url).build()
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    isConnect = true
                    onOpen()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    onMessage2(text)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    isConnect = false
                    onClose2("${code},${reason}")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    isConnect = false
                    onError2("onFailure:${t.message},${response?.code ?: ""},${response?.message ?: ""}")
                }
            }

            webSocket = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .build()
                .newWebSocket(request, listener)
        } catch (e: Exception) {
            onError2("Exception:${e.message}")
            isConnect = false
            e.printStackTrace()
        }
    }

    override fun send(text: String) {
        webSocket?.send(text) ?: Log.w("gh0st", "send: socket==null")
    }

    override fun send(bytes: ByteArray) {
        webSocket?.send(bytes.toByteString()) ?: Log.w("gh0st", "send: socket==null")
    }

    override fun disconnect() {
        isConnect = false
        webSocket?.close(1000, "连接已正常关闭 WSClientOK")
        webSocket = null
    }
}
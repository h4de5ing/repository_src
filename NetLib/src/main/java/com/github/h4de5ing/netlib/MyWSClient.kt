package com.github.h4de5ing.netlib

import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MyWSClient(
    val url: String,
    val reconnect: Boolean = false,
    val delay: Long = 10000,
    val onOpenCallback: () -> Unit = {},
    val onFailureCallback: (Throwable, Response?) -> Unit = { _, _ -> },
    val onClosingCallback: (Int, String) -> Unit = { _, _ -> },
    val onClosedCallback: () -> Unit = {},
    val onMessageCallback: (String) -> Unit = {}
) {
    private var webSocket: WebSocket? = null
    private var open = false
    private var activeDisconnect = false
    fun isOpen(): Boolean = open
    fun connectWebSocket() {
        try {
            val request = Request.Builder().url(url).build()
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    open = true
                    onOpenCallback()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    open = false
                    if (reconnect) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            connectWebSocket()
                        }, delay)
                    }
                    onFailureCallback(t, response)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    onMessageCallback(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    onClosingCallback(code, reason)
                    open = false
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    open = false
                    if (reconnect && !activeDisconnect) Handler(Looper.getMainLooper()).postDelayed(
                        { connectWebSocket() },
                        delay
                    )
                    activeDisconnect = false
                    onClosedCallback()
                }
            }
            webSocket = OkHttpClient().newWebSocket(request, listener)
        } catch (_: Exception) {
            open = false
        }
    }

    fun write(data: String) = webSocket?.send(data)

    fun closeWebSocket() {
        activeDisconnect = true
        webSocket?.close(1000, "Goodbye!")
        webSocket = null
    }
}
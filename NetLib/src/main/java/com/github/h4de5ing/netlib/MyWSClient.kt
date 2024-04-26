package com.github.h4de5ing.netlib

import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket

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
    var webSocket: WebSocket? = null
    var open = false
    private var activeDisconnect = false

    fun isOpen(): Boolean {
        return open
    }

    fun connectWebSocket() {
        try {
            val request = Request.Builder().url(url).build()

            val listener = MyWebSocketListener(
                onOpenCallback1 = {
                    open = true
                    onOpenCallback()
                },
                onFailureCallback1 = { t, r ->
                    open = false
                    if (reconnect) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            connectWebSocket()
                        }, delay)
                    }
                    onFailureCallback(t, r)

                },
                onMessageCallback,
                onClosingCallback1 = { i, s ->
                    onClosingCallback(i, s)
                    open = false
                },
                onClosedCallback1 = {
                    open = false
                    if (reconnect && !activeDisconnect) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            connectWebSocket()
                        }, delay)
                    }
                    activeDisconnect = false
                    onClosedCallback()
                }
            )

            webSocket = OkHttpClient().newWebSocket(request, listener)
        } catch (_: Exception) {
            open = false
        }
    }

    fun closeWebSocket() {
        activeDisconnect = true
        webSocket?.close(1000, "Goodbye!")
        webSocket = null
    }
}
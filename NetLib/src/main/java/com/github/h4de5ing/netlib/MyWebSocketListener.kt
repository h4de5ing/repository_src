package com.github.h4de5ing.netlib

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MyWebSocketListener(
    var onOpenCallback1: () -> Unit = {},
    var onFailureCallback1: (Throwable, Response?) -> Unit = { _, _ -> },
    var onMessageCallback: (String) -> Unit = {},
    var onClosingCallback1: (Int, String) -> Unit = { _, _ -> },
    var onClosedCallback1: () -> Unit = {}
) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        onOpenCallback1()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        onFailureCallback1(t, response)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        onMessageCallback(text)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        onClosingCallback1(code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        onClosedCallback1()
    }
}
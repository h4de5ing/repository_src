package com.github.h4de5ing.netlib

import android.content.Context
import com.github.h4de5ing.netlib.wsmanager.WsManager
import com.github.h4de5ing.netlib.wsmanager.WsStatusListener
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

var changeBoolean: ChangeBoolean? = null
var booleanChange: Boolean by Delegates.observable(false) { _, _, new ->
    changeBoolean?.change(new)
}

interface ChangeBoolean {
    fun change(message: Boolean)
}

private fun setBooleanChangeListener(change: ChangeBoolean) {
    changeBoolean = change
}

fun setOnChangeBoolean(block: ((Boolean) -> Unit)) {
    setBooleanChangeListener(object : ChangeBoolean {
        override fun change(message: Boolean) {
            block(message)
        }
    })
}

var wsManager: WsManager? = null
fun send2WS(message: String) {
    wsManager?.apply { this.sendMessage(message) }
}

fun ws(context: Context, url: String, block: ((String) -> Unit)) {
    wsManager = WsManager.Builder(context).client(
        OkHttpClient().newBuilder()
            .pingInterval(0, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    ).wsUrl(url).build()
    wsManager?.setWsStatusListener(object : WsStatusListener() {
        override fun onOpen(response: Response?) {
            super.onOpen(response)
            booleanChange = true
            println("ws onOpen 连接成功")
        }

        override fun onMessage(text: String?) {
            super.onMessage(text)
            booleanChange = true
            block("$text")
            println("接收到消息1:$text")
        }

        override fun onReconnect() {
            super.onReconnect()
            booleanChange = false
            println("ws onReconnect...")
        }

        override fun onClosing(code: Int, reason: String?) {
            super.onClosing(code, reason)
            booleanChange = false
            println("ws onClosing...")
        }

        override fun onClosed(code: Int, reason: String?) {
            super.onClosed(code, reason)
            booleanChange = false
            println("ws onClosed...")
        }
    })
    wsManager?.startConnect()
}
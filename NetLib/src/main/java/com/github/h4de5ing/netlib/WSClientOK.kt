package com.github.h4de5ing.netlib

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.dnsoverhttps.DnsOverHttps
import okio.ByteString.Companion.toByteString
import java.net.InetAddress
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class WSClientOK(
    val url: String,
    val delay: Long = 10000L,
    val onOpen: () -> Unit = {},
    val onClose2: (code: Int, reason: String) -> Unit = { _, _ -> },
    val onError2: (Throwable) -> Unit = { },
    val onMessage2: (String) -> Unit = {}
) : WSClient {
    private var webSocket: WebSocket? = null
    private var isConnect = false
    private var delayReconnect = delay
    override fun isOpen(): Boolean = isConnect

    init {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (!isConnect) connect()
            }
        }, 0, delayReconnect)
    }

    override fun connect() {
        try {
            val request = Request.Builder().url(url).build()
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    isConnect = true
                    onOpen()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    isConnect = false
                    onError2(t)

                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    isConnect = true
                    onMessage2(text)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    isConnect = false
                    onClose2(code, reason)
                }
            }

            webSocket = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
//                .doh()//TODO 增加这个功能
                .build().newWebSocket(request, listener)
        } catch (e: Exception) {
            isConnect = false
            e.printStackTrace()
        }
    }

    override fun send(text: String) {
        webSocket?.send(text)
    }

    override fun send(bytes: ByteArray) {
        webSocket?.send(bytes.toByteString())
    }

    override fun setReconnectDelay(delayByUser: Long) {
        delayReconnect = delayByUser
    }

    override fun disconnect() {
        isConnect = false
        webSocket?.close(1000, "连接已正常关闭 WSClientOK")
        webSocket = null
    }

    fun OkHttpClient.Builder.doh(
        url: String = "https://cloudflare-dns.com/dns-query",
        ips: List<String> = listOf(
            "162.159.36.1",
            "162.159.46.1",
            "1.1.1.1",
            "1.0.0.1",
            "162.159.132.53",
            "2606:4700:4700::1111",
            "2606:4700:4700::1001",
            "2606:4700:4700::0064",
            "2606:4700:4700::6400"
        )
    ) = dns(
        DnsOverHttps
            .Builder()
            .client(build())
            .url(url.toHttpUrl())
            .bootstrapDnsHosts(ips.map { InetAddress.getByName(it) })
            .build()
    )
}
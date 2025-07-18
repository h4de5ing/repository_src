package com.github.h4de5ing.netlib

interface WSClient {
    fun connect()
    fun disconnect()
    fun isOpen(): Boolean
    fun send(text: String)
    fun send(bytes: ByteArray)
    fun setReconnectDelay(delayByUser: Long)
}
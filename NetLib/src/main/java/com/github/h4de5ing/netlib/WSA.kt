package com.github.h4de5ing.netlib

import android.util.Log
import java.util.Timer
import java.util.TimerTask

abstract class WSA : WSClient {
    var isConnect = false
    var delayReconnect = 10_000L

    init {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (!isConnect) {
                    Log.e("gh0st", "retry")
                    connect()
                }
            }
        }, 0, delayReconnect)
    }

    override fun isOpen(): Boolean = isConnect
    override fun setReconnectDelay(delayByUser: Long) {
        delayReconnect = delayByUser
    }
}
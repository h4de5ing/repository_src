package com.github.h4de5ing.netlib

import android.util.Log
import java.util.Timer
import java.util.TimerTask

abstract class WSA(val delay2: Long = 10000L) : WSClient {
    var isConnect = false
    var delayReconnect = delay2
        set(value) {
            field = value
            restartTimer()
        }
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timer = Timer()
        timerTask = createTimerTask().also { timer?.schedule(it, 0, delayReconnect) }
    }

    private fun createTimerTask() = object : TimerTask() {
        override fun run() {
            if (!isConnect) {
                Log.e("gh0st", "retry")
                connect()
            }
        }
    }

    private fun restartTimer() {
        timerTask?.cancel()
        timer?.cancel()
        timer = null
        timerTask = null
        startTimer()
    }

    override fun isOpen(): Boolean = isConnect
    override fun setReconnectDelay(delayByUser: Long) {
        delayReconnect = delayByUser
    }
}
package com.github.h4de5ing.serialportlib

import android_serialport_api.SerialPort
import java.io.File

/**
 * Created by luowei on 2017/9/13.
 */
object SerialPortIO : BaseIO() {
    private var serialPort: SerialPort? = null
    fun start(name: String, baud: Int, callback: (buffer: ByteArray, size: Int) -> Unit) {
        serialPort = SerialPort(File(name), baud, 0)
        super.start(serialPort!!.inputStream, serialPort!!.outputStream, callback)
    }

    override fun stop() {
        super.stop()
        serialPort?.close()
        serialPort = null
    }
}
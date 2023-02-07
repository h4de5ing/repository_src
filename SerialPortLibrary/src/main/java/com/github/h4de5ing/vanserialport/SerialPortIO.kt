package com.github.h4de5ing.vanserialport

import com.giftedcat.serialportlibrary.SerialPortManager
import java.io.File

//https://github.com/Giftedcat/AndroidSerialPortManager
object SerialPortIO {
    private var serialPortManager: SerialPortManager? = null
    fun start(name: String, baud: Int, callback: (buffer: ByteArray, size: Int) -> Unit) {
        SerialPortManager.openLog()
        SerialPortManager(File(name), baud, SerialPortManager.NORMAL)
            .setOnSerialPortDataListener { bytes: ByteArray ->
                callback(bytes, bytes.size)
            }.also { serialPortManager = it }
    }

    fun write(data: ByteArray) = serialPortManager?.sendBytes(data)
    fun stop() {
        serialPortManager?.closeSerialPort()
        serialPortManager = null
    }
}
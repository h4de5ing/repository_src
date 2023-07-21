package com.github.h4de5ing.vanserialport

import android.util.Log
import com.van.uart.LastError
import com.van.uart.UartManager
import com.van.uart.UartManager.BaudRate

object SerialPortIO {
    private var serialPort: UartManager? = null
    private var readThread: ReadThread? = null
    var isSuccess: Boolean = false
    fun start(name: String, baud: Int, callback: (buffer: ByteArray, size: Int) -> Unit) {
        try {
            val devName: String = name.split("/dev/").toTypedArray()[1]
            serialPort = UartManager()
            isSuccess = serialPort?.open(devName, getBaudRate(baud)) ?: false
            readThread = ReadThread(callback)
            readThread?.start()
            Log.i("gh0st", "$name open success [$baud]")
        } catch (e: LastError) {
            isSuccess = false
            val result = "$e".toByteArray()
            callback(result, result.size)
            e.printStackTrace()
        }
    }

    fun write(data: ByteArray): Int? {
        return try {
            serialPort?.write(data, data.size)
        } catch (e: LastError) {
            -1
        }
    }

    private class ReadThread(private val callback: (buffer: ByteArray, size: Int) -> Unit) :
        Thread() {
        private val readBuffer = ByteArray(4096)
        private var readSize = 0
        override fun run() {
            try {
                serialPort?.apply {
                    while (this.isOpen) {
                        try {
                            readSize = this.read(readBuffer, readBuffer.size, 50, 1)
                            callback.invoke(readBuffer, readSize)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        readThread = null
        serialPort?.close()
        serialPort = null
    }

    fun getBaudRate(baudRate: Int): BaudRate {
        return when (baudRate) {
            1200 -> BaudRate.B1200
            2400 -> BaudRate.B2400
            4800 -> BaudRate.B4800
            9600 -> BaudRate.B9600
            19200 -> BaudRate.B19200
            38400 -> BaudRate.B38400
            57600 -> BaudRate.B57600
            115200 -> BaudRate.B115200
            230400 -> BaudRate.B230400
            else -> BaudRate.B115200
        }
    }

    fun getSupportBauds(): Array<Int> =
        arrayOf(1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400)
}
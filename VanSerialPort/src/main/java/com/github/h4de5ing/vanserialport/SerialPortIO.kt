package com.github.h4de5ing.vanserialport

import android.util.Log
import com.van.uart.LastError
import com.van.uart.UartManager
import com.van.uart.UartManager.BaudRate

object SerialPortIO {
    private var serialPort: UartManager? = null
    private var readThread: ReadThread? = null
    fun start(name: String, baud: Int, callback: (buffer: ByteArray, size: Int) -> Unit) {
        try {
            val devName: String = name.split("/dev/").toTypedArray()[1]
            serialPort = UartManager()
            serialPort?.open(devName, getBaudRate(baud))
            readThread = ReadThread(callback)
            readThread?.start()
            Log.i("gh0st", "$name open success [$baud]")
        } catch (e: Exception) {
            val result = "exception:${e.message}".toByteArray()
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
//                            sleep(100)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

//        fun close() {
//            interrupt()
//        }
    }

    fun stop() {
//        readThread?.close()
        readThread = null
        serialPort?.close()
        serialPort = null
    }

    fun getBaudRate(baudrate: Int): BaudRate {
        val value: BaudRate?
        when (baudrate) {
            1200 -> value = BaudRate.B1200
            2400 -> value = BaudRate.B2400
            4800 -> value = BaudRate.B4800
            9600 -> value = BaudRate.B9600
            19200 -> value = BaudRate.B19200
            38400 -> value = BaudRate.B38400
            57600 -> value = BaudRate.B57600
            115200 -> value = BaudRate.B115200
            230400 -> value = BaudRate.B230400
            else -> {
                value = BaudRate.B115200
                Exception("not support baudrate [$baudrate]")
            }
        }
        return value
    }

    fun getSupportBauds(): Array<Int> {
        return arrayOf(1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400)
    }
}
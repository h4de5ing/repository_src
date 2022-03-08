package com.github.h4de5ing.vanserialport

import com.van.uart.UartManager
import com.van.uart.UartManager.BaudRate
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object SerialPortIO {
    private var isRun: Boolean = false
    private val executor = ThreadPoolExecutor(3, 10, 5, TimeUnit.SECONDS, LinkedBlockingQueue())
    private var serialPort: UartManager? = null
    private var readThread: ReadThread? = null
    fun start(name: String, baud: Int, callback: (buffer: ByteArray, size: Int) -> Unit) {
        try {
            val devName: String = name.split("/dev/").toTypedArray()[1]
            serialPort = UartManager()
            serialPort?.open(devName, getBaudRate(baud))
            isRun = true
            readThread = startReadThread(callback)
        } catch (e: Exception) {
            var result = "${e.message}".toByteArray()
            callback(result, result.size)
            e.printStackTrace()
        }
    }

    fun write(data: ByteArray): Int? = serialPort?.write(data, data.size)
    private fun startReadThread(
        callback: (buffer: ByteArray, size: Int) -> Unit
    ): ReadThread {
        val readThread = ReadThread(callback)
        executor.execute(readThread)
        return readThread
    }

    private class ReadThread(
        private val callback: (buffer: ByteArray, size: Int) -> Unit
    ) : Thread() {
        private val readBuffer = ByteArray(2048)
        private var readSize = 0
        private val objecz = Object()
        override fun run() {
            while (serialPort!!.isOpen) {
                try {
                    readSize = serialPort?.read(readBuffer, readBuffer.size, 50, 1)!!
                    if (readSize > 0) {
                        synchronized(objecz) {
                            objecz.notify()
                        }
                        callback.invoke(readBuffer, readSize)
                    }
                } catch (e: Exception) {
                    isRun = false
                    readThread?.close()
                    readThread = null
                    e.printStackTrace()
                }
            }
        }

        fun close() {
            interrupt()
        }
    }

    fun stop() {
        isRun = false
        readThread?.close()
        readThread = null
        serialPort?.close()
        serialPort = null
    }

    fun getBaudRate(baudrate: Int): BaudRate? {
        var value: BaudRate? = null
        when (baudrate) {
            9600 -> value = BaudRate.B9600
            19200 -> value = BaudRate.B19200
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
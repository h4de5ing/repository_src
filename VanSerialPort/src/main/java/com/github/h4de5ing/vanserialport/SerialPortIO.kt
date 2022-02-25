package com.github.h4de5ing.vanserialport

import com.van.uart.LastError
import com.van.uart.UartManager
import com.van.uart.UartManager.BaudRate
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object SerialPortIO {
    private var isRun: Boolean = false
    private val executor = ThreadPoolExecutor(3, 10, 5, TimeUnit.SECONDS, LinkedBlockingQueue())
    private var serialPort: UartManager? = null
    private var readThread: ReadThread? = null
    private var writeThread: WriteThread? = null
    fun start(name: String, baud: Int, callback: (buffer: ByteArray, size: Int) -> Unit) {
        try {
            val devName: String = name.split("/dev/").toTypedArray()[1]
            serialPort = UartManager()
            serialPort?.open(devName, getBaudRate(baud))
            isRun = true
            readThread = startReadThread(callback)
            writeThread = startWriteThread(readThread!!)
        } catch (e: Exception) {
            var result = "${e.message}".toByteArray()
            callback(result, result.size)
            e.printStackTrace()
        }
    }

    fun write(data: ByteArray): Boolean = (writeThread?.write(Packet(data)) == true)
    fun write(packet: Packet): Boolean = isRun && (writeThread?.write(packet) == true)
    private fun startReadThread(
        callback: (buffer: ByteArray, size: Int) -> Unit
    ): ReadThread {
        val readThread = ReadThread(callback)
        executor.execute(readThread)
        return readThread
    }

    private fun startWriteThread(readThread: ReadThread): WriteThread {
        val writeThread = WriteThread(readThread)
        executor.execute(writeThread)
        return writeThread
    }

    private class WriteThread(
        private val readThread: ReadThread
    ) : Thread() {
        private val queen = LinkedList<Packet>()
        private val objecz = Object()

        init {
            readThread.setWriteThread(this)
        }

        override fun run() {
            while (isRun) {
                while (queen.isEmpty()) {
                    synchronized(objecz) {
                        objecz.wait()
                    }
                }
                val poll = queen.poll()
                try {
                    (poll.buffer)
                    var buffer: ByteArray? = null
                    if (poll.callback != null) {
                        buffer = readThread.get(2000)
                    }
                    poll.callback?.invoke(buffer != null, buffer ?: poll.buffer)
                } catch (e: Exception) {
                    //e.printStackTrace()
                }
            }
        }

        @Synchronized
        private fun write(buffer: ByteArray) {
            if (serialPort != null && serialPort?.isOpen == true) {
                try {
                    serialPort?.write(buffer, buffer.size)
                } catch (lastError: LastError) {
                    lastError.printStackTrace()
                }
            }
        }

        fun write(packet: Packet): Boolean {
            var rt = false
            if (queen.size < 10) {
                queen.offer(packet)
                rt = true
            }
            synchronized(objecz) {
                objecz.notify()
            }
            return rt
        }

        fun close() {
            write(ByteArray(0))
            interrupt()
        }
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

        fun get(timeout: Long): ByteArray? {
            if (readSize <= 0) {
                synchronized(objecz) {
                    objecz.wait(timeout)
                }
            }
            if (readSize > 0)
                return readBuffer.copyOfRange(0, readSize)
            return null
        }

        private lateinit var writeThread: WriteThread

        fun setWriteThread(writeThread: WriteThread) {
            this.writeThread = writeThread
        }

        fun close() {
            interrupt()
        }
    }

    class Packet(
        val buffer: ByteArray,
        val callback: ((success: Boolean, buffer: ByteArray) -> Unit)? = null
    )

    fun stop() {
        isRun = false
        readThread?.close()
        readThread = null
        writeThread?.close()
        writeThread = null
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
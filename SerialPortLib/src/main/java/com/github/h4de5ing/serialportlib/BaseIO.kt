package com.github.h4de5ing.serialportlib

import android.os.SystemClock
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by luowei on 2017/9/14.
 * https://github.com/luv135/AsyncIO/blob/fa64145b48f1c925bc428961f6c05b9bd72ec439/baseiolibrary/src/main/java/com/unistrong/luowei/factorysuite/pc/serial/BaseIO.kt
 *
 */
abstract class BaseIO {

    private var isRun: Boolean = false
    private val executor = ThreadPoolExecutor(3, 10, 5, TimeUnit.SECONDS, LinkedBlockingQueue())

    fun start(
        inputStream: InputStream,
        outputStream: OutputStream,
        callback: (buffer: ByteArray, size: Int) -> Unit
    ) {
        isRun = true
        readThread = startReadThread(inputStream, callback)
        writeThread = startWriteThread(outputStream, readThread!!)
    }

    private var readThread: ReadThread? = null
    private var writeThread: WriteThread? = null
    open fun stop() {
        isRun = false
        readThread?.close()
        readThread = null
        writeThread?.close()
        writeThread = null

    }

    fun write(packet: Packet): Boolean = isRun && (writeThread?.write(packet) == true)
    fun write(data: ByteArray): Boolean = isRun && (writeThread?.write(Packet(data)) == true)

    private fun startWriteThread(outputStream: OutputStream, readThread: ReadThread): WriteThread {
        val writeThread = WriteThread(outputStream, readThread)
        executor.execute(writeThread)
        return writeThread
    }

    private fun startReadThread(
        inputStream: InputStream,
        callback: (buffer: ByteArray, size: Int) -> Unit
    ): ReadThread {
        val readThread = ReadThread(inputStream, callback)
        executor.execute(readThread)
        return readThread
    }

    inner class WriteThread(
        private val outputStream: OutputStream,
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
                    write(poll.buffer)
                    var buffer: ByteArray? = null
                    if (poll.callback != null) {
                        buffer = readThread.get(200)
                    }
                    poll.callback?.invoke(buffer != null, buffer ?: poll.buffer)
                } catch (e: Exception) {
                    //this@BaseIO.stop()
                }
            }
        }

        @Synchronized
        private fun write(buffer: ByteArray) {
            outputStream.write(buffer)
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
            write(Packet(ByteArray(0)))
            interrupt()
        }
    }

    class Packet(
        val buffer: ByteArray,
        val callback: ((success: Boolean, buffer: ByteArray) -> Unit)? = null
    )


    inner class ReadThread(
        private val inputStream: InputStream,
        private val callback: (buffer: ByteArray, size: Int) -> Unit
    ) : Thread() {
        private val readBuffer = ByteArray(1024)
        private var readSize = 0
        private val objecz = Object()
        private var readBytes: ByteArray? = null
        override fun run() {
//            while (isRun) {
//                try {
//                    readSize = inputStream.read(readBuffer)
//                    if (readSize > 0) {
//                        synchronized(objecz) {
//                            objecz.notify()
//                        }
//                        callback.invoke(readBuffer, readSize)
//                    }
//                } catch (e: Exception) {
//                    this@BaseIO.stop()
//                }
//            }
            while (!isInterrupted) {
                var size = 0
                try {
                    /** 获取流中数据的量 */
                    val i: Int = inputStream.available()
                    size = if (i == 0) {
                        0
                    } else {
                        /** 流中有数据，则添加到临时数组中 */
                        inputStream.read(readBuffer)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (size > 0) {
                    /** 发现有信息后就追加到临时变量 */
                    readBytes = DataUtil.arrayAppend(readBytes, readBuffer, size)
                } else {
                    /** 没有需要追加的数据了，回调 */
                    if (readBytes != null) {
//                        onDataReceived(readBytes)
                        callback.invoke(readBytes!!, readSize)
                    }
                    /** 清空，等待下个信息单元 */
                    readBytes = null
                }
                SystemClock.sleep(10)
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
}
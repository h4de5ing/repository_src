package com.github.h4de5ing.base

import android.os.Build
import java.io.*

//用Kt处理文件相关
fun String.append2File(filePath: String) {
    var writer: BufferedWriter? = null
    try {
        writer = BufferedWriter(FileWriter(filePath, true))
        writer.write(this)
    } catch (_: Exception) {
    } finally {
        closeQuietly(writer)
    }
}

fun String.write2File(filePath: String) {
    var writer: BufferedWriter? = null
    try {
        writer = BufferedWriter(FileWriter(filePath, false))
        writer.write(this)
    } catch (_: Exception) {
    } finally {
        close(writer)
    }
}

fun write2File(name: String, content: String, append: Boolean) {
    var writer: BufferedWriter? = null
    try {
        writer = BufferedWriter(FileWriter(name, append))
        writer.write(content)
    } catch (_: Exception) {
    } finally {
        closeQuietly(writer)
    }
}

fun closeQuietly(autoCloseable: AutoCloseable?) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            autoCloseable?.close()
    } catch (unused: Exception) {
        unused.printStackTrace()
    }
}

fun close(vararg closeables: Closeable?) {
    if (closeables.isEmpty()) return
    for (cb in closeables) {
        try {
            if (null == cb) continue
            cb.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

//判断是否是SQLite数据库文件
fun File.isValidDBFile() = try {
    val reader = FileReader(this)
    val buffer = CharArray(16)
    reader.read(buffer, 0, 16)
    val str = String(buffer)
    reader.close()
    str == "SQLite format 3\u0000"
} catch (e: Exception) {
    e.printStackTrace()
    false
}
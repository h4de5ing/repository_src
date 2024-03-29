package com.github.h4de5ing.base

import android.os.Build
import java.io.*

//用Kt处理文件相关
@Deprecated("使用File.write,此方法参数容易有歧义")
fun String.append2File(filePath: String): Boolean {
    return try {
        val writer = BufferedWriter(FileWriter(filePath, true))
        writer.write(this)
        writer.flush()
        writer.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Deprecated("使用File.write,此方法参数容易有歧义")
fun String.write2File(filePath: String): Boolean {
    return try {
        val writer = BufferedWriter(FileWriter(filePath, false))
        writer.write(this)
        writer.flush()
        writer.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
@Deprecated("有bug，写中文字符串会乱码,用kotlin appendText方法替代")
fun File.append(content: String): Boolean {
    return try {
        val writer = BufferedWriter(FileWriter(this, true))
        writer.write(content)
        writer.flush()
        writer.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Deprecated("有bug，写中文字符串会乱码,用kotlin writeText方法替代")
fun File.write(content: String): Boolean {
    return try {
        val writer = BufferedWriter(FileWriter(this, false))
        writer.write(content)
        writer.flush()
        writer.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun File.read(): String {
    val fileContent = StringBuilder()
    var reader: BufferedReader? = null
    try {
        val `is` = InputStreamReader(FileInputStream(this), "UTF-8")
        reader = BufferedReader(`is`)
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            if (fileContent.toString() != "") fileContent.append("\n")
            fileContent.append(line)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        closeQuietly(reader)
    }
    return fileContent.toString()
}

fun closeQuietly(autoCloseable: AutoCloseable?) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) autoCloseable?.close()
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
//fun File.isValidDBFile() = try {
//    val reader = FileReader(this)
//    val buffer = CharArray(16)
//    reader.read(buffer, 0, 16)
//    val str = String(buffer)
//    reader.close()
//    str == "SQLite format 3\u0000"
//} catch (e: Exception) {
//    e.printStackTrace()
//    false
//}
package com.github.h4de5ing.base

import java.io.File
import java.io.FileReader

//用Kt处理文件相关


//文件的读写
fun File.read() = try {

} catch (e: Exception) {
    e.printStackTrace()
}

fun File.write() = try {

} catch (e: Exception) {
    e.printStackTrace()
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
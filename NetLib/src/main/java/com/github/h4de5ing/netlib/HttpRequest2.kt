package com.github.h4de5ing.netlib

import android.annotation.SuppressLint
import java.io.BufferedReader
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.log10
import kotlin.math.pow


@SuppressLint("SdCardPath")
fun isDebug(): Boolean = File("/sdcard/debug").exists()
fun String.print() {
    if (isDebug()) println(this)
}

const val userAgent = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"
fun get(url: String, params: Map<String, Any>?, header: Map<String, String>?): String {
    val result = StringBuilder()
    try {
        val reader: BufferedReader
        var paramStr = StringBuilder()
        params?.apply {
            paramStr.append("?")
            for (key in params.keys) paramStr.append(key).append("=").append(params[key])
                .append("&")
            paramStr = StringBuilder(paramStr.substring(0, paramStr.length - 1))
        }
        val realUrl = URL(url + paramStr)
        val conn = realUrl.openConnection() as HttpURLConnection
        conn.setRequestProperty("accept", "*/*")
        conn.setRequestProperty("connection", "Keep-Alive")
        conn.setRequestProperty("user-agent", userAgent)
        header?.apply { for (key in this.keys) conn.setRequestProperty(key, this[key]) }
        val map = conn.headerFields
        url.print()
        for (key in map.keys) "${key}->${map[key]}".print()
        conn.connect()
        if (conn.responseCode == 200) {
            reader = BufferedReader(InputStreamReader(conn.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) result.append(line).append("\n")
            reader.close()
        } else throw Exception(conn.responseCode.toString() + " " + conn.responseMessage)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result.toString()
}

fun post(url: String, params: Map<String, Any>?, header: Map<String, String>?): String {
    val result = StringBuilder()
    val out: PrintWriter
    val reader: BufferedReader
    val paramStr = StringBuilder()
    params?.apply {
        for (key in this.keys) paramStr.append(key).append("=").append(this[key]).append("&")
    }
    val realUrl = URL(url)
    val conn = realUrl.openConnection()
    conn.setRequestProperty("accept", "*/*")
    conn.setRequestProperty("connection", "Keep-Alive")
    conn.setRequestProperty("user-agent", userAgent)
    header?.apply { for (key in this.keys) conn.setRequestProperty(key, this[key]) }
    conn.doOutput = true
    conn.doInput = true
    out = PrintWriter(conn.getOutputStream())
    out.print(paramStr)
    out.flush()
    url.print()
    paramStr.toString().print()
    val map = conn.headerFields
    for (key in map.keys) "${key}->${map[key]}".print()
    reader = BufferedReader(InputStreamReader(conn.getInputStream()))
    var line: String?
    while (reader.readLine().also { line = it } != null) result.append(line)
    out.close()
    reader.close()
    return result.toString()
}

fun post(url: String, json: String, header: Map<String, String>?): String {
    val result = StringBuilder()
    try {
        val realUrl = URL(url)
        val conn = realUrl.openConnection() as HttpURLConnection
        conn.setRequestProperty("connection", "Keep-Alive")
        conn.setRequestProperty("Charset", "UTF-8")
        conn.setRequestProperty("user-agent", userAgent)
        conn.setRequestProperty("Content-Length", json.toByteArray().size.toString())
        conn.setRequestProperty("Content-type", "application/json")
        header?.apply { for (key in this.keys) conn.setRequestProperty(key, this[key]) }
        conn.doOutput = true
        conn.doInput = true
        val out = conn.outputStream
        out.write(json.toByteArray())
        out.flush()
        out.close()
        val map = conn.headerFields
        "网址:$url".print()
        "json:$json".print()
        for (key in map.keys) "${key}->${map[key]}".print()
        if (conn.responseCode == 200) {
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) result.append(line).append("\n")
            reader.close()
        } else throw Exception(conn.responseCode.toString() + " " + conn.responseMessage)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result.toString()
}


/**
 * @description 文件上传
 * @param fileType Content-Type 类型，默认为：multipart/form-data
 * application/x-www-form-urlencoded：这是默认的表单数据编码类型
 * application/json：用于发送 JSON 格式的数据
 * text/plain：表示纯文本内容
 * text/html：表示 HTML 文档
 * image/jpeg：表示jpeg类型图片
 * image/png：表示png类型图片
 * application/pdf：用于表示 PDF 文档
 * application/octet-stream：表示二进制数据流
 * @return result
 */
fun uploadFile(
    urlStr: String?,
    fileMap: Map<String, File>?,
    params: Map<String, Any>?,
    header: Map<String, String>?,
    fileType: String = "multipart/form-data"
): String {
    val result: String
    val conn: HttpURLConnection
    //boundary就是request头和上传文件内容的分隔符
    val boundary = "---------------------------123821742118716"
    params?.apply {
        for (key in keys) "上传参数：${key}->${params[key]}".print()
    }
    header?.apply {
        for (key in keys) "上传头：${key}->${header[key]}".print()
    }
    val url = URL(urlStr)
    conn = url.openConnection() as HttpURLConnection
    conn.connectTimeout = 5000
    conn.readTimeout = 5000
    conn.doOutput = true
    conn.doInput = true
    conn.useCaches = false
    conn.requestMethod = "POST"
    conn.setRequestProperty("Connection", "Keep-Alive")
    conn.setRequestProperty("User-Agent", userAgent)
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
    header?.apply { for (key in this.keys) conn.setRequestProperty(key, this[key]) }
    val out: OutputStream = DataOutputStream(conn.outputStream)
    val strBuf0 = StringBuilder()
    params?.entries?.forEach {
        val inputName: String = it.key
        val inputValue = it.value
        strBuf0.append("\r\n").append("--").append(boundary).append("\r\n")
        strBuf0.append("Content-Disposition: form-data; name=\"").append(inputName)
            .append("\"\r\n\r\n")
        strBuf0.append(inputValue)
    }
    out.write(strBuf0.toString().toByteArray())
    fileMap?.entries?.forEach { fileEntry ->
        val inputName: String = fileEntry.key
        val file: File = fileEntry.value
        val filename = file.name
        val strBuf =
            "--${boundary}\r\nContent-Disposition: form-data; name=\"${inputName}\"; filename=\"${filename}\"\r\nContent-Type:$fileType\r\n\r\n"
        out.write(strBuf.toByteArray())
        val `in` = DataInputStream(FileInputStream(file))
        var bytes: Int
        val bufferOut = ByteArray(1024)
        while (`in`.read(bufferOut).also { bytes = it } != -1) out.write(bufferOut, 0, bytes)
        `in`.close()
    }
    val endData = "\r\n--$boundary".toByteArray()
    out.write(endData)
    out.flush()
    out.close()
    "网址:$url".print()
    val map = conn.headerFields
    for (key in map.keys) "head:${key}->${map[key]}".print()
    val strBuf1 = StringBuilder()
    val reader = BufferedReader(InputStreamReader(conn.inputStream))
    var line: String?
    while (reader.readLine().also { line = it } != null) strBuf1.append(line).append("\n")
    result = strBuf1.toString()
    reader.close()
    conn.disconnect()
    return result
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    val size = bytes / 1024.0.pow(digitGroups.toDouble())
    return "%.1f%s".format(size, units[digitGroups])
}

fun downloadFile(
    downloadUrl: String,
    fileSavePath: String,
    progress: (Int, String) -> Unit = { _, _ -> },
    error: (Throwable) -> Unit = {},
    complete: (File) -> Unit = {}
) {
    var connection: HttpURLConnection? = null
    var inputStream: InputStream? = null
    var outputStream: FileOutputStream? = null
    try {
        val url = URL(downloadUrl)
        connection = url.openConnection() as HttpURLConnection
        connection.connect()
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            error(IOException("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}"))
        }
        val fileSize = connection.contentLengthLong
        val file = File(fileSavePath)
        file.parentFile?.mkdirs()
        inputStream = connection.inputStream
        outputStream = FileOutputStream(file)
        val buffer = ByteArray(8 * 1024)
        var totalBytesRead: Long = 0
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead
            if (fileSize > 0) {
                val percent = (totalBytesRead * 100 / fileSize).toInt()
                // Format downloaded and total size (e.g., "5.2MB / 10MB")
                val downloadedStr = formatFileSize(totalBytesRead)
                progress(percent, "${downloadedStr}/${formatFileSize(fileSize)}")
            }
        }
        complete(file)
    } catch (e: Exception) {
        error(e)
    } finally {
        inputStream?.close()
        outputStream?.close()
        connection?.disconnect()
    }
}

fun closeSilently(closeable: Any?) {
    try {
        if (closeable != null) if (closeable is Closeable) closeable.close()
    } catch (_: IOException) {
    }
}
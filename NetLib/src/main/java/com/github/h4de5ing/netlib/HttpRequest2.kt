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
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL


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
            for (key in params.keys)
                paramStr.append(key)
                    .append("=")
                    .append(params[key])
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
        } else
            if (isDebug()) throw ResponseCodeErrorException(conn.responseCode.toString() + " " + conn.responseMessage)
    } catch (e: Exception) {
        if (isDebug()) e.printStackTrace()
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

@Throws(Exception::class)
fun post(url: String, json: String, header: Map<String, String>?): String {
    val result = StringBuilder()
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
    } else if (isDebug()) throw ResponseCodeErrorException(conn.responseCode.toString() + " " + conn.responseMessage)
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
@Throws(Exception::class)
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

private fun Long.ratio(bottom: Long): Double {
    if (bottom <= 0) return 0.0
    val result = (this * 100.0).toBigDecimal()
        .divide((bottom * 1.0).toBigDecimal(), 2, BigDecimal.ROUND_HALF_UP)
    return result.toDouble()
}

fun downloadFile(
    downloadUrl: String,
    fileSavePath: String,
    progress: (Int) -> Unit = {},
    error: (Throwable) -> Unit = {},
    complete: (File) -> Unit = {}
) {
    var downloadFile: File? = null
    var connection: HttpURLConnection? = null
    try {
        Thread.currentThread().priority = Thread.MIN_PRIORITY
        val url = URL(downloadUrl)
        connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 60000
        connection.doInput = true
        val `is` = connection.inputStream
        val temp = File(fileSavePath)
        if (temp.exists()) temp.delete()
        temp.createNewFile()
        downloadFile = temp
        val contentLength = connection.contentLength
        val os = FileOutputStream(temp)
        val buf = ByteArray(8 * 1024)
        var len: Int
        var totalRead: Long = 0
        try {
            while (`is`.read(buf).also { len = it } != -1) {
                os.write(buf, 0, len)
                totalRead += len
                progress((totalRead.ratio(contentLength.toLong())).toInt())
            }
            os.flush()
            os.fd.sync()
        } finally {
            closeSilently(os)
            closeSilently(`is`)
        }
        complete(temp)
        "download complete url=${downloadUrl}, fileSize=${temp.length()}".print()
    } catch (e: Exception) {
        error(e)
        downloadFile?.delete()
        e.printStackTrace()
    } finally {
        connection?.disconnect()
    }
}

fun closeSilently(closeable: Any?) {
    try {
        if (closeable != null) if (closeable is Closeable) closeable.close()
    } catch (_: IOException) {
    }
}
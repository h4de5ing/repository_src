package com.github.h4de5ing.netlib

import android.annotation.SuppressLint
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


@SuppressLint("SdCardPath")
fun isDebug(): Boolean = File("/sdcard/debug").exists() or BuildConfig.DEBUG
fun String.print() {
    if (isDebug()) println(this)
}

const val userAgent = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"
fun get(url: String, params: Map<String, Any>, header: Map<String, String>): String {
    val result = StringBuilder()
    try {
        val reader: BufferedReader
        var paramStr = StringBuilder()
        if (params.isNotEmpty()) {
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
        if (header.isNotEmpty()) for (key in header.keys)
            conn.setRequestProperty(key, header[key])
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

fun post(url: String, params: Map<String, Any>, header: Map<String, String>): String {
    val result = StringBuilder()
    val out: PrintWriter
    val reader: BufferedReader
    val paramStr = StringBuilder()
    if (params.isNotEmpty())
        for (key in params.keys) paramStr.append(key).append("=").append(params[key]).append("&")
    val realUrl = URL(url)
    val conn = realUrl.openConnection()
    conn.setRequestProperty("accept", "*/*")
    conn.setRequestProperty("connection", "Keep-Alive")
    conn.setRequestProperty("user-agent", userAgent)
    if (header.isNotEmpty()) for (key in header.keys) conn.setRequestProperty(key, header[key])
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
fun post(url: String, json: String, header: Map<String?, String?>?): String? {
    val result = StringBuilder()
    val realUrl = URL(url)
    val conn = realUrl.openConnection() as HttpURLConnection
    conn.setRequestProperty("connection", "Keep-Alive")
    conn.setRequestProperty("Charset", "UTF-8")
    conn.setRequestProperty("user-agent", userAgent)
    conn.setRequestProperty("Content-Length", json.toByteArray().size.toString())
    conn.setRequestProperty("Content-type", "application/json")
    if (header != null && header.isNotEmpty())
        for (key in header.keys) conn.setRequestProperty(key, header[key])
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

@Throws(Exception::class)
fun uploadFile(
    urlStr: String?,
    fileMap: Map<String, File>?,
    params: Map<String, Any>?,
    header: Map<String, String>?
): String? {
    val result: String
    val conn: HttpURLConnection
    //boundary就是request头和上传文件内容的分隔符
    val boundary = "---------------------------123821742118716"
    if (params != null && params.isNotEmpty()) for (key in params.keys) "上传参数：${key}->${params[key]}".print()
    if (header != null && header.isNotEmpty()) for (key in header.keys) "上传头：${key}->${header[key]}".print()
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
    if (header != null && header.isNotEmpty()) for (key in header.keys)
        conn.setRequestProperty(key, header[key])
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
    fileMap?.entries?.forEach {
        val inputName: String = it.key
        val file: File = it.value
        val filename = file.name
        val strBuf =
            "\r\n--${boundary}\r\nContent-Disposition: form-data; name=\"${inputName}\"; filename=\"${filename}\"\r\nContent-Type:multipart/form-data\r\n\r\n" +
                    "\n"
        out.write(strBuf.toByteArray())
        val `in` = DataInputStream(FileInputStream(file))
        var bytes: Int
        val bufferOut = ByteArray(1024)
        while (`in`.read(bufferOut).also { bytes = it } != -1) out.write(bufferOut, 0, bytes)
        `in`.close()
    }
    val endData = "\r\n--$boundary--\r\n".toByteArray()
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

interface FileDownloadComplete {
    fun error(throwable: Throwable?)
    fun progress(progress: Long)
    fun complete(file: File?)
}

fun downloadFile(downloadUrl: String, fileSavePath: String, complete: FileDownloadComplete) {
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
                totalRead += len.toLong()
                complete.progress(totalRead * 100 / contentLength)
            }
            os.flush()
            os.fd.sync()
        } finally {
            closeSilently(os)
            closeSilently(`is`)
        }
        complete.complete(temp)
        "download complete url=${downloadUrl}, fileSize=${temp.length()}".print()
    } catch (e: Exception) {
        complete.error(e)
        downloadFile?.delete()
    } finally {
        connection?.disconnect()
    }
}

fun closeSilently(closeable: Any?) {
    try {
        if (closeable != null) if (closeable is Closeable) closeable.close()
    } catch (ignored: IOException) {
    }
}
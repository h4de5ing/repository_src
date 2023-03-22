package com.github.h4de5ing.netlib

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL


fun isDebug(): Boolean = File("/sdcard/debug").exists() or BuildConfig.DEBUG

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
        conn.setRequestProperty(
            "user-agent",
            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"
        )
        if (header.isNotEmpty()) for (key in header.keys)
            conn.setRequestProperty(key, header[key])
        val map = conn.headerFields
        if (isDebug()) println(url)
        for (key in map.keys) if (isDebug()) println(key + "--->" + map[key])
        conn.connect()
        if (conn.responseCode == 200) {
            reader = BufferedReader(InputStreamReader(conn.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) result.append(line).append("\n")
            reader.close()
        } else {
            if (isDebug()) throw ResponseCodeErrorException(conn.responseCode.toString() + " " + conn.responseMessage)
        }
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
    if (params.isNotEmpty()) {
        for (key in params.keys) paramStr.append(key).append("=").append(params[key]).append("&")
    }
    val realUrl = URL(url)
    val conn = realUrl.openConnection()
    conn.setRequestProperty("accept", "*/*")
    conn.setRequestProperty("connection", "Keep-Alive")
    conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
    if (header.isNotEmpty()) {
        for (key in header.keys) conn.setRequestProperty(key, header[key])
    }
    conn.doOutput = true
    conn.doInput = true
    out = PrintWriter(conn.getOutputStream())
    out.print(paramStr)
    out.flush()
    println(url)
    println(paramStr)
    val map = conn.headerFields
    for (key in map.keys) println(key + "--->" + map[key])
    reader = BufferedReader(InputStreamReader(conn.getInputStream()))
    var line: String?
    while (reader.readLine().also { line = it } != null) result.append(line)
    out.close()
    reader.close()
    return result.toString()
}
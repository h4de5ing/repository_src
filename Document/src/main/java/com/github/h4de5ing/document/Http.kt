package com.github.h4de5ing.document

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface

fun getMyIp(): String {
    var ip = "127.0.0.1"
    try {
        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
            if (networkInterface.isUp && !networkInterface.isLoopback) {
                networkInterface.interfaceAddresses.forEach {
                    when (it.address) {
                        is Inet4Address -> {
                            it.address.hostAddress?.apply { ip = this }
                        }
                    }
                }
            }
        }
    } catch (_: Exception) {
    }
    return ip
}

fun initHttp(context: Context, port: Int) {
    try {
        Http(context, port).start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

class Http(private val context: Context, port: Int) : NanoHTTPD(port) {
    private fun getAssets(fileName: String): InputStream? = try {
        context.assets?.open(fileName)
    } catch (e: Exception) {
        context.assets?.open("404.html")
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        var filename = uri.substring(1)
        if (uri == "/") filename = "index.html"
        val mimetype: String
        var isPng = false
        if (filename.contains(".html") || filename.contains(".htm")) {
            mimetype = "text/html"
        } else if (filename.contains(".js")) {
            mimetype = "text/javascript"
        } else if (filename.contains(".css")) {
            mimetype = "text/css"
        } else if (filename.contains(".png")) {
            mimetype = "image/png"
            isPng = true
        } else {
            mimetype = "text/html"
        }
        val response = StringBuilder()
        var line: String?
        val reader: BufferedReader
        return if (isPng) {
            try {
                val isr: InputStream = getAssets(filename)!!
                newFixedLengthResponse(Response.Status.OK, mimetype, isr, isr.available().toLong())
            } catch (e: IOException) {
                newFixedLengthResponse(Response.Status.OK, mimetype, "png not found")
            }
        } else {
            try {
                if (filename.endsWith(".md") || filename.endsWith(".markdown"))
                    response.append("<script src=\"./md-page.js\"></script><noscript>")
                reader = BufferedReader(InputStreamReader(getAssets(filename)))
                while (reader.readLine().also { line = it } != null)
                    response.append(line).append("\n")
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            newFixedLengthResponse(Response.Status.OK, mimetype, response.toString())
        }
    }
}
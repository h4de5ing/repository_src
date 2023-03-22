package com.github.h4de5ing.netlib

import java.io.IOException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.ProtocolException

//TODO 封装网络请求 并将结果以Result形势返回

fun String.httpGet(block: ((String) -> Unit)) {
    try {
        val response = HttpRequest.sendGet(this, null, null)
        block(response)
    } catch (e: MalformedURLException) {
        System.err.println("URL格式错误：${e.message}")
    } catch (e: ConnectException) {
        System.err.println("端口错误：${e.message}")
    } catch (e: IOException) {
        System.err.println("IOException错误：${e.message}")
    } catch (e: ProtocolException) {
        System.err.println("ProtocolException错误：${e.message}")
    } catch (e: ResponseCodeErrorException) {
        System.err.println("服务器响应错误码：${e.message}")
    } catch (e: Exception) {
        System.err.println("未知错误：${e.message}")
    }
}

fun String.httpGet(headerMap: HashMap<String, String>, block: ((String) -> Unit)) {
    try {
        val response = HttpRequest.sendGet(this, null, headerMap)
        block(response)
    } catch (e: MalformedURLException) {
        System.err.println("URL格式错误：${e.message}")
    } catch (e: ConnectException) {
        System.err.println("端口错误：${e.message}")
    } catch (e: IOException) {
        System.err.println("IOException错误：${e.message}")
    } catch (e: ProtocolException) {
        System.err.println("ProtocolException错误：${e.message}")
    } catch (e: ResponseCodeErrorException) {
        System.err.println("服务器响应错误码：${e.message}")
    } catch (e: Exception) {
        System.err.println("未知错误：${e.message}")
    }
}
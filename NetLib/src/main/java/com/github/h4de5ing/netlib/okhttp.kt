package com.github.h4de5ing.netlib

import com.github.h4de5ing.netlib.https.InternalSSLSocketFactory
import okhttp3.Authenticator
import okhttp3.CipherSuite
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.Credentials
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.TlsVersion
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.security.KeyStore
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * 设置OkHttpClient，主要是设置代理和SSL
 */
fun okClient(
    useSSL: Boolean = true,
    proxy: Proxy = Proxy.NO_PROXY,
    proxyAuthenticator: Authenticator = Authenticator.NONE,
): OkHttpClient {
    val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3).cipherSuites(
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_AES_128_GCM_SHA256,
            CipherSuite.TLS_AES_256_GCM_SHA384,
            // Note that the following cipher suites are all on HTTP/2's bad cipher suites list.
            // We'll continue to include them until better suites are commonly available. For example,
            // none of the better cipher suites listed above shipped with Android 4.4 or Java 7.
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,

            // Additional inf ura CipherSuites
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256,
            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256
        ).build()
    val okHttpBuilder: OkHttpClient.Builder =
        OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS)
            .hostnameVerifier { hostname, session -> true }
            .protocols(listOf(Protocol.HTTP_1_1, Protocol.HTTP_2)).proxy(proxy)
            .proxyAuthenticator(proxyAuthenticator)
    try {
        val trustManagerFactory: TrustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            ("Unexpected default trust managers:" + trustManagers.contentToString())
        }
        val trustManager: X509TrustManager = trustManagers[0] as X509TrustManager
        if (useSSL) {
            okHttpBuilder.connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
            okHttpBuilder.sslSocketFactory(InternalSSLSocketFactory(), trustManager)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return okHttpBuilder.build()
}

/**
 * 简单get请求
 */
fun okSampleGet(url: String): Int {
    val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS).connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS))
        .build()
    val build = Request.Builder().url(url).build()
    val response = client.newCall(build).execute()
    val code = response.code
    val body = response.body.string()
    println("请求结果:${code},${body}")
    return code
}

/**
 * 设置代理ip和端口
 */
fun proxy(ip: String, port: Int): Proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(ip, port))

/**
 * 设置代理用户名和密码
 */
fun proxyAuthenticator(username: String, password: String): Authenticator {
    val proxyAuthenticator = Authenticator { _, response ->
        val credential = Credentials.basic(username, password)
        response.request.newBuilder().header("Proxy-Authorization", credential).build()
    }
    return proxyAuthenticator
}

/**
 * 默认ua
 */
val defaultHeaders = mapOf(
    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
)

/**
 * okhttp get请求
 */
fun okGet(
    url: String,
    params: Map<String, Any> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    proxy: Proxy = Proxy.NO_PROXY,
    proxyAuthenticator: Authenticator = Authenticator.NONE,
): String {
    val start = System.currentTimeMillis()
    var newUrl = url
    if (params.isNotEmpty()) newUrl += "?" + params.map { "${it.key}=${it.value}" }
        .joinToString("&")
    val request: Request = Request.Builder().url(newUrl).headers(headers.toHeaders()).build()
//    request.headers.forEach { t -> println("${t.first}:${t.second}") }
    val response =
        okClient(url.startsWith("https"), proxy, proxyAuthenticator).newCall(request).execute()
    val stop = System.currentTimeMillis()
    println("${nowISO()} ${URL(newUrl).let { "${it.protocol}://${it.host}${it.path}" }} -> ${response.code},took ${stop - start}ms")
    val responseBody = response.body.string()
    if (response.code != 200) println("发生了啥:$responseBody")
    return responseBody
}

/**
 * okhttp post请求
 */
fun okPostJson(
    url: String,
    json: String,
    headers: Map<String, String> = emptyMap(),
    proxy: Proxy = Proxy.NO_PROXY,
    proxyAuthenticator: Authenticator = Authenticator.NONE,
): String {
    val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
    println("okPostJson:$json")
    val request = Request.Builder().url(url).headers(headers.toHeaders()).post(body).build()
    request.headers.forEach { t -> println("${t.first}:${t.second}") }
    val response =
        okClient(url.startsWith("https"), proxy, proxyAuthenticator).newCall(request).execute()
    println("${nowISO()} ${URL(url).let { "${it.protocol}://${it.host}${it.path}" }} -> ${response.code}")
    val responseBody = response.body.string()
    if (response.code != 200) println("发生了啥:$responseBody")
    return responseBody
}

fun okFileUpload() {}
fun okFileDownload() {}
fun nowISO(): String =
    DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(Instant.now().atZone(ZoneId.systemDefault()))
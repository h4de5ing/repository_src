package com.github.h4de5ing.netlib.https

import okio.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class InternalSSLSocketFactory : SSLSocketFactory() {
    private val internalSSLSocketFactory: SSLSocketFactory

    override fun getDefaultCipherSuites(): Array<String> {
        return internalSSLSocketFactory.defaultCipherSuites;
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
        val socket = internalSSLSocketFactory.createSocket(s, host, port, autoClose)
//        addTlsLogging(socket = socket as SSLSocket)
        return enableTLSOnSocket(socket)
    }

    fun addTlsLogging(socket: SSLSocket) {
        socket.addHandshakeCompletedListener { event ->
            println("\n=== TLS Handshake Details ===")
            println("Cipher Suite: ${event.cipherSuite}")
            println("Session ID: ${event.session.id.toHexString()}")
            println("Peer Principal: ${event.peerPrincipal}")
            println("Local Supported Cipher Suites: ${socket.supportedCipherSuites.joinToString()}")
            println("Negotiated Cipher Suite: ${socket.session.cipherSuite}")
            println("Protocol: ${socket.session.protocol}")
        }
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String?, port: Int): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(
        host: String?,
        port: Int,
        localHost: InetAddress?,
        localPort: Int
    ): Socket {

        return enableTLSOnSocket(
            internalSSLSocketFactory.createSocket(
                host,
                port,
                localHost,
                localPort
            )
        )
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress?, port: Int): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int
    ): Socket {
        return enableTLSOnSocket(
            internalSSLSocketFactory.createSocket(
                address,
                port,
                localAddress,
                localPort
            )
        )
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return internalSSLSocketFactory.supportedCipherSuites;
    }


    private fun enableTLSOnSocket(socket: Socket): Socket {
        if (socket is SSLSocket) socket.enabledProtocols = arrayOf("TLSv1.2", "TLSv1.3")
        return socket
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { eachByte -> "%02x".format(eachByte) }

    init {
        val context: SSLContext = SSLContext.getInstance("TLS")
        context.init(null, null, null)
        internalSSLSocketFactory = context.socketFactory
    }
}
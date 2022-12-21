package com.github.h4de5ing.base

import android.content.Context
import android.content.pm.PackageManager
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.cert.Certificate
import java.util.jar.JarEntry
import java.util.jar.JarFile

object SignUtil {
    /**
     * 获取已经安装的 app 的 MD5 签名信息
     */
    @Throws(Exception::class)
    fun getApkSignatureMD5(apkPath: String): String {
        val sign = getSignaturesFromApk(apkPath)
        return if (sign != null) hexDigest(sign, "MD5") else ""
    }

    @Throws(Exception::class)
    fun getApkSignatureSHA1(apkPath: String): String {
        val sign = getSignaturesFromApk(apkPath)
        return if (sign != null) hexDigest(sign, "SHA1") else ""
    }

    @Throws(Exception::class)
    fun getApkSignatureSHA256(apkPath: String): String {
        val sign = getSignaturesFromApk(apkPath)
        return if (sign != null) hexDigest(sign, "SHA256") else ""
    }

    fun getAppSignature(context: Context, pkgName: String?, algorithm: String): String {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(pkgName!!, PackageManager.GET_SIGNATURES)
            val signs = packageInfo.signatures
            val sign = signs[0]
            return hexDigest(sign.toByteArray(), algorithm)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 从APK中读取签名
     *
     * @param apkPath apk路径
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getSignaturesFromApk(apkPath: String): ByteArray? {
        val file = File(apkPath)
        val jarFile = JarFile(file)
        try {
            val je = jarFile.getJarEntry("AndroidManifest.xml")
            val readBuffer = ByteArray(8192)
            val certs = loadCertificates(jarFile, je, readBuffer)
            if (certs != null) for (c in certs) return c.encoded
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    /**
     * 加载签名
     * 如果是debug或者未签名的apk 会导致获取为null
     *
     * @param jarFile    jar文件
     * @param je         jar实体
     * @param readBuffer 读取缓存
     * @return
     */
    fun loadCertificates(
        jarFile: JarFile,
        je: JarEntry?,
        readBuffer: ByteArray
    ): Array<Certificate>? {
        try {
            val `is` = jarFile.getInputStream(je)
            while (`is`.read(readBuffer, 0, readBuffer.size) != -1) {
            }
            `is`.close()
            return je?.certificates
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun hexDigest(bytes: ByteArray, algorithm: String): String {
        val md5: MessageDigest
        return try {
            md5 = MessageDigest.getInstance(algorithm)
            val md5Bytes = md5.digest(bytes)
            val hexValue = StringBuilder()
            for (md5Byte in md5Bytes) {
                val `val` = md5Byte.toInt() and 0xff
                if (`val` < 16) hexValue.append("0")
                hexValue.append(Integer.toHexString(`val`))
            }
            hexValue.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
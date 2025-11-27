package com.android.otalibrary.ext

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.security.MessageDigest

object Utils {
    fun hexdigest(paramArrayOfByte: ByteArray): String {
        val hexDigits = "0123456789abcdef".toCharArray()
        try {
            val localMessageDigest = MessageDigest.getInstance("MD5")
            localMessageDigest.update(paramArrayOfByte)
            val arrayOfByte = localMessageDigest.digest()
            val arrayOfChar = CharArray(32)
            var i = 0
            var j = 0
            while (true) {
                if (i >= 16) return String(arrayOfChar)
                val k = arrayOfByte[i].toInt()
                arrayOfChar[j] = hexDigits[(0xF and (k ushr 4))]
                arrayOfChar[++j] = hexDigits[(k and 0xF)]
                i++
                j++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun installApk(context: Context, apk: String, packageName: String?) {
        try {
            val apkFile = File(apk)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (!apkFile.exists()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val ss = "$packageName.provider"
                val contentUri =
                    FileProvider.getUriForFile(context.applicationContext, ss, apkFile)
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
            } else {
                intent.setDataAndType(
                    Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive"
                )
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

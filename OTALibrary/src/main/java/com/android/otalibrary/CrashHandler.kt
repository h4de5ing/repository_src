package com.android.otalibrary

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess

class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private var mContext: Context? = null
    private val appInformation: MutableMap<String?, String?> = HashMap<String?, String?>()

    @SuppressLint("SimpleDateFormat")
    private val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    private var logDir: File? = null

    fun init(context: Context) {
        mContext = context
        logDir = mContext!!.getExternalFilesDir("LOG")
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler?.uncaughtException(thread, ex)
        } else {
            try {
                Thread.sleep(3000)
            } catch (_: InterruptedException) {
            }
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }


    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) return false
        collectDeviceInfo(mContext!!)
        saveCrashInfo2File(ex)
        return true
    }

    fun collectDeviceInfo(ctx: Context) {
        try {
            val pm = ctx.packageManager
            val pi = pm.getPackageInfo(ctx.packageName, PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName: String =
                    (if (pi.versionName == null) "null" else pi.versionName) ?: ""
                val versionCode = pi.versionCode.toString() + ""
                appInformation["versionName"] = versionName
                appInformation["versionCode"] = versionCode
            }
        } catch (_: PackageManager.NameNotFoundException) {
        }
        val fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                appInformation[field.name] = "${field.get(null)}"
            } catch (_: Exception) {
            }
        }
    }

    private fun saveCrashInfo2File(ex: Throwable) {
        val sb = StringBuilder()
        for (entry in appInformation.entries) {
            val key = entry.key
            val value = entry.value
            sb.append(key).append("=").append(value).append("\n")
        }

        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result = writer.toString()
        sb.append(result)
        try {
            val timestamp = System.currentTimeMillis()
            val time = formatter.format(Date())
            val fileName = "$time-$timestamp.txt"
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val file = File(logDir, fileName)
                val fos = FileOutputStream(file)
                fos.write(sb.toString().toByteArray())
                fos.close()
            }
        } catch (_: Exception) {
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance: CrashHandler = CrashHandler()
    }
}
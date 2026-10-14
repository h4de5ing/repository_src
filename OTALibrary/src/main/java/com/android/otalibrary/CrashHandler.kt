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
        // 先把崩溃信息同步落盘（已在内部 try/catch，失败也不影响后续结束流程）
        handleException(ex)
        // 关键：不要在崩溃线程上 Thread.sleep。
        // 若崩溃发生在主线程，长时间 sleep 会阻塞输入分发，把一次崩溃放大成“输入超时 ANR”，
        // 反而掩盖真正的崩溃（见 FileEditor 大文件崩溃被放大为 ANR 的问题）。
        // 本地日志已由 saveCrashInfo2File 同步写盘，无需再 sleep 等待。
        Process.killProcess(Process.myPid())
        exitProcess(1)
    }


    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) return false
        return try {
            mContext?.let { collectDeviceInfo(it) }
            saveCrashInfo2File(ex)
            true
        } catch (_: Throwable) {
            false
        }
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
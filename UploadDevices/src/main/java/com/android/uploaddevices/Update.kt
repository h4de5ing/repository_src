package com.android.uploaddevices

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import kotlinx.coroutines.MainScope
import kotlinx.serialization.json.Json

private var scope = MainScope()
val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = true
    encodeDefaults = true
}
private lateinit var mContext: Context
fun initialize(context: Context) {
    mContext = context
}


fun getDeviceInfo() {
    val list = buildList {
        add("Android: ${android.os.Build.VERSION.SDK_INT}")
        add("Network: ${getNetworkType(mContext)}")
    }
}

/**
 * 获取网络类型
 */
fun getNetworkType(context: Context): String {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val ani = cm.activeNetworkInfo
    if (ani != null && ani.isConnected) {
        return when (val type = ani.type) {
            9 -> "Ethernet"
            1 -> "WiFi"
            0 -> {
                val telephoneManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val networkType = when (val type2 = telephoneManager.networkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN -> "2G"

                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"

                    TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                    TelephonyManager.NETWORK_TYPE_NR -> "5G"
                    else -> "Unknown[${type2}]"
                }
                val subtype = ani.subtype
                val subName = ani.subtypeName
                "Mobile - ${subName}- $subtype - $networkType"
            }

            else -> "Unknown[$type]"
        }
    } else return "No Network"
}
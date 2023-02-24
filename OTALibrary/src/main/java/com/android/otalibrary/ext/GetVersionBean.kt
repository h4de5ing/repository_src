package com.android.otalibrary.ext

data class GetVersionBean(val packageName: String, val versionCode: Long, val list: List<Data>)

data class Data(
    val tag: String, val hash: String, val apkPath: String
)
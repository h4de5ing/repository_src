package com.github.h4de5ing.base

import java.io.*
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

//常见的扩展函数
//判断任何对象是否为空
fun Any?.isNotEmpty(): Boolean = this != null
fun Double.nDecimal(n: Int): String = String.format("%.${n}f", this)
fun Long.date(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date(this))
fun Long.date(pattern: String) = SimpleDateFormat(pattern, Locale.CHINA).format(Date(this))
fun now(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date(System.currentTimeMillis()))

fun string2Date(s: String?, style: String?): Date? {
    val simpleDateFormat = SimpleDateFormat(style)
    var date: Date? = null
    if (s == null || s.length < 6) {
        return null
    }
    try {
        date = simpleDateFormat.parse(s)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date
}

fun today(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(System.currentTimeMillis()))

fun todayZero(): Long =
    System.currentTimeMillis() / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().rawOffset

fun thisMonth(): String =
    SimpleDateFormat("yyyy-MM", Locale.CHINA).format(Date(System.currentTimeMillis()))

fun Long.toTime(): String = SimpleDateFormat("HHmmss", Locale.CHINA).format(Date(this))

fun Int.startZeroStr(format: String): String = DecimalFormat(format).format(this)

fun delayed(delay: Long, block: () -> Unit) {
    Timer().schedule(object : TimerTask() {
        override fun run() {
            block()
        }
    }, delay)
}

fun timer(delay: Long, block: () -> Unit) {
    Timer().schedule(object : TimerTask() {
        override fun run() {
            block()
        }
    }, 0, delay)
}

/**
 * "data.json".stream().buffered().reader("utf-8").readLines()
 */
fun String.stream() = FileInputStream(this)
fun InputStream.buffered() = BufferedInputStream(this)
fun InputStream.reader(charset: String) = InputStreamReader(this, charset)
fun Reader.readLines(): List<String> {
    val result = arrayListOf<String>()
    forEachLine { result.add(it) }
    return result
}
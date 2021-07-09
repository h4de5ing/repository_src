package com.github.h4de5ing.base

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

//获取某天0点
private fun getData0(year: Int, month: Int, dayOfMonth: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, dayOfMonth, 0, 0, 0)
    return calendar.time.time
}

//获取某天24点
private fun getToDay24(year: Int, month: Int, dayOfMonth: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, dayOfMonth, 23, 59, 59)
    return calendar.time.time
}

//获取今天第一秒
fun getStartOfDay(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time.time
}

//获取今天最后一秒
fun getEndOfDay(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time.time
}

fun todayTime(): Long = System.currentTimeMillis()
fun Long.toHuman() = this / 1000000000
val Int.days: Long get() = (this * 3600 * 24).toLong()
val Long.ago: Long get() = todayTime() - this * 1000

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
 * assets.open("txt.txt").buffered().bufferedReader(Charsets.UTF_8).readLines().forEach {
list.add(it)
}
 */
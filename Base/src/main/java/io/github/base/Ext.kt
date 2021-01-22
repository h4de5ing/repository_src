package io.github.base

import java.text.SimpleDateFormat
import java.util.*

//判断任何对象是否为空
fun Any?.isNotEmpty(): Boolean = this != null
fun Double.nDecimal(n: Int): String = String.format("%.${n}f", this)
fun Long.date(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date(this))
fun today(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(System.currentTimeMillis()))

fun thisMonth(): String =
    SimpleDateFormat("yyyy-MM", Locale.CHINA).format(Date(System.currentTimeMillis()))

fun Long.toTime(): String = SimpleDateFormat("HHmmss", Locale.CHINA).format(Date(this))

fun delayed(delay: Long, block: () -> Unit) {
    Timer().schedule(object : TimerTask() {
        override fun run() {
            block()
        }
    }, delay)
}
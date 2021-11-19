package com.github.h4de5ing.base

import kotlin.experimental.and

//主要用于处理进制转换以及各种运算
/**
 * @param s input string like : 000102030405060708
 * @return byte[] b={0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08}
 */
fun int2bytes2(s2: String): ByteArray {
    var newStr = s2.replace(" ", "")
    var data = ByteArray(newStr.length / 2)
    try {
        if (newStr.length % 2 != 0) {
            newStr = newStr.substring(0, newStr.length - 1) + "0" + newStr.substring(
                newStr.length - 1,
                newStr.length
            )
        }
        for (j in data.indices) {
            data[j] = (Integer.valueOf(newStr.substring(j * 2, j * 2 + 2), 16) and 0xff).toByte()
        }
    } catch (e: Exception) {

    }
    return data
}

fun printlnHex(data: ByteArray): String = printlnHex(data, data.size)
fun printlnHex(data: ByteArray, length: Int): String {
    val sb: StringBuilder = StringBuilder(data.size * 2)
    val HEX =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    for (i in 0 until length) {
        val value: Byte = data[i] and 0xff.toByte()
        sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ")
    }
    return sb.toString()
}
package com.github.h4de5ing.base

import java.io.Closeable
import java.io.File
import java.io.IOException

@Deprecated(
    message = "Use File(filePath).append(content) instead.",
    replaceWith = ReplaceWith("File(filePath).append(this)", "java.io.File")
)
fun String.append2File(filePath: String): Boolean = File(filePath).append(this)

@Deprecated(
    message = "Use File(filePath).write(content) instead.",
    replaceWith = ReplaceWith("File(filePath).write(this)", "java.io.File")
)
fun String.write2File(filePath: String): Boolean = File(filePath).write(this)

fun File.append(content: String): Boolean = performFileOperation {
    appendText(content, Charsets.UTF_8)
}

fun File.write(content: String): Boolean = performFileOperation {
    writeText(content, Charsets.UTF_8)
}

fun File.read(): String = try {
    bufferedReader(Charsets.UTF_8).useLines { lines ->
        lines.joinToString(separator = "\n")
    }
} catch (e: Exception) {
    e.printStackTrace()
    ""
}

private inline fun performFileOperation(operation: () -> Unit): Boolean {
    return try {
        operation()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun closeQuietly(autoCloseable: AutoCloseable?) {
    try {
        autoCloseable?.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun close(vararg closeables: Closeable?) {
    closeables.filterNotNull().forEach { closeable ->
        try {
            closeable.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
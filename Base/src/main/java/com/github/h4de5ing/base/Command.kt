package com.github.h4de5ing.base

import android.text.TextUtils
import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

const val TAG = "Command"
const val COMMAND_SH = "sh"
const val COMMAND_EXIT = "exit\n"
const val COMMAND_LINE_END = "\n"

class CommandResult {
    var result = -1
    var errorMsg: String? = null
    var successMsg: String? = null
    override fun toString(): String {
        val sb = StringBuilder()
        if (!TextUtils.isEmpty(errorMsg)) sb.append("error=").append(errorMsg)
        if (!TextUtils.isEmpty(successMsg)) sb.append("success=").append(successMsg)
        return sb.toString()
    }
}

fun exec(command: String): CommandResult {
    val commandResult = CommandResult()
    var process: Process? = null
    var os: DataOutputStream? = null
    var successResult: BufferedReader? = null
    var errorResult: BufferedReader? = null
    val successMsg: StringBuilder
    val errorMsg: StringBuilder
    try {
        process =
            if (command.contains("|")) Runtime.getRuntime().exec(arrayOf(COMMAND_SH, "-c", command))
            else Runtime.getRuntime().exec(COMMAND_SH)
        os = DataOutputStream(process.outputStream)
        os.write(command.toByteArray())
        os.writeBytes(COMMAND_LINE_END)
        os.writeBytes(COMMAND_EXIT)
        os.flush()
        commandResult.result = process.waitFor()
        successMsg = StringBuilder()
        errorMsg = StringBuilder()
        successResult = BufferedReader(InputStreamReader(process.inputStream))
        errorResult = BufferedReader(InputStreamReader(process.errorStream))
        var s: String?
        while (successResult.readLine().also { s = it } != null) successMsg.append(s).append("\n")
        while (errorResult.readLine().also { s = it } != null) errorMsg.append(s).append("\n")
        commandResult.successMsg = successMsg.toString()
        commandResult.errorMsg = errorMsg.toString()
    } catch (e: Exception) {
        val errMsg = e.message
        if (errMsg != null) {
            Log.e(TAG, errMsg)
        } else {
            e.printStackTrace()
        }
    } finally {
        try {
            os?.close()
            successResult?.close()
            errorResult?.close()
        } catch (e: IOException) {
            val errMsg = e.message
            if (errMsg != null) {
                Log.e(TAG, errMsg)
            } else {
                e.printStackTrace()
            }
        }
        process?.destroy()
    }
    return commandResult
}

fun exec(commands: Array<String>): CommandResult {
    val commandResult = CommandResult()
    var process: Process? = null
    var os: DataOutputStream? = null
    var successResult: BufferedReader? = null
    var errorResult: BufferedReader? = null
    val successMsg: StringBuilder
    val errorMsg: StringBuilder
    try {
        process = Runtime.getRuntime().exec(COMMAND_SH)
        os = DataOutputStream(process.outputStream)
        for (command in commands) {
            os.write(command.toByteArray())
            os.writeBytes(COMMAND_LINE_END)
            os.flush()
        }
        os.writeBytes(COMMAND_EXIT)
        os.flush()
        commandResult.result = process.waitFor()
        successMsg = StringBuilder()
        errorMsg = StringBuilder()
        successResult = BufferedReader(InputStreamReader(process.inputStream))
        errorResult = BufferedReader(InputStreamReader(process.errorStream))
        var s: String?
        while (successResult.readLine().also { s = it } != null) successMsg.append(s).append("\n")
        while (errorResult.readLine().also { s = it } != null) errorMsg.append(s).append("\n")
        commandResult.successMsg = successMsg.toString()
        commandResult.errorMsg = errorMsg.toString()
    } catch (e: Exception) {
        val errMsg = e.message
        if (errMsg != null) {
            Log.e(TAG, errMsg)
        } else {
            e.printStackTrace()
        }
    } finally {
        try {
            os?.close()
            successResult?.close()
            errorResult?.close()
        } catch (e: IOException) {
            val errMsg = e.message
            if (errMsg != null) {
                Log.e(TAG, errMsg)
            } else {
                e.printStackTrace()
            }
        }
        process?.destroy()
    }
    return commandResult
}
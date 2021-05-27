package com.github.h4de5ing.baseui.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.h4de5ing.baseui.SPUtils
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable

open class BaseActivity : AppCompatActivity() {
    inline fun <reified TARGET : Activity> Activity.startActivity(
        vararg params: Pair<String, Any>
    ) = startActivity(Intent(this, TARGET::class.java).putExtras(*params))

    fun getSP(key: String, defaultValue: Any) = SPUtils.getSp(this, key, defaultValue)

    fun getSP(key: String) = getSP(key, "")

    fun setSP(key: String, value: Any) = SPUtils.setSP(this, key, value)

    fun showToast(message: String) =
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

    fun showSnack(view: View, message: String) =
        runOnUiThread { Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show() }

    fun Intent.putExtras(vararg params: Pair<String, Any>): Intent {
        if (params.isEmpty()) return this
        params.forEach { (key, value) ->
            when (value) {
                is Int -> putExtra(key, value)
                is Byte -> putExtra(key, value)
                is Char -> putExtra(key, value)
                is Long -> putExtra(key, value)
                is Float -> putExtra(key, value)
                is Short -> putExtra(key, value)
                is Double -> putExtra(key, value)
                is Boolean -> putExtra(key, value)
                is Bundle -> putExtra(key, value)
                is String -> putExtra(key, value)
                is IntArray -> putExtra(key, value)
                is ByteArray -> putExtra(key, value)
                is CharArray -> putExtra(key, value)
                is LongArray -> putExtra(key, value)
                is FloatArray -> putExtra(key, value)
                is Parcelable -> putExtra(key, value)
                is ShortArray -> putExtra(key, value)
                is DoubleArray -> putExtra(key, value)
                is BooleanArray -> putExtra(key, value)
                is CharSequence -> putExtra(key, value)
                is Array<*> -> {
                    when {
                        value.isArrayOf<String>() ->
                            putExtra(key, value as Array<String?>)
                        value.isArrayOf<Parcelable>() ->
                            putExtra(key, value as Array<Parcelable?>)
                        value.isArrayOf<CharSequence>() ->
                            putExtra(key, value as Array<CharSequence?>)
                        else -> putExtra(key, value)
                    }
                }
                is Serializable -> putExtra(key, value)
            }
        }
        return this
    }
}
package com.github.h4de5ing.baseui.base

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.h4de5ing.baseui.SPUtils
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {
    fun getSP(key: String,defaultValue: Any): Any {
        return SPUtils.getSp(this, key, defaultValue)
    }

    fun setSP(key: String, value: Any) {
        SPUtils.setSP(this, key, value)
    }

    fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    fun showSnack(view: View, message: String) {
        runOnUiThread { Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show() }
    }
}
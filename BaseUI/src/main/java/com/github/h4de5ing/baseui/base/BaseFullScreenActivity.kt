package com.github.h4de5ing.baseui.base

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.View

//全屏BaseActivity
open class BaseFullScreenActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFormat(PixelFormat.TRANSLUCENT)
        try {
            if (Build.VERSION.SDK_INT >= 11) {
                window.setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                )
            }
            hideBottomUIMenu()
        } catch (e: Exception) {
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT in 12..18) { // lower api
            val v = window.decorView
            v.systemUiVisibility = View.GONE
        } else {
            //for new api versions.这种方式虽然是官方推荐，但是根本达不到效果
            val decorView = window.decorView
            val uiOptions =
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
        }
    }
}
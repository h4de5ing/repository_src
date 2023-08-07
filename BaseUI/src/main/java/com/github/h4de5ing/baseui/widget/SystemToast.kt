package com.github.h4de5ing.baseui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.*
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.github.h4de5ing.baseui.R
import java.lang.reflect.Field
import java.lang.reflect.Method

class SystemToast(context: Context) {
    private val bkColor = Color.TRANSPARENT
    private val delayLong = 3600
    private val delayShort = 2100
    private var mHide: Method? = null
    private var mNextView: Field? = null
    private var mParams: Field? = null
    private var mRealtimeToast: SystemToast? = null
    private var mShow: Method? = null
    private var mTn: Field? = null
    private var mVerNMR1 = false
    private var isShow = false
    private var mAnimation: Animation? = null
    private var mContext: Context? = context
    private var mDuration = 0
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            this@SystemToast.hide()
        }
    }
    private var mTN: Any? = null
    private var mToast: Toast? = null
    private var mView: View? = null

    @SuppressLint("SoonBlockedPrivateApi")
    private fun initToast() {
        try {
            if (mTn == null) {
                mTn = mToast?.javaClass?.getDeclaredField("mTN")
                mTn?.isAccessible = true
            }
            if (mTN == null) mTn?.apply { this[mToast] }
            if (mShow == null) {
                try {
                    mShow = mTN?.javaClass?.getMethod("show", IBinder::class.java)
                    mVerNMR1 = true
                } catch (_: Exception) {
                }
                if (mShow == null) {
                    mShow = mTN?.javaClass?.getMethod("show", *arrayOfNulls(0))
                }
            }
            if (mHide == null) {
                mHide = mTN?.javaClass?.getMethod("hide", *arrayOfNulls(0))
            }
            if (mNextView == null) {
                mNextView = mTN?.javaClass?.getDeclaredField("mNextView")
                mNextView?.isAccessible = true
            }
            if (mParams == null) {
                mParams = mTN?.javaClass?.getDeclaredField("mParams")
                mParams?.isAccessible = true
            }
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }

    private fun setWindowType(type: Int) {
        try {
            mParams?.apply { (this[mTN] as WindowManager.LayoutParams).type = type }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAlwaysOnTop() = setWindowType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)

    private fun setDrawOverStatusBar() =
        setWindowType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR)

    private fun handleCancel() {
        mHandler.removeMessages(0)
        mHandler.sendEmptyMessageDelayed(
            0,
            (if (mDuration == 0) delayShort else delayLong).toLong()
        )
    }

    fun disableStartAnimation() {
        if (mAnimation == null) mAnimation = mView?.animation
        mView?.clearAnimation()
        mView?.animation = null
    }

    fun setText(newTxt: String?) {
        mToast?.setText(newTxt)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setTextAppearance(resId: Int) {
        ((mView as ViewGroup?)?.getChildAt(0) as TextView).setTextAppearance(resId)
    }

    fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        mToast?.setGravity(gravity, xOffset, yOffset)
    }

    private fun setBackgroundColor(color: Int) {
        mView?.background?.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    fun show() {
        try {
            if (!isShow) {
                mNextView?.apply { this[mTN] = mView }
                if (mVerNMR1) mShow?.invoke(mTN, mView?.windowToken)
                else mShow?.invoke(mTN, *arrayOfNulls(0))
                isShow = true
                handleCancel()
                if (mAnimation != null) {
                    mView?.animation = mAnimation
                    return
                }
                return
            }
            handleCancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hide() {
        if (isShow) {
            mRealtimeToast = null
            mHandler.removeMessages(0)
            try {
                mHide?.invoke(mTN, *arrayOfNulls(0))
                isShow = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun makeText(context: Context, resId: Int, duration: Int): SystemToast? =
        makeText(context, context.resources.getText(resId), duration)

    fun makeText(context: Context, text: CharSequence?, duration: Int): SystemToast? {
        if (mRealtimeToast != null) {
            if (mRealtimeToast?.mContext === context) {
                mRealtimeToast?.mToast?.setText(text)
                mRealtimeToast?.mDuration = duration
            } else mRealtimeToast?.hide()
        }
        if (mRealtimeToast == null) {
            try {
                mRealtimeToast = SystemToast(context)
                mRealtimeToast?.mToast = Toast.makeText(context, text, duration)
                val view = View.inflate(context, R.layout.toast, null)
                val textView = view.findViewById<TextView>(R.id.tv)
                textView.text = text
                mRealtimeToast?.mView = view
                mRealtimeToast?.mDuration = duration
                mRealtimeToast?.setBackgroundColor(bkColor)
                mRealtimeToast?.initToast()
                mRealtimeToast?.setDrawOverStatusBar()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return mRealtimeToast
    }
}
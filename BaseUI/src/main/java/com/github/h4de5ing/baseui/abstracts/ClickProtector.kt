package com.github.h4de5ing.baseui.abstracts

import android.view.View

/**
 * 连点保护器，防止按钮多次被点击
 *
 *button.setOnClickListener(object : ClickProtector() {
 *
 * override fun onRealClick(v: View) {
 *
 * Log.d("gh0st", "${++count}")
 *
 * }
 * }.delay(1000))
 *
 *
 *
 */
abstract class ClickProtector : View.OnClickListener {
    private var delay = 0L
    abstract fun onRealClick(v: View)
    fun delay(delay: Long): ClickProtector {
        this.delay = delay
        return this
    }

    override fun onClick(v: View) {
        val key = v.id
        val lastTime = v.getTag(key)
        if (lastTime != null &&
            (System.currentTimeMillis() - lastTime.toString().toLong()) < delay
        ) return
        onRealClick(v)
        v.setTag(key, System.currentTimeMillis())
    }
}
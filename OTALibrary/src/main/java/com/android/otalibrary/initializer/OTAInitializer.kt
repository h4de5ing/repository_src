package com.android.otalibrary.initializer

import android.content.Context
import androidx.startup.Initializer
import com.android.otalibrary.CrashHandler
import com.android.otalibrary.initialize
import com.android.otalibrary.isDebug

class OTAInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        if (!isDebug()) CrashHandler.getInstance().init(context)
        initialize(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
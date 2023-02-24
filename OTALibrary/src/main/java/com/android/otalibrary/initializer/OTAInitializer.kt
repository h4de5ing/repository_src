package com.android.otalibrary.initializer

import android.content.Context
import androidx.startup.Initializer
import com.android.otalibrary.initialize

class OTAInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        initialize(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
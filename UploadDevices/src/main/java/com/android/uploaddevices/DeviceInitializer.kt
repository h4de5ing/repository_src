package com.android.uploaddevices

import android.content.Context
import androidx.startup.Initializer

class DeviceInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        initialize(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
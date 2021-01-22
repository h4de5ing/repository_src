package com.github.h4de5ing.baseui

import android.os.Bundle

//带返回的BaseActivity
open class ReturnBaseActivity : BaseActivity() {
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true)
            supportActionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
}
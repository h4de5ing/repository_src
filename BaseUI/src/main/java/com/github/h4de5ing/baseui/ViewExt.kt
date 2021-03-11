package com.github.h4de5ing.baseui

import android.app.Activity
import android.content.Intent

//常见view扩展封装
fun Activity.startActivity(activityClass: Class<*>?) = startActivity(Intent(this, activityClass))
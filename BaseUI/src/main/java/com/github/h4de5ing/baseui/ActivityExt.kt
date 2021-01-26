package com.github.h4de5ing.baseui

import android.app.Activity
import android.content.Intent


fun Activity.startActivity(activityClass: Class<*>?) = startActivity(Intent(this, activityClass))
package com.android.otalibrary.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.otalibrary.*
import com.github.h4de5ing.netlib.HttpRequest
import java.io.File

class OTAActivity : AppCompatActivity() {
    private var spf: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        overridePendingTransition(0, 0)
        setWindowSize()
        spf = PreferenceManager.getDefaultSharedPreferences(this)
        val progressBar = findViewById<TextView>(R.id.tv_description)
        val ibClose = findViewById<ImageButton>(R.id.ib_close)
        ibClose.setOnClickListener {
            spf?.edit()?.putLong("today", System.currentTimeMillis())?.apply()
            finish()
        }
        val btnUpdate = findViewById<Button>(R.id.btn_update)
        btnUpdate.setOnClickListener {
            if (isDownloaded) {
                installApp(localAPkPath)
            } else {
                Thread {
                    try {
                        HttpRequest.downloadFile(apkUrl, localAPkPath, object :
                            HttpRequest.FileDownloadComplete {
                            override fun progress(progress: Long) {
//                                println("下载进度 $progress")
                                runOnUiThread { progressBar.text = "${progress}%" }
                            }

                            override fun complete(file: File?) {
                                isDownloaded = true
                                runOnUiThread {
                                    btnUpdate.text = getString(R.string.app_update_click_hint)
                                }
                            }
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle.text = String.format(
            resources.getString(R.string.app_update_dialog_new), versionCode
        )
    }

    private fun setWindowSize() {
        val attributes = window.attributes
        attributes.width = (resources.displayMetrics.widthPixels * 0.75f).toInt()
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.gravity = Gravity.CENTER
        window.attributes = attributes
    }
}
package com.android.otalibrary.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.otalibrary.R
import com.android.otalibrary.apkUrl
import com.android.otalibrary.getAPKFilePackageName
import com.android.otalibrary.getAPKFileVersionCode
import com.android.otalibrary.installApp
import com.android.otalibrary.isDownloaded
import com.android.otalibrary.isUpdate
import com.android.otalibrary.localAPkPath
import com.android.otalibrary.logD
import com.android.otalibrary.targetVersion
import com.android.otalibrary.versionCode
import com.github.h4de5ing.netlib.downloadFile
import java.io.File

class OTAActivity : AppCompatActivity() {
    private var spf: SharedPreferences? = null

    @SuppressLint("PackageManagerGetSignatures")
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
            isUpdate = false
            finish()
        }
        val checkBox = findViewById<CheckBox>(R.id.check_box)
        findViewById<LinearLayout>(R.id.ignore_check_box).setOnClickListener {
            checkBox.isChecked = !checkBox.isChecked
            spf?.edit()?.putBoolean("ignore", checkBox.isChecked)?.apply()
            spf?.edit()?.putLong("versionCode", targetVersion)?.apply()
        }
        val btnUpdate = findViewById<Button>(R.id.btn_update)
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle.text =
            String.format(resources.getString(R.string.app_update_dialog_new), versionCode)
        try {
            if (File(localAPkPath).exists() && getAPKFilePackageName(localAPkPath) == packageName && getAPKFileVersionCode(
                    localAPkPath
                ) == targetVersion.toInt()
            ) {
                progressBar.text = getString(R.string.already_exist)
                btnUpdate.text = getString(R.string.app_update_click_hint)
                isDownloaded = true
            }
        } catch (e: Exception) {
            "${e.printStackTrace()}".logD()
        }
        btnUpdate.setOnClickListener {
            if (isDownloaded) {
                installApp(localAPkPath) {
                    runOnUiThread {
                        if (it == 0) Toast.makeText(
                            this,
                            getString(R.string.install_success),
                            Toast.LENGTH_LONG
                        ).show()
                        else Toast.makeText(
                            this,
                            getString(R.string.install_fail),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                File(localAPkPath).delete()
                finish()
            } else {
                Thread {
                    try {
                        runOnUiThread {
                            progressBar.text = getString(R.string.app_update_download) + "0%"
                            btnUpdate.isEnabled = false
                        }
                        downloadFile(
                            apkUrl,
                            localAPkPath,
                            progress = { it, message ->
                                runOnUiThread {
                                    progressBar.text =
                                        getString(R.string.app_update_download) + "${it}%"
                                }
                            },
                            error = {
                                runOnUiThread {
                                    btnUpdate.isEnabled = true
                                    progressBar.text = getString(R.string.network_disable)
                                    btnUpdate.text = getString(R.string.try_again)
                                }
                            },
                            complete = {
                                isDownloaded = true
                                runOnUiThread {
                                    btnUpdate.isEnabled = true
                                    progressBar.text =
                                        getString(R.string.app_update_download_completed)
                                    btnUpdate.text = getString(R.string.app_update_click_hint)
                                }
                            })
                    } catch (e: Exception) {
                        runOnUiThread { btnUpdate.isEnabled = true }
                        e.printStackTrace()
                    }
                }.start()
            }
        }
    }

    private fun setWindowSize() {
        val attributes = window.attributes
        attributes.width = (resources.displayMetrics.widthPixels * 0.75f).toInt()
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.gravity = Gravity.CENTER
        window.attributes = attributes
    }
}
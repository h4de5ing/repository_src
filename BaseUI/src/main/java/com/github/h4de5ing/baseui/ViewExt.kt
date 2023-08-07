package com.github.h4de5ing.baseui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.chip.Chip

//常见view扩展封装
fun Spinner.selected(selected: ((Int) -> Unit)) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            selected(position)
        }
    }
}

fun CompoundButton.changed(change: ((Boolean) -> Unit)) =
    this.setOnCheckedChangeListener { _, isChecked -> change(isChecked) }

/**
 * 这个方式，如果是代码调用是不会响应回调
 */
fun CompoundButton.changedNoIsPressed(change: ((Boolean) -> Unit)) =
    this.setOnCheckedChangeListener { view, isChecked ->
        //如果没有按下，则认为是代码设置的，直接拦截
        if (view.isPressed) return@setOnCheckedChangeListener
        change(isChecked)
    }

fun SeekBar.change(selected: ((Int) -> Unit)) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            selected(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    })
}

fun Chip.changed(change: ((Boolean) -> Unit)) =
    this.setOnCheckedChangeListener { _, isChecked -> change(isChecked) }

fun RadioGroup.checkedChange(checked: ((Int) -> Unit)) {
    this.setOnCheckedChangeListener { _, checkedId ->
        checked(checkedId)
    }
}

class EditTextWatcher(private val change: (String) -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        change.invoke(s.toString())
    }
}

fun TextView.textChanged(change: ((String) -> Unit)) =
    addTextChangedListener(EditTextWatcher(change))

fun CheckBox.checkedChange(change: ((Boolean) -> Unit)) {
    this.setOnCheckedChangeListener { _, isChecked ->
        change(isChecked)
    }
}

fun <T> androidx.appcompat.app.AlertDialog.Builder.changed(
    items: MutableList<T>,
    title: String,
    change: ((T) -> Unit)
) {
    setTitle(title)
    val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, items)
    setSingleChoiceItems(
        adapter, 0
    ) { dialog, which ->
        change(items[which])
        dialog.dismiss()
    }
    setCancelable(false)
    //setNegativeButton(android.R.string.cancel, null)
    create().show()
}

//弹出确认按钮
fun alertConfirm(context: Context, message: String, block: ((Boolean) -> Unit)) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(message)
    builder.setNegativeButton(android.R.string.cancel) { _, _ -> block(false) }
    builder.setPositiveButton(android.R.string.ok) { _, _ -> block(true) }
    builder.create().show()
}

fun alertConfirmJustOk(context: Context, message: String, block: ((Boolean) -> Unit)) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(message)
    builder.setPositiveButton(android.R.string.ok) { _, _ -> block(true) }
    builder.create().show()
}

/**
 * 设置View为可见
 */
fun View.visible() = run { visibility = View.VISIBLE }

/**
 * 设置View不可见，占用布局空间
 */
fun View.invisible() = run { visibility = View.INVISIBLE }

/**
 * 设置View不可见，不占用位置
 */
fun View.gone() = run { visibility = View.GONE }
fun View.isVisible(isShowed: Boolean) =
    run { if (isShowed) visibility = View.VISIBLE else View.GONE }

/**
 * 设置View的黑色主题
 */
fun View.darkTheme() {
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    setLayerType(View.LAYER_TYPE_HARDWARE, paint)
}

/**
 * 重置View的主题
 */
fun View.resetTheme() = this.setLayerType(View.LAYER_TYPE_HARDWARE, Paint())

fun Activity.toast(text: CharSequence): Unit = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
fun Activity.longToast(text: CharSequence): Unit =
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
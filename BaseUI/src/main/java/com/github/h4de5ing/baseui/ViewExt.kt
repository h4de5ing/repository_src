package com.github.h4de5ing.baseui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatSpinner
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

//常见view扩展封装
fun Activity.startActivity(activityClass: Class<*>?) = startActivity(Intent(this, activityClass))
fun AppCompatSpinner.selected(selected: ((Int) -> Unit)) {
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

fun SwitchMaterial.changed(change: ((Boolean) -> Unit)) =
    this.setOnCheckedChangeListener { _, isChecked -> change(isChecked) }

/**
 * 这个方式，如果是代码调用是不会响应回调
 */
fun SwitchMaterial.changedNoIsPressed(change: ((Boolean) -> Unit)) =
    this.setOnCheckedChangeListener { view, isChecked ->
        //如果没有按下，则认为是代码设置的，直接拦截
        if (view.isPressed) return@setOnCheckedChangeListener
        change(isChecked)
    }

fun Chip.changed(change: ((Boolean) -> Unit)) =
    this.setOnCheckedChangeListener { _, isChecked -> change(isChecked) }

fun RadioGroup.checkedChange(checked: ((Int) -> Unit)) {
    this.setOnCheckedChangeListener { _, checkedId ->
        checked(checkedId)
    }
}

fun TextInputEditText.textChanged(change: ((CharSequence) -> Unit)) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) =
            change.invoke(s)

        override fun afterTextChanged(s: Editable?) = Unit
    })
}

fun MaterialCheckBox.checkedChange(change: ((Boolean) -> Unit)) {
    this.setOnCheckedChangeListener { _, isChecked ->
        change(isChecked)
    }
}

fun <T> androidx.appcompat.app.AlertDialog.Builder.changed(
    items: MutableList<T>,
    title: String,
    change: ((T) -> Unit)
) {
    this.setTitle(title)
    val adapter = ArrayAdapter(this.context, android.R.layout.simple_list_item_1, items)
    this.setSingleChoiceItems(
        adapter, 0
    ) { dialog, which ->
        change(items[which])
        dialog.dismiss()
    }
    this.setCancelable(false)
    //this.setNegativeButton(android.R.string.cancel, null)
    this.create().show()
}

//弹出确认按钮
fun alertConfirm(context: Context, message: String, block: ((Boolean) -> Unit)) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(message)
    builder.setNegativeButton(android.R.string.cancel) { _, _ -> block(false) }
    builder.setPositiveButton(android.R.string.ok) { _, _ -> block(true) }
    builder.create().show()
}

//判断任何对象是否为空
//fun Any?.isNotEmpty(): Boolean = this != null

/**
 * 设置View为可见
 */
fun View.visible() = run { this.visibility = View.VISIBLE }

/**
 * 设置View不可见，占用布局空间
 */
fun View.invisible() = run { this.visibility = View.INVISIBLE }

/**
 * 设置View不可见，不占用位置
 */
fun View.gone() = run { this.visibility = View.GONE }
fun View.isVisible(isShowed: Boolean) =
    run { if (isShowed) this.visibility = View.VISIBLE else View.GONE }

/**
 * 设置View的黑色主题
 */
fun View.darkTheme() {
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    this.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
}

/**
 * 重置View的主题
 */
fun View.resetTheme() = this.setLayerType(View.LAYER_TYPE_HARDWARE, Paint())
package com.github.h4de5ing.baseui

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
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

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            change(s)
        }

        override fun afterTextChanged(s: Editable?) = Unit
    })
}

fun MaterialCheckBox.checkedChange(change: ((Boolean) -> Unit)) {
    this.setOnCheckedChangeListener { _, isChecked ->
        change(isChecked)
    }
}

//判断任何对象是否为空
fun Any?.isNotEmpty(): Boolean = this != null
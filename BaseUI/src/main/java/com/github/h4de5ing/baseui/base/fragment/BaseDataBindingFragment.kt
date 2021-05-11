package com.github.h4de5ing.baseui.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseDataBindingFragment<DB : ViewDataBinding> : Fragment() {
    lateinit var binding: DB
    abstract fun layoutId(): Int
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(
                requireActivity(),
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
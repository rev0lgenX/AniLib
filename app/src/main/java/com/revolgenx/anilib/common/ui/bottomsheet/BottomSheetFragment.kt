package com.revolgenx.anilib.common.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.revolgenx.anilib.R

abstract class BottomSheetFragment<VB : ViewBinding> : BottomSheetDialogFragment() {
    open lateinit var windowContext: Context

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_App_BottomSheetDialog
    }

    private var _binding: VB? = null
    protected val binding: VB get()= _binding!!


    abstract fun bindView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindView(inflater, container, savedInstanceState)
        return binding.root
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    fun show(context: Context) {
        windowContext = context
        show()
    }

    fun show() {
        windowContext.let { ctx ->
            when (ctx) {
                is FragmentActivity -> show(ctx.supportFragmentManager, TAG)
                is AppCompatActivity -> show(ctx.supportFragmentManager, TAG)
                is Fragment -> show(ctx.childFragmentManager, TAG)
                is PreferenceFragmentCompat -> show(ctx.childFragmentManager, TAG)
                else -> throw IllegalStateException("Context has no window attached.")
            }
        }
    }
}
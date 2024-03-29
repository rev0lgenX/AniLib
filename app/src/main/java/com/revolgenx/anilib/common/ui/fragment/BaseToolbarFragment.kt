package com.revolgenx.anilib.common.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout
import com.pranavpandey.android.dynamic.support.widget.DynamicToolbar
import com.revolgenx.anilib.databinding.BaseToolbarFragmentLayoutBinding

abstract class BaseToolbarFragment<T : ViewBinding> : BaseLayoutFragment<T>() {
    private lateinit var toolbar: DynamicToolbar
    override var setHomeAsUp: Boolean = true

    open val noScrollToolBar = false
    open val toolbarColorType: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val b = BaseToolbarFragmentLayoutBinding.inflate(inflater, container, false)
        toolbar = b.toolbarLayout.dynamicToolbar

        if (noScrollToolBar) {
            (toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        }

        val content = super.onCreateView(inflater, container, savedInstanceState)
        b.toolbarContainerLayout.addView(content)
        return b.root
    }

    override fun getBaseToolbar(): Toolbar {
        return toolbar
    }

}
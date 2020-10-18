package com.revolgenx.anilib.fragment.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

abstract class BaseTransitiveLayoutFragment : BaseLayoutFragment() {
    abstract val toolbar: Toolbar

    protected var toolbarTitle: String = ""
        set(value) {
            toolbar.title = value
            field = value
        }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        super.onActivityCreated(savedInstanceState)
    }

}
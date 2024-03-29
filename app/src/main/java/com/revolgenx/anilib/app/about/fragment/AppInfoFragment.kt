package com.revolgenx.anilib.app.about.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.revolgenx.anilib.common.ui.fragment.BaseLayoutFragment
import com.revolgenx.anilib.databinding.AppInfoFragmentLayoutBinding

class AppInfoFragment : BaseLayoutFragment<AppInfoFragmentLayoutBinding>() {
    override fun bindView(inflater: LayoutInflater, parent: ViewGroup?): AppInfoFragmentLayoutBinding {
        return AppInfoFragmentLayoutBinding.inflate(inflater, parent, false)
    }
}

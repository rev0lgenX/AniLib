package com.revolgenx.anilib.app.setting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pranavpandey.android.dynamic.theme.Theme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.preference.*
import com.revolgenx.anilib.common.ui.fragment.BaseToolbarFragment
import com.revolgenx.anilib.databinding.SettingFragmentLayoutBinding
import com.revolgenx.anilib.common.event.*
import com.revolgenx.anilib.util.loginContinue

class SettingFragment : BaseToolbarFragment<SettingFragmentLayoutBinding>() {

    override val titleRes: Int = R.string.settings
    override val toolbarColorType: Int = Theme.ColorType.BACKGROUND
    override fun bindView(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): SettingFragmentLayoutBinding {
        return SettingFragmentLayoutBinding.inflate(inflater, parent, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.applicationSettingIv.setOnClickListener {
            OpenSettingEvent(SettingEventTypes.APPLICATION).postEvent
        }

        binding.themeSettingIv.setOnClickListener {
            OpenSettingEvent(SettingEventTypes.THEME).postEvent
        }

        if (loggedIn()) {
            binding.notificationSetting.setOnClickListener {
                OpenSettingEvent(SettingEventTypes.NOTIFICATION).postEvent
            }

            binding.mediaSettingIv.setOnClickListener {
                OpenSettingEvent(SettingEventTypes.MEDIA_SETTING).postEvent
            }

            binding.listSettingIv.setOnClickListener {
                OpenSettingEvent(SettingEventTypes.MEDIA_LIST).postEvent
            }
        } else {
            binding.notificationSettingLayout.visibility = View.GONE
            binding.mediaListSettingLayout.visibility = View.GONE
            binding.mediaSettingLayout.visibility = View.GONE
        }


        binding.filterSetting.setOnClickListener {
            OpenSettingEvent(SettingEventTypes.CUSTOMIZE_FILTER).postEvent
        }

        binding.widgetSetting.setOnClickListener {
            OpenSettingEvent(SettingEventTypes.AIRING_WIDGET).postEvent
        }

        binding.translationSetting.setOnClickListener {
            OpenSettingEvent(SettingEventTypes.TRANSLATION).postEvent
        }

        loginContinue(false) {
            binding.aboutItemView.visibility = View.VISIBLE
            binding.aboutItemView.setOnClickListener {
                OpenSettingEvent(SettingEventTypes.ABOUT).postEvent
            }
        }

    }

}

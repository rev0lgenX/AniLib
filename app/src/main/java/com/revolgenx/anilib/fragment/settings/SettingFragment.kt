package com.revolgenx.anilib.fragment.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.revolgenx.anilib.R
import com.revolgenx.anilib.activity.AboutActivity
import com.revolgenx.anilib.activity.ContainerActivity
import com.revolgenx.anilib.activity.ToolbarContainerActivity
import com.revolgenx.anilib.dialog.ReleaseInfoDialog
import com.revolgenx.anilib.fragment.base.BaseToolbarFragment
import com.revolgenx.anilib.fragment.base.ParcelableFragment
import com.revolgenx.anilib.fragment.notification.NotificationSettingFragment
import kotlinx.android.synthetic.main.setting_fragment_layout.*

class SettingFragment : BaseToolbarFragment() {

    override val title: Int = R.string.settings

    override val contentRes: Int by lazy {
        R.layout.setting_fragment_layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).also {
            it.supportActionBar?.setDisplayShowHomeEnabled(true)
            it.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        themeItemView.setOnClickListener {
            ContainerActivity.openActivity(
                requireContext(),
                ParcelableFragment(
                    ThemeControllerFragment::class.java,
                    null
                )
            )
        }

        notificationSetting.setOnClickListener {
            ToolbarContainerActivity.openActivity(
                requireContext(),
                ParcelableFragment(
                    NotificationSettingFragment::class.java,
                    null
                )
            )
        }

        customizeFilterItemView.setOnClickListener {
            ToolbarContainerActivity.openActivity(
                requireContext(),
                ParcelableFragment(
                    CustomizeFilterFragment::class.java,
                    null
                )
            )
        }

        aboutItemView.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
        whatsNew.setOnClickListener {
            ReleaseInfoDialog().show(childFragmentManager, ReleaseInfoDialog.tag)
        }
    }


}
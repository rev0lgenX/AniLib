package com.revolgenx.anilib.ui.adapter.binder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pranavpandey.android.dynamic.support.recyclerview.binder.DynamicRecyclerViewBinder
import com.pranavpandey.android.dynamic.support.view.DynamicHeader
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ui.adapter.AppInfoAdapter
import com.revolgenx.anilib.util.openLink

class AboutOtherInfoBinder(binderAdapter: AppInfoAdapter) :
    DynamicRecyclerViewBinder<AboutOtherInfoBinder.ViewHolder>(binderAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.about_other_info, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        // Return item count.
        return 1
    }


    /**
     * Holder class to hold view holder item.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val privacyPolicy: DynamicHeader = view.findViewById(R.id.privacyPolicy)
        private val termsAndCondition: DynamicHeader = view.findViewById(R.id.termsAndCondition)

        init {
            privacyPolicy.setOnClickListener {
                it.context.openLink(it.context.getString(R.string.privacy_policy_url))
            }

            termsAndCondition.setOnClickListener {
                it.context.openLink(it.context.getString(R.string.terms_and_condition_url))
            }


        }
    }

}
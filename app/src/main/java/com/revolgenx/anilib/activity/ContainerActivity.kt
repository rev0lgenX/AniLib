package com.revolgenx.anilib.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import com.revolgenx.anilib.R
import com.revolgenx.anilib.common.ui.fragment.BaseFragment
import com.revolgenx.anilib.common.ui.fragment.ParcelableFragment
import com.revolgenx.anilib.databinding.ContainerActivityBinding

class ContainerActivity : BaseDynamicActivity<ContainerActivityBinding>() {

    companion object {
        const val fragmentContainerKey = "fragment_container_key"

        fun openActivity(
            context: Context,
            parcelableFragment: ParcelableFragment,
            option: ActivityOptionsCompat? = null
        ) {
            context.startActivity(Intent(context, ContainerActivity::class.java).also {
                it.putExtra(fragmentContainerKey, parcelableFragment)
            }, option?.toBundle())
        }

    }


    override fun bindView(inflater: LayoutInflater, parent: ViewGroup?): ContainerActivityBinding {
        return ContainerActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parcel:ParcelableFragment =
            intent.getParcelableExtra(fragmentContainerKey)
                ?: return

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            @Suppress("UNCHECKED_CAST")
            transaction.replace(R.id.fragmentContainer, (parcel.clzz as Class<BaseFragment>).newInstance().apply {
                this.arguments = parcel.bundle
            }).commitNow()
        }
    }

    override fun finishAfterTransition() {
        finish()
    }


}
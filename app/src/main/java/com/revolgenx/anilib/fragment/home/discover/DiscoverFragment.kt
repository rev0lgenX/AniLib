package com.revolgenx.anilib.fragment.home.discover

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.revolgenx.anilib.R
import com.revolgenx.anilib.activity.ToolbarContainerActivity
import com.revolgenx.anilib.dialog.HomeOrderDialog
import com.revolgenx.anilib.event.BrowseEvent
import com.revolgenx.anilib.fragment.base.ParcelableFragment
import com.revolgenx.anilib.fragment.notification.NotificationFragment
import com.revolgenx.anilib.meta.UserMeta
import com.revolgenx.anilib.preference.loggedIn
import com.revolgenx.anilib.preference.userId

class DiscoverFragment : DiscoverReviewFragment() {

    override fun onResume() {
        super.onResume()
        invalidateOptionMenu()
        setHasOptionsMenu(true)
        updateToolbarTitle()
    }

    private fun updateToolbarTitle() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setTitle(R.string.app_name)
            it.supportActionBar?.setSubtitle(0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.discover_fragment_menu, menu)
        menu.findItem(R.id.notificationMenu).isVisible = requireContext().loggedIn()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchMenu -> {
                BrowseEvent().postEvent
                true
            }
            R.id.notificationMenu -> {
                ToolbarContainerActivity.openActivity(
                    requireContext(),
                    ParcelableFragment(
                        NotificationFragment::class.java,
                        bundleOf(
                            UserMeta.userMetaKey to UserMeta(
                                requireContext().userId(),
                                null,
                                true
                            )
                        )
                    )
                )
                true
            }
            R.id.orderMenu->{
                HomeOrderDialog().show(childFragmentManager, HomeOrderDialog::class.java.simpleName)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

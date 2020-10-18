package com.revolgenx.anilib.fragment.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEachIndexed
import androidx.core.view.iterator
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.revolgenx.anilib.R
import com.revolgenx.anilib.adapter.makePagerAdapter
import com.revolgenx.anilib.fragment.base.BaseTransitiveLayoutFragment
import com.revolgenx.anilib.fragment.home.discover.DiscoverFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.discover_viewpager_fragment.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class DiscoverContainerFragment : BaseTransitiveLayoutFragment() {
    override val layoutRes: Int = R.layout.discover_viewpager_fragment

    override val toolbar: Toolbar
        get() = dynamicToolbar

    private lateinit var adapter: FragmentPagerAdapter

    private val discoverFragments by lazy{
        newInstances(
            listOf(
                DiscoverFragment::class.java,
                SeasonFragment::class.java,
                RecommendationFragment::class.java
            )
        )
    }

    companion object {
        fun newInstance() = DiscoverContainerFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomNav.setBackgroundColor(DynamicTheme.getInstance().get().backgroundColor)
        themeBottomNavigation()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initListener()
        adapter = makePagerAdapter(
            discoverFragments
        )
        mainViewPager.adapter = adapter
        mainViewPager.offscreenPageLimit = 3
    }

    private fun initListener() {

        val toggle = ActionBarDrawerToggle(
            requireActivity(),
            requireActivity().drawerLayout,
            dynamicToolbar,
            R.string.nav_open,
            R.string.nav_close
        )

        requireActivity().drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        mainViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                bottomNav.menu.iterator().forEach {
                    it.isChecked = false
                }
                bottomNav.menu.getItem(position).isChecked = true
            }
        })

        bottomNav.setOnNavigationItemSelectedListener {
            bottomNav.menu.forEachIndexed { index, item ->
                if (it == item) {
                    mainViewPager.setCurrentItem(index, true)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    private fun themeBottomNavigation() {
        bottomNav.color = DynamicTheme.getInstance().get().primaryColor
        bottomNav.textColor = DynamicTheme.getInstance().get().accentColor
    }

}
package com.revolgenx.anilib.common.ui.screen

import cafe.adriel.voyager.androidx.AndroidScreenLifecycleOwner
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleOwner
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleProvider
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.tab.Tab

abstract class BaseTabScreen: Tab, ScreenLifecycleProvider {
    override val key: ScreenKey = uniqueScreenKey
    override fun getLifecycleOwner(): ScreenLifecycleOwner = AndroidScreenLifecycleOwner.get(this)
}
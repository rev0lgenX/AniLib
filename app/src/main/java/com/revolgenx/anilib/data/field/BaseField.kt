package com.revolgenx.anilib.data.field

import android.content.Context
import com.revolgenx.anilib.common.preference.canShowAdult
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseField<T>() : KoinComponent {
    protected val context: Context by inject()

    abstract fun toQueryOrMutation(): T

    val canShowAdult: Boolean
        get() = canShowAdult(context)

    companion object {
        const val PER_PAGE = 20
    }
}

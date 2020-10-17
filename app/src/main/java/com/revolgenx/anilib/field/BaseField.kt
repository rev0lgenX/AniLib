package com.revolgenx.anilib.field

import android.content.Context
import com.revolgenx.anilib.preference.canShowAdult
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseField<T>() : KoinComponent {
    protected val context: Context by inject()

    abstract fun toQueryOrMutation(): T

    val canShowAdult: Boolean
        get() = canShowAdult(context)

    companion object {
        const val PER_PAGE = 14
    }
}

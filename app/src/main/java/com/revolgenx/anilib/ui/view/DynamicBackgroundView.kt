package com.revolgenx.anilib.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.pranavpandey.android.dynamic.utils.DynamicDrawableUtils
import com.revolgenx.anilib.R

class DynamicBackgroundView : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, def: Int) : super(
        context,
        attributeSet,
        def
    ) {
        ContextCompat.getDrawable(context, R.drawable.ic_color_stick) !!.let {
            background = DynamicDrawableUtils.colorizeDrawable(it, DynamicTheme.getInstance().get().accentColor)
        }
    }
}
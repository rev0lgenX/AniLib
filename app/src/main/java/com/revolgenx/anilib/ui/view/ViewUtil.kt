package com.revolgenx.anilib.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.pranavpandey.android.dynamic.toasts.internal.ToastCompat
import com.revolgenx.anilib.R
import kotlinx.android.synthetic.main.dynamic_toast_view_layout.view.*

private val tintSurface by lazy {
    DynamicTheme.getInstance().get().tintSurfaceColor
}

fun Fragment.makeToast(
    @StringRes str: Int? = null,
    msg: String? = null,
    @DrawableRes icon: Int? = null
) {
    requireContext().makeToast(str, msg, icon)
}

fun Context.makeToast(
    @StringRes str: Int? = null,
    msg: String? = null,
    @DrawableRes icon: Int? = null
) {
    makeDynamicToastView(this, str?.let { getString(it) } ?: msg, icon)
}


fun makeDynamicToastView(
    context: Context,
    msg: String? = null,
    @DrawableRes drawable: Int? = null
) {
    val toastView =
        LayoutInflater.from(context).inflate(
            R.layout.dynamic_toast_view_layout, LinearLayout(
                context
            ), false
        )
    toastView.apply {
        drawable?.let {
            toastIconIv.visibility = View.VISIBLE
            toastIconIv.setImageResource(drawable)
            toastIconIv.setColorFilter(tintSurface)
        }
        toastIconTv.text = msg
    }
    ToastCompat(context, Toast(context)).apply {
        view = toastView
        duration = Toast.LENGTH_SHORT
        show()
    }

}
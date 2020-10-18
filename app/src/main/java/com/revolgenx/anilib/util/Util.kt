package com.revolgenx.anilib.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.pranavpandey.android.dynamic.support.widget.DynamicSpinner
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.pranavpandey.android.dynamic.utils.DynamicLinkUtils
import com.pranavpandey.android.dynamic.utils.DynamicUnitUtils
import com.revolgenx.anilib.R
import com.revolgenx.anilib.fragment.base.BaseFragment
import com.revolgenx.anilib.type.MediaSeason
import com.revolgenx.anilib.ui.view.makeToast
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.greenrobot.eventbus.EventBus
import org.ocpsoft.prettytime.PrettyTime
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import java.util.*


const val COLLAPSED = 0
const val EXPANDED = 1


fun getSeasonFromMonth(monthOfYear: Int): MediaSeason {
    monthOfYear.let {
        return if (it == 12 || it == 1 || it == 2) {
            MediaSeason.WINTER
        } else if (it == 3 || it == 4 || it == 5) {
            MediaSeason.SPRING
        } else if (it == 6 || it == 7 || it == 8) {
            MediaSeason.SUMMER
        } else
            MediaSeason.FALL
    }
}

private val suffixes: NavigableMap<Long, String> by lazy {
    TreeMap<Long, String>().apply {
        put(1_000L, "k")
        put(1_000_000L, "M")
        put(1_000_000_000L, "G")
        put(1_000_000_000_000L, "T")
        put(1_000_000_000_000_000L, "P")
        put(1_000_000_000_000_000_000L, "E")
    }
}

fun Int.prettyNumberFormat(): String = this.toLong().prettyNumberFormat()

fun Long.prettyNumberFormat(): String { //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
    if (this == Long.MIN_VALUE) return (Long.MIN_VALUE + 1).prettyNumberFormat()
    if (this < 0) return "-" + this.prettyNumberFormat()
    if (this < 1000) return this.toString() //deal with easy case
    val e: Map.Entry<Long, String> = suffixes.floorEntry(this)!!
    val divideBy = e.key
    val suffix = e.value
    val truncated = this / (divideBy / 10) //the number part of the output times 10
    val hasDecimal =
        truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
    return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
}


fun <T> T.registerForEvent() {
    val bus = EventBus.getDefault()
    if (!bus.isRegistered(this))
        bus.register(this)
}

fun <T> T.unRegisterForEvent() {
    val bus = EventBus.getDefault()
    if (bus.isRegistered(this)) {
        bus.unregister(this)
    }
}

suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}


//view
fun dp(dp: Float) = DynamicUnitUtils.convertDpToPixels(dp)

fun sp(sp: Float) = DynamicUnitUtils.convertSpToPixels(sp)


fun TextView.naText(text: String?) {
    this.text = text?.takeIf { it.isNotEmpty() } ?: "?"
}

fun String?.naText() = this.takeIf { it != null && it.isNotEmpty() } ?: "?"

fun String?.getOrDefault() = this ?: ""
fun Int?.getOrDefault() = this ?: 0
fun Double?.getOrDefault() = this ?: 0.0

//fun naText(na: String?) = na.takeIf { it != null && it.isNotEmpty() } ?: "?"


fun DynamicSpinner.onItemSelected(callback: (position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            callback.invoke(position)
        }
    }
}

fun Context.hideKeyboard(view: View) {
    (getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
        view.windowToken,
        0
    )
}

fun Context.openLink(url: String?) {
    try {
        if (url != null)
            startActivity(Intent(Intent.ACTION_VIEW, url.trim().toUri()))
    } catch (e: Exception) {
        makeToast(R.string.no_app_found_to_open)
    }
}

fun Long.prettyTime(): String {
    return PrettyTime().format(DateTimeUtils.toDate(Instant.ofEpochSecond(this)))
}

fun View.string(@StringRes id: Int) = context.getString(id)
fun Context.string(@StringRes id: Int) = getString(id)

fun Context.color(@ColorRes id: Int) = ContextCompat.getColor(this, id)


fun Context.getClipBoardText(): String {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
}

fun Context.copyToClipBoard(str: String?) {
    if (str == null) return
    DynamicLinkUtils.copyToClipboard(this, str, str)
    makeToast(R.string.copied_to_clipboard)
}


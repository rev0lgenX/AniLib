package com.revolgenx.anilib.preference

import android.content.Context
import com.pranavpandey.android.dynamic.preferences.DynamicPreferences
import com.pranavpandey.android.dynamic.utils.DynamicPackageUtils
import com.revolgenx.anilib.R

private const val crashReportKey = "crash_report_key"
private const val versionKey = "versionKey"
private const val updateVersionKey = "update_version_key"

const val languagePrefKey = "application_language_key"

const val DISCOVER_NAV_POS = "0"
const val ANIME_LIST_NAV_POS = "1"
const val MANGA_LIST_NAV_POS = "2"


fun isCrashReportEnabled(context: Context): Boolean {
    return context.getBoolean(crashReportKey, true)
}

fun enableCrashReport(context: Context, enable: Boolean) {
    context.putBoolean(crashReportKey, enable)
}

fun getApplicationLocale(): String {
    return DynamicPreferences.getInstance().load(languagePrefKey, "en")!!
}

fun getVersion(context: Context): String {
    val version = context.getString(versionKey) ?: ""
    context.putString(versionKey, DynamicPackageUtils.getAppVersion(context))
    return version
}

fun getUpdateVersion(context: Context): String {
    return DynamicPreferences.getInstance()
        .load(updateVersionKey, DynamicPackageUtils.getAppVersion(context))!!
}

fun setUpdateVersion(newVersion: String) {
    DynamicPreferences.getInstance().save(updateVersionKey, newVersion)
}

fun setStartNavigation(context: Context, navigation: String) {
    DynamicPreferences.getInstance().save(context.getString(R.string.start_navigation_key), navigation)
}

fun getStartNavigation(context: Context) =
    DynamicPreferences.getInstance().load(context.getString(R.string.start_navigation_key), DISCOVER_NAV_POS)

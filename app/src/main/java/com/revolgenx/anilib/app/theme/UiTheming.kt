package com.revolgenx.anilib.app.theme

import com.llollox.androidtoggleswitch.widgets.ToggleSwitch
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.pranavpandey.android.dynamic.utils.DynamicColorUtils
import com.revolgenx.anilib.util.dp

fun ToggleSwitch.themeIt(){
    val dynamicTheme = DynamicTheme.getInstance().get()
    uncheckedBackgroundColor = ThemeController.lightSurfaceColor()
    checkedBackgroundColor = dynamicTheme.accentColor
    borderRadius = dp(10f).toFloat()
    toggleElevation = dp(1f).toFloat()
    checkedTextColor = DynamicColorUtils.getContrastColor(dynamicTheme.tintAccentColor, dynamicTheme.backgroundColor)
    uncheckedTextColor = dynamicTheme.textPrimaryColor
    reDraw()
}
package com.revolgenx.anilib.app.theme

import com.pranavpandey.android.dynamic.support.theme.DynamicTheme
import com.pranavpandey.android.dynamic.utils.DynamicColorUtils

val dynamicTheme get() = DynamicTheme.getInstance().get()

val contrastAccentWithBg get() =
    DynamicColorUtils.getContrastColor(dynamicAccentColor, dynamicTheme.backgroundColor)

val contrastAccentWithPrimary get() =
    DynamicColorUtils.getContrastColor(dynamicAccentColor, dynamicTheme.primaryColor)

val contrastPrimaryTextColorWithAccent
    get() = DynamicColorUtils.getContrastColor(
        dynamicTextColorPrimary, dynamicAccentColor
    )

val dynamicBackgroundColor get() = dynamicTheme.backgroundColor
val dynamicTintBackgroundColor get() = dynamicTheme.tintBackgroundColor
val dynamicSurfaceColor get() = dynamicTheme.surfaceColor
val dynamicTintSurfaceColor get() = dynamicTheme.tintSurfaceColor
val dynamicTextColorPrimary get() = dynamicTheme.textPrimaryColor
val dynamicTextColorPrimaryInverse get() = dynamicTheme.textPrimaryColorInverse
val dynamicPrimaryColor get() = dynamicTheme.primaryColor
val dynamicPrimaryColorDark get() = dynamicTheme.primaryColorDark
val dynamicAccentColor get() = dynamicTheme.accentColor
val dynamicTintPrimaryColor get() = dynamicTheme.tintPrimaryColor

val dynamicCornerRadius = dynamicTheme.cornerRadius
package org.linphone.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import androidx.core.content.ContextCompat

object ThemeColorWrapper {
    private const val TAG = "[Theme Color Wrapper]"

    /**
     * Gets a color from the theme, with fallback to provider colors if available
     * @param context Application context
     * @param colorAttr Color attribute (e.g., R.attr.color_main1_500)
     * @return Color value
     */
    fun getColor(context: Context, colorAttr: Int): Int {
        // First try to get provider color
        val providerColor = DynamicThemeManager.getProviderColor(context, colorAttr, -1)
        if (providerColor != -1) {
            return providerColor
        }
        
        // Fallback to theme color
        val typedValue = TypedValue()
        context.theme.resolveAttribute(colorAttr, typedValue, true)
        return typedValue.data
    }

    /**
     * Gets a color state list from the theme, with fallback to provider colors if available
     * @param context Application context
     * @param colorAttr Color attribute (e.g., R.attr.color_main1_500)
     * @return ColorStateList
     */
    fun getColorStateList(context: Context, colorAttr: Int): ColorStateList {
        // First try to get provider color
        val providerColor = DynamicThemeManager.getProviderColor(context, colorAttr, -1)
        if (providerColor != -1) {
            return ColorStateList.valueOf(providerColor)
        }
        
        // Fallback to theme color
        val typedValue = TypedValue()
        context.theme.resolveAttribute(colorAttr, typedValue, true)
        return ContextCompat.getColorStateList(context, typedValue.resourceId) ?: ColorStateList.valueOf(Color.BLACK)
    }

    /**
     * Gets a color resource ID from the theme
     * @param context Application context
     * @param colorAttr Color attribute
     * @return Resource ID or -1 if not found
     */
    fun getColorResourceId(context: Context, colorAttr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(colorAttr, typedValue, true)
        return typedValue.resourceId
    }
}

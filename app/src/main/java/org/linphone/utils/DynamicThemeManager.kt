package org.linphone.utils

import android.content.Context
import androidx.annotation.UiThread
import org.linphone.core.tools.Log
import org.linphone.R
import org.voxnode.voxnode.models.LoginResult
import org.voxnode.voxnode.storage.VoxNodeDataManager

object DynamicThemeManager {
    private const val TAG = "[Dynamic Theme Manager]"
    
    private var currentProviderColor1: String? = null
    private var currentProviderColor2: String? = null
    private var colorPalette1: Map<String, Int>? = null
    private var colorPalette2: Map<String, Int>? = null

    /**
     * Applies provider colors from login result to the app theme
     * @param context Application context
     * @param loginResult Login result containing provider colors
     */
    @UiThread
    fun applyProviderColors(context: Context, loginResult: LoginResult) {
         val providerColor1 = loginResult.providerColor1
        // val providerColor1 = "000000"
         val providerColor2 = loginResult.providerColor2
        // val providerColor2 = "000000"

        Log.i("$TAG Applying provider colors - Color1: $providerColor1, Color2: $providerColor2")
        
        // Validate colors
        if (!ColorUtils.isValidHexColor(providerColor1) && !ColorUtils.isValidHexColor(providerColor2)) {
            Log.w("$TAG No valid provider colors found, using default theme")
            return
        }
        
        // Create color palettes
        colorPalette1 = if (ColorUtils.isValidHexColor(providerColor1)) {
            ColorUtils.createColorPalette(providerColor1)
        } else {
            null
        }
        
        colorPalette2 = if (ColorUtils.isValidHexColor(providerColor2)) {
            ColorUtils.createColorPalette(providerColor2)
        } else {
            null
        }
        
        currentProviderColor1 = providerColor1
        currentProviderColor2 = providerColor2
        
        Log.i("$TAG Provider colors applied successfully")
    }

    /**
     * Gets a color from the current provider color palette
     * @param context Application context
     * @param colorAttr Color attribute to get (e.g., R.attr.color_main1_500)
     * @param fallbackColor Fallback color if provider color is not available
     * @return Color value
     */
    fun getProviderColor(context: Context, colorAttr: Int, fallbackColor: Int): Int {
        // Try to get color from provider palette first
        val colorName = getColorNameFromAttr(colorAttr)
        if (colorName != null) {
            val color = getColorFromPalette(colorName)
            if (color != null) {
                return color
            }
        }
        
        // Fallback to default color
        return fallbackColor
    }

    /**
     * Gets a color from the current provider color palette by name
     * @param colorName Color name (e.g., "100", "300", "500", "700")
     * @return Color value or null if not available
     */
    private fun getColorFromPalette(colorName: String): Int? {
        // Try color palette 1 first (primary color)
        colorPalette1?.get(colorName)?.let { return it }
        
        // Try color palette 2 (secondary color)
        colorPalette2?.get(colorName)?.let { return it }
        
        return null
    }

    /**
     * Maps color attributes to color names
     * @param colorAttr Color attribute
     * @return Color name or null if not a provider color
     */
    private fun getColorNameFromAttr(colorAttr: Int): String? {
        return when (colorAttr) {
            R.attr.color_main1_100 -> "100"
            R.attr.color_main1_100_alpha_50 -> "100_alpha_50"
            R.attr.color_main1_300 -> "300"
            R.attr.color_main1_500 -> "500"
            R.attr.color_main1_700 -> "700"
            else -> null
        }
    }

    /**
     * Checks if provider colors are currently applied
     * @return true if provider colors are active
     */
    fun hasProviderColors(): Boolean {
        return colorPalette1 != null || colorPalette2 != null
    }

    /**
     * Clears provider colors and reverts to default theme
     */
    fun clearProviderColors() {
        currentProviderColor1 = null
        currentProviderColor2 = null
        colorPalette1 = null
        colorPalette2 = null
        Log.i("$TAG Provider colors cleared")
    }

    /**
     * Applies provider colors from stored login result
     * @param context Application context
     */
    @UiThread
    fun applyStoredProviderColors(context: Context) {
        try {
            val loginResult = VoxNodeDataManager.getLoginResult()
            if (loginResult != null) {
                applyProviderColors(context, loginResult)
            }
        } catch (e: Exception) {
            Log.e("$TAG Failed to apply stored provider colors: ${e.message}")
        }
    }

    /**
     * Gets the current provider color 1
     * @return Provider color 1 hex string or null
     */
    fun getCurrentProviderColor1(): String? = currentProviderColor1

    /**
     * Gets the current provider color 2
     * @return Provider color 2 hex string or null
     */
    fun getCurrentProviderColor2(): String? = currentProviderColor2
}

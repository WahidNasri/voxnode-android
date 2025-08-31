package org.linphone.utils

import android.graphics.Color
import org.linphone.core.tools.Log

object ColorUtils {
    private const val TAG = "[Color Utils]"

    /**
     * Converts a hex color string to Color object
     * @param hexColor Hex color string (e.g., "#FF0000" or "FF0000")
     * @return Color object or null if invalid
     */
    fun hexToColor(hexColor: String?): Int? {
        if (hexColor.isNullOrEmpty()) return null
        
        return try {
            val cleanHex = hexColor.removePrefix("#")
            Color.parseColor("#$cleanHex")
        } catch (e: Exception) {
            Log.e("$TAG Failed to parse hex color: $hexColor, error: ${e.message}")
            null
        }
    }

    /**
     * Creates a lighter version of a color (100 shade)
     * @param baseColor Base color as Color object
     * @return Lighter color
     */
    fun createLighterColor(baseColor: Int): Int {
        val hsl = FloatArray(3)
        Color.colorToHSV(baseColor, hsl)
        
        // Increase lightness and reduce saturation for a lighter shade
        hsl[1] = hsl[1] * 0.3f // Reduce saturation
        hsl[2] = minOf(hsl[2] * 1.8f, 0.95f) // Increase lightness but cap at 0.95
        
        return Color.HSVToColor(hsl)
    }

    /**
     * Creates a lighter version with 50% alpha (100_alpha_50 shade)
     * @param baseColor Base color as Color object
     * @return Lighter color with 50% alpha
     */
    fun createLighterColorWithAlpha(baseColor: Int): Int {
        val lighterColor = createLighterColor(baseColor)
        return Color.argb(128, Color.red(lighterColor), Color.green(lighterColor), Color.blue(lighterColor))
    }

    /**
     * Creates a medium-light version of a color (300 shade)
     * @param baseColor Base color as Color object
     * @return Medium-light color
     */
    fun createMediumLightColor(baseColor: Int): Int {
        val hsl = FloatArray(3)
        Color.colorToHSV(baseColor, hsl)
        
        // Moderate increase in lightness and slight reduction in saturation
        hsl[1] = hsl[1] * 0.6f // Reduce saturation moderately
        hsl[2] = minOf(hsl[2] * 1.4f, 0.85f) // Increase lightness moderately
        
        return Color.HSVToColor(hsl)
    }

    /**
     * Creates a darker version of a color (700 shade)
     * @param baseColor Base color as Color object
     * @return Darker color
     */
    fun createDarkerColor(baseColor: Int): Int {
        val hsl = FloatArray(3)
        Color.colorToHSV(baseColor, hsl)
        
        // Decrease lightness and increase saturation for a darker shade
        hsl[1] = minOf(hsl[1] * 1.2f, 1.0f) // Increase saturation slightly
        hsl[2] = hsl[2] * 0.6f // Decrease lightness
        
        return Color.HSVToColor(hsl)
    }

    /**
     * Creates a complete color palette from a base color
     * @param baseColorHex Hex color string
     * @return Map of color variations
     */
    fun createColorPalette(baseColorHex: String?): Map<String, Int>? {
        val baseColor = hexToColor(baseColorHex) ?: return null
        
        return mapOf(
            "100" to createLighterColor(baseColor),
            "100_alpha_50" to createLighterColorWithAlpha(baseColor),
            "300" to createMediumLightColor(baseColor),
            "500" to baseColor,
            "700" to createDarkerColor(baseColor)
        )
    }

    /**
     * Validates if a hex color string is valid
     * @param hexColor Hex color string
     * @return true if valid, false otherwise
     */
    fun isValidHexColor(hexColor: String?): Boolean {
        if (hexColor.isNullOrEmpty()) return false
        
        val cleanHex = hexColor.removePrefix("#")
        return cleanHex.length == 6 && cleanHex.matches(Regex("[0-9A-Fa-f]{6}"))
    }
}

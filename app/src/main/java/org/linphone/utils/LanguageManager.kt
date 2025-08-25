/*
 * Copyright (c) 2010-2023 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import org.linphone.core.tools.Log
import java.util.Locale
import kotlin.UninitializedPropertyAccessException

object LanguageManager {
    private const val TAG = "[Language Manager]"

    /**
     * Get SharedPreferences safely
     */
    private fun getSharedPreferences(context: Context): SharedPreferences? {
        return try {
            context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        } catch (e: Exception) {
            Log.e("$TAG Failed to get SharedPreferences: ${e.message}")
            null
        }
    }

    /**
     * Get the saved language preference or system default
     */
    fun getSavedLanguage(context: Context? = null): String {
        return try {
            // Try to get from CorePreferences first (if available and initialized)
            try {
                val corePreferences = org.linphone.LinphoneApplication.corePreferences
                val savedLanguage = corePreferences.config.getString("app", "language", "")
                if (!savedLanguage.isNullOrEmpty()) {
                    return savedLanguage
                }
            } catch (e: UninitializedPropertyAccessException) {
                Log.d("$TAG CorePreferences not initialized yet, using SharedPreferences")
            } catch (e: Exception) {
                Log.d("$TAG CorePreferences error, trying SharedPreferences: ${e.message}")
            }
            
            // Fallback to SharedPreferences
            if (context != null) {
                val prefs = getSharedPreferences(context)
                val savedLanguage = prefs?.getString("language", "") ?: ""
                if (savedLanguage.isNotEmpty()) {
                    return savedLanguage
                }
            }
            
            // Default to system language
            Locale.getDefault().language
        } catch (e: Exception) {
            Log.e("$TAG Failed to get saved language, using system default: ${e.message}")
            Locale.getDefault().language
        }
    }

    /**
     * Save the language preference
     */
    fun saveLanguage(languageCode: String, context: Context? = null) {
        try {
            // Try to save to CorePreferences first (if initialized)
            try {
                val corePreferences = org.linphone.LinphoneApplication.corePreferences
                corePreferences.config.setString("app", "language", languageCode)
                Log.i("$TAG Language preference saved to CorePreferences: $languageCode")
                return // Success, no need to save to SharedPreferences
            } catch (e: UninitializedPropertyAccessException) {
                Log.d("$TAG CorePreferences not initialized yet, saving to SharedPreferences")
            } catch (e: Exception) {
                Log.d("$TAG CorePreferences error, saving to SharedPreferences: ${e.message}")
            }
            
            // Fallback to SharedPreferences
            if (context != null) {
                val prefs = getSharedPreferences(context)
                prefs?.edit()?.putString("language", languageCode)?.apply()
                Log.i("$TAG Language preference saved to SharedPreferences: $languageCode")
            } else {
                Log.w("$TAG Cannot save language preference - neither CorePreferences nor context available")
            }
        } catch (e: Exception) {
            Log.e("$TAG Failed to save language preference: ${e.message}")
        }
    }

    /**
     * Apply language to the given context and return updated context
     */
    fun applyLanguageToContext(context: Context, languageCode: String): Context {
        Log.i("$TAG Applying language $languageCode to context")
        
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }

    /**
     * Change language globally using AppCompatDelegate (Android 13+)
     */
    fun changeLanguageGlobally(languageCode: String, context: Context? = null) {
        Log.i("$TAG Changing language globally to: $languageCode")
        
        try {
            // Save the preference first
            saveLanguage(languageCode, context)
            
            // For Android 13+ (API 33+), use AppCompatDelegate
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeList = LocaleListCompat.forLanguageTags(languageCode)
                AppCompatDelegate.setApplicationLocales(localeList)
                Log.i("$TAG Applied language using AppCompatDelegate for Android 13+")
            } else {
                // For older versions, we'll need to recreate activities
                Log.i("$TAG Language change applied, activities will need recreation")
            }
        } catch (e: Exception) {
            Log.e("$TAG Failed to change language globally: ${e.message}")
        }
    }

    /**
     * Initialize language on app start
     */
    fun initializeLanguage(context: Context) {
        val savedLanguage = getSavedLanguage(context)
        Log.i("$TAG Initializing app with language: $savedLanguage")
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeList = LocaleListCompat.forLanguageTags(savedLanguage)
                AppCompatDelegate.setApplicationLocales(localeList)
            } else {
                // For older versions, apply to default locale
                val locale = Locale(savedLanguage)
                Locale.setDefault(locale)
            }
        } catch (e: Exception) {
            Log.e("$TAG Failed to initialize language: ${e.message}")
        }
    }

    /**
     * Get display name for language code
     */
    fun getLanguageDisplayName(languageCode: String, context: Context? = null): String {
        return when (languageCode) {
            "en" -> context?.getString(org.linphone.R.string.language_display_english) ?: "English"
            "fr" -> context?.getString(org.linphone.R.string.language_display_french) ?: "Français"
            else -> context?.getString(org.linphone.R.string.language_display_default) ?: "English"
        }
    }

    /**
     * Get available languages
     */
    fun getAvailableLanguages(context: Context? = null): List<Pair<String, String>> {
        return listOf(
            Pair("en", getLanguageDisplayName("en", context)),
            Pair("fr", getLanguageDisplayName("fr", context))
        )
    }
}

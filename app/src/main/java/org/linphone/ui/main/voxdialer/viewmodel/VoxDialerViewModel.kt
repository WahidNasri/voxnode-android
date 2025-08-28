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
package org.linphone.ui.main.voxdialer.viewmodel

import androidx.annotation.UiThread
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.core.tools.Log
import org.linphone.ui.main.viewmodel.AbstractMainViewModel
import org.linphone.ui.main.voxdialer.viewmodel.VoxDialerViewModel.CountryData.COUNTRY_CALLING_CODE_TO_ISO
import org.linphone.utils.Event
import org.linphone.utils.LinphoneUtils

@UiThread
class VoxDialerViewModel
    constructor() : AbstractMainViewModel() {
    companion object {
        private const val TAG = "[VoxDialer ViewModel]"
        private const val MAX_INPUT_LENGTH = 18
    }

    val enteredUri = MutableLiveData<String>()
    // Exposed formatted number for display
    val formattedNumber = MediatorLiveData<String>()
    // Emoji flag for detected country when number is international
    val countryFlagEmoji = MediatorLiveData<String>()
    val isFlagVisible = MediatorLiveData<Boolean>()
    val callButtonEnabled = MutableLiveData<Boolean>()
    val deleteButtonEnabled = MutableLiveData<Boolean>()
    val addContactEvent: MutableLiveData<Event<String>> by lazy {
        MutableLiveData<Event<String>>()
    }

    init {
        Log.i("$TAG Initialized")
        title.value = "Dialer"
        searchBarVisible.value = false
        
        enteredUri.value = ""
        callButtonEnabled.value = false
        deleteButtonEnabled.value = false

        formattedNumber.value = ""
        countryFlagEmoji.value = ""
        isFlagVisible.value = false

        // React to changes and compute derived state
        formattedNumber.addSource(enteredUri) { uri ->
            val normalized = normalizeInput(uri.orEmpty())
            if (normalized != uri) {
                // Avoid infinite loop by only updating when changed
                enteredUri.value = normalized
            }

            val display = formatForDisplay(normalized)
            formattedNumber.value = display

            val flag = computeCountryFlag(normalized)
            countryFlagEmoji.value = flag
            isFlagVisible.value = flag.isNotEmpty()

            val hasContent = normalized.isNotEmpty()
            callButtonEnabled.value = hasContent
            deleteButtonEnabled.value = hasContent
        }
    }

    override fun filter() {
        Log.i("$TAG Filter for VoxDialer: [$currentFilter]")
        // Dialer doesn't need filtering functionality
    }

    fun appendDigit(digit: String) {
        val current = enteredUri.value ?: ""
        val next = (current + digit)
        if (next.length <= MAX_INPUT_LENGTH) {
            enteredUri.value = next
            Log.d("$TAG Appended digit [$digit], current URI: [${enteredUri.value}]")
        } else {
            Log.d("$TAG Input too long, ignoring digit [$digit]")
        }
    }

    fun deleteLastDigit() {
        val current = enteredUri.value ?: ""
        if (current.isNotEmpty()) {
            enteredUri.value = current.dropLast(1)
            Log.d("$TAG Deleted last digit, current URI: [${enteredUri.value}]")
        }
    }

    fun clearUri() {
        enteredUri.value = ""
        Log.d("$TAG Cleared URI")
    }

    private fun clearUriFromBackgroundThread() {
        enteredUri.postValue("")
        Log.d("$TAG Cleared URI from background thread")
    }

    fun pasteNumber() {
        // This will be called from the fragment when paste is detected
        // The actual paste logic will be handled in the fragment
        Log.d("$TAG Paste requested")
    }

    fun setPastedNumber(number: String) {
        val cleaned = number.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        if (cleaned.length <= MAX_INPUT_LENGTH) {
            enteredUri.value = cleaned
            Log.d("$TAG Pasted number: [$cleaned]")
        } else {
            // Truncate to max length
            enteredUri.value = cleaned.take(MAX_INPUT_LENGTH)
            Log.d("$TAG Pasted number truncated to: [${enteredUri.value}]")
        }
    }

    fun makeCall() {
        val uri = (enteredUri.value ?: "").replace(" ", "")
        if (!uri.isNullOrEmpty()) {
            Log.i("$TAG Initiating call to: [$uri]")
            coreContext.postOnCoreThread { core ->
                val address = core.interpretUrl(
                    uri,
                    LinphoneUtils.applyInternationalPrefix()
                )
                if (address != null) {
                    Log.i("$TAG Calling [${address.asStringUriOnly()}]")
                    coreContext.startAudioCall(address)
                    // Clear the dialer after successful call initiation
                    clearUriFromBackgroundThread()
                } else {
                    Log.e("$TAG Failed to parse [$uri] as SIP address")
                    // TODO: Add appropriate error message toast
                }
            }
        }
    }

    fun longPressDelete() {
        clearUri()
        Log.d("$TAG Long press delete - cleared all")
    }

    fun onExtraActionClicked() {
        val number = (enteredUri.value ?: "").replace(" ", "")
        if (number.isNotEmpty()) {
            addContactEvent.value = Event(number)
        }
    }

    private fun normalizeInput(input: String): String {
        if (input.isEmpty()) return input
        var value = input
        // Turn starting 00 into +
        if (value.startsWith("00")) {
            value = "+" + value.removePrefix("00")
        }
        // Remove any spaces typed by user; formatting is handled separately
        value = value.replace(" ", "")
        return value
    }

    private fun formatForDisplay(input: String): String {
        if (input.isEmpty()) return ""
        if (input.startsWith("+")) {
            val (countryCode, rest) = splitInternational(input)
            return if (countryCode.isNotEmpty()) "+$countryCode $rest" else input
        }
        // Local format: add space every 2 digits
        val digitsOnly = input.filter { it.isDigit() || it == '*' || it == '#' }
        val builder = StringBuilder()
        var count = 0
        for (ch in digitsOnly) {
            if (ch.isDigit()) {
                if (count > 0 && count % 2 == 0) builder.append(' ')
                builder.append(ch)
                count++
            } else {
                // Keep special chars at the end without spacing logic
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    private fun computeCountryFlag(input: String): String {
        if (!input.startsWith("+")) return ""
        val (countryCode, _) = splitInternational(input)
        if (countryCode.isEmpty()) return ""
        val iso = COUNTRY_CALLING_CODE_TO_ISO[countryCode] ?: return ""
        return isoToFlagEmoji(iso)
    }

    private fun splitInternational(input: String): Pair<String, String> {
        // input starts with '+' and contains only digits/spaces thereafter
        val digits = input.removePrefix("+").filter { it.isDigit() }
        // Country calling codes are 1-3 digits. Try longest-first matching.
        for (len in 3 downTo 1) {
            if (digits.length >= len) {
                val code = digits.substring(0, len)
                if (COUNTRY_CALLING_CODE_TO_ISO.containsKey(code)) {
                    val rest = digits.substring(len)
                    return Pair(code, rest)
                }
            }
        }
        return Pair("", digits)
    }

    private fun isoToFlagEmoji(isoCountryCode: String): String {
        if (isoCountryCode.length != 2) return ""
        val upper = isoCountryCode.uppercase()
        val first = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
        val second = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }

    object CountryData {
        // Minimal mapping; extend as needed
        val COUNTRY_CALLING_CODE_TO_ISO: Map<String, String> = mapOf(
            // NANP
            "1" to "US",
            // Europe
            "30" to "GR",
            "31" to "NL",
            "32" to "BE",
            "33" to "FR",
            "34" to "ES",
            "36" to "HU",
            "39" to "IT",
            "40" to "RO",
            "41" to "CH",
            "43" to "AT",
            "44" to "GB",
            "45" to "DK",
            "46" to "SE",
            "47" to "NO",
            "48" to "PL",
            "49" to "DE",
            // Others common
            "52" to "MX",
            "55" to "BR",
            "61" to "AU",
            "62" to "ID",
            "63" to "PH",
            "64" to "NZ",
            "65" to "SG",
            "66" to "TH",
            "81" to "JP",
            "82" to "KR",
            "84" to "VN",
            "86" to "CN",
            "90" to "TR",
            "91" to "IN",
            "92" to "PK",
            "93" to "AF",
            "94" to "LK",
            "95" to "MM",
            "98" to "IR",
            // Africa
            "20" to "EG",
            "212" to "MA",
            "213" to "DZ",
            "216" to "TN",
            "218" to "LY",
            "221" to "SN",
            "225" to "CI",
            "229" to "BJ",
            "234" to "NG",
            "237" to "CM",
            "254" to "KE",
            "255" to "TZ",
            "256" to "UG",
            "260" to "ZM",
            "261" to "MG",
            "263" to "ZW",
            // Russia
            "7" to "RU",
        )
    }
}

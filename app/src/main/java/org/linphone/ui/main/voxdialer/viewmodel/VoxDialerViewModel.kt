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
import androidx.lifecycle.MutableLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.core.tools.Log
import org.linphone.ui.main.viewmodel.AbstractMainViewModel
import org.linphone.utils.LinphoneUtils

@UiThread
class VoxDialerViewModel
    constructor() : AbstractMainViewModel() {
    companion object {
        private const val TAG = "[VoxDialer ViewModel]"
    }

    val enteredUri = MutableLiveData<String>()
    val callButtonEnabled = MutableLiveData<Boolean>()
    val deleteButtonEnabled = MutableLiveData<Boolean>()

    init {
        Log.i("$TAG Initialized")
        title.value = "Dialer"
        searchBarVisible.value = false
        
        enteredUri.value = ""
        callButtonEnabled.value = false
        deleteButtonEnabled.value = false

        enteredUri.observeForever { uri ->
            val hasContent = !uri.isNullOrEmpty()
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
        enteredUri.value = current + digit
        Log.d("$TAG Appended digit [$digit], current URI: [${enteredUri.value}]")
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

    fun makeCall() {
        val uri = enteredUri.value
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
}

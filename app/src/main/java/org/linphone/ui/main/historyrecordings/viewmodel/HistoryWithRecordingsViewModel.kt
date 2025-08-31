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
package org.linphone.ui.main.historyrecordings.viewmodel

import androidx.annotation.UiThread
import androidx.lifecycle.MutableLiveData
import org.linphone.core.tools.Log
import org.linphone.ui.main.viewmodel.AbstractMainViewModel
import org.voxnode.voxnode.storage.VoxNodeDataManager

@UiThread
class HistoryWithRecordingsViewModel : AbstractMainViewModel() {
    companion object {
        private const val TAG = "[HistoryWithRecordings ViewModel]"
    }

    val currentTabIndex = MutableLiveData<Int>()
    val isRecordingEnabled = MutableLiveData<Boolean>()

    init {
        title.value = "History"
        
        // Check recording enabled status
        val recordingEnabled = checkIfRecordingEnabled()
        isRecordingEnabled.value = recordingEnabled
        
        // Set default tab index based on recording status
        currentTabIndex.value = 0 // Always start with Calls tab
        Log.i("$TAG HistoryWithRecordings ViewModel initialized with recordingEnabled: $recordingEnabled")
    }

    @UiThread
    override fun filter() {
        // No filtering needed for this container fragment
        // Child fragments handle their own filtering
    }

    @UiThread
    fun setCurrentTab(index: Int) {
        val maxTabIndex = if (isRecordingEnabled.value == true) 1 else 0
        
        if (index <= maxTabIndex) {
            Log.i("$TAG Setting current tab to index [$index]")
            currentTabIndex.value = index
        } else {
            Log.w("$TAG Invalid tab index [$index], max allowed is [$maxTabIndex]")
            currentTabIndex.value = 0 // Default to Calls tab
        }
    }

    private fun checkIfRecordingEnabled(): Boolean {
        return try {
            val loginResult = VoxNodeDataManager.getLoginResult()
            val isEnabled = loginResult?.clientRecordingEnabled == 1
            Log.i("$TAG clientRecordingEnabled from login result: ${loginResult?.clientRecordingEnabled}, isEnabled: $isEnabled")
            isEnabled
        } catch (e: Exception) {
            Log.e("$TAG Error checking recording enabled status: ${e.message}")
            false
        }
    }
}

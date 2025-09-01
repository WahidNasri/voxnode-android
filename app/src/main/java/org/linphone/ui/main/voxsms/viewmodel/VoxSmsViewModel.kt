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
package org.linphone.ui.main.voxsms.viewmodel

import androidx.annotation.UiThread
import androidx.lifecycle.MutableLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.core.tools.Log
import org.linphone.ui.main.viewmodel.AbstractMainViewModel
import org.linphone.ui.main.voxsms.model.SmsModel
import org.linphone.utils.Event
import org.voxnode.voxnode.storage.VoxNodeDataManager

@UiThread
class VoxSmsViewModel : AbstractMainViewModel() {
    companion object {
        private const val TAG = "[VoxSms ViewModel]"
    }

    val smsList = MutableLiveData<List<SmsModel>>()
    val smsCount = MutableLiveData<Int>()
    
    // SMS functionality enabled state
    val isSmsEnabled = MutableLiveData<Boolean>()
    
    // Navigation events
    val navigateToSendSmsEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData<Event<Boolean>>()
    }

    init {
        title.value = "SMS"
        Log.i("$TAG VoxSms ViewModel initialized")
        
        // Initialize with empty list
        smsList.value = emptyList()
        smsCount.value = 0
        
        // Check if SMS is enabled for this client
        checkSmsEnabled()
        
        // Load SMS messages
        loadSmsMessages()
    }

    private fun checkSmsEnabled() {
        try {
            val loginResult = VoxNodeDataManager.getLoginResult()
            val clientSmsEnabled = loginResult?.clientSmsEnabled ?: 0
            isSmsEnabled.value = clientSmsEnabled == 1
            Log.i("$TAG SMS enabled: ${isSmsEnabled.value} (clientSmsEnabled: $clientSmsEnabled)")
        } catch (e: Exception) {
            Log.w("$TAG Failed to check SMS enabled state: ${e.message}")
            isSmsEnabled.value = false
        }
    }

    @UiThread
    fun onNewSmsClicked() {
        Log.i("$TAG New SMS button clicked")
        navigateToSendSmsEvent.value = Event(true)
    }

    @UiThread
    fun loadSmsMessages() {
        Log.i("$TAG Loading SMS messages")
        
        // For now, use dummy data. In a real implementation, this would
        // load from a local database or API
        val dummySmsList = listOf<SmsModel>(
            // Add some dummy SMS for testing if needed
        )
        
        coreContext.postOnMainThread {
            smsList.value = dummySmsList
            smsCount.value = dummySmsList.size
            Log.i("$TAG Loaded ${dummySmsList.size} SMS messages")
        }
    }

    @UiThread
    fun refreshSmsList() {
        Log.i("$TAG Refreshing SMS list")
        loadSmsMessages()
    }

    @UiThread
    override fun filter() {
        // SMS filtering can be implemented here if needed
    }
}

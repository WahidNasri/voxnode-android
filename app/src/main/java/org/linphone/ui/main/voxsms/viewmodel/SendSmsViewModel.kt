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
import androidx.lifecycle.MediatorLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.core.tools.Log
import org.linphone.ui.GenericViewModel
import org.linphone.utils.Event
import org.voxnode.voxnode.api.VoxnodeRepository
import org.voxnode.voxnode.storage.VoxNodeDataManager

@UiThread
class SendSmsViewModel : GenericViewModel() {
    companion object {
        private const val TAG = "[SendSms ViewModel]"
    }

    val recipientNumber = MutableLiveData<String>()
    val smsContent = MutableLiveData<String>()
    val isSending = MutableLiveData<Boolean>()
    
    // LiveData that automatically updates when inputs change
    val canSendSms = MediatorLiveData<Boolean>()
    
    // Navigation events
    val smsSuccessEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData<Event<Boolean>>()
    }
    
    private val voxnodeRepository = VoxnodeRepository()

    init {
        Log.i("$TAG SendSms ViewModel initialized")
        
        recipientNumber.value = ""
        smsContent.value = ""
        isSending.value = false
        
        // Set up MediatorLiveData to observe input changes
        canSendSms.addSource(recipientNumber) { updateCanSendSms() }
        canSendSms.addSource(smsContent) { updateCanSendSms() }
        canSendSms.addSource(isSending) { updateCanSendSms() }
        
        // Initial update
        updateCanSendSms()
    }

    private fun updateCanSendSms() {
        val recipient = recipientNumber.value?.trim()
        val content = smsContent.value?.trim()
        val sending = isSending.value ?: false
        
        val canSend = !recipient.isNullOrEmpty() && 
                     !content.isNullOrEmpty() && 
                     !sending
        
        canSendSms.value = canSend
    }

    @UiThread
    fun sendSms() {
        val recipient = recipientNumber.value?.trim()
        val content = smsContent.value?.trim()
        
        if (recipient.isNullOrEmpty() || content.isNullOrEmpty()) {
            Log.e("$TAG Cannot send SMS: missing recipient or content")
            showFormattedRedToast("Please fill in all fields", org.linphone.R.drawable.warning_circle)
            return
        }
        
        val loginResult = VoxNodeDataManager.getLoginResult()
        val providerId = loginResult?.providerId
        val clientId = loginResult?.clientId
        val clientKey = loginResult?.clientKey
        
        if (providerId == null || clientId == null || clientKey == null) {
            Log.e("$TAG Cannot send SMS: missing login credentials")
            showFormattedRedToast("Missing login credentials", org.linphone.R.drawable.warning_circle)
            return
        }
        
        Log.i("$TAG Sending SMS to $recipient with content: ${content.take(50)}...")
        isSending.value = true
        
        voxnodeRepository.sendSms(
            providerId = providerId,
            clientId = clientId,
            clientKey = clientKey,
            number = recipient,
            message = content,
            onSuccess = { response ->
                Log.i("$TAG SMS sent successfully: ${response.message}")
                
                coreContext.postOnMainThread {
                    isSending.value = false
                    showFormattedGreenToast("SMS sent successfully", org.linphone.R.drawable.check)
                    
                    // Clear the form
                    recipientNumber.value = ""
                    smsContent.value = ""
                    
                    // Trigger success event to navigate back
                    smsSuccessEvent.value = Event(true)
                }
            },
            onError = { error ->
                Log.e("$TAG Failed to send SMS: $error")
                
                coreContext.postOnMainThread {
                    isSending.value = false
                    showFormattedRedToast("Failed to send SMS: $error", org.linphone.R.drawable.warning_circle)
                }
            }
        )
    }
}

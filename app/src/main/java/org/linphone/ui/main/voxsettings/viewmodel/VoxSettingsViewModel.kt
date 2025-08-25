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
package org.linphone.ui.main.voxsettings.viewmodel

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.core.tools.Log
import org.linphone.ui.main.viewmodel.AbstractMainViewModel
import org.voxnode.voxnode.storage.VoxNodeDataManager

@UiThread
class VoxSettingsViewModel : AbstractMainViewModel() {
    companion object {
        private const val TAG = "[VoxSettings ViewModel]"
    }

    val voxSettingsTitle = MutableLiveData<String>()

    // VoxNode user data
    val userEmail = MutableLiveData<String>()
    val sipAddress = MutableLiveData<String>()
    val isLoggedIn = MutableLiveData<Boolean>()

    init {
        title.value = "VoxSettings"
        voxSettingsTitle.value = "VoxSettings Configuration"
        Log.i("$TAG VoxSettings ViewModel initialized")

        // Load VoxNode data
        loadVoxNodeData()
    }

    @UiThread
    fun loadVoxNodeData() {
        coreContext.postOnCoreThread {
            loadVoxNodeDataOnWorkerThread()
        }
    }

    @WorkerThread
    private fun loadVoxNodeDataOnWorkerThread() {
        try {
            val loginResult = VoxNodeDataManager.getLoginResult()
            val loggedIn = VoxNodeDataManager.isUserLoggedIn()

            // Update UI on main thread
            coreContext.postOnMainThread {
                isLoggedIn.value = loggedIn

                if (loginResult != null) {
                    userEmail.value = loginResult.clientEmail ?: "Not available"
                    sipAddress.value = loginResult.clientKey ?: "Not available"

                    Log.i("$TAG VoxNode data loaded successfully for user: ${loginResult.clientEmail}")
                } else {
                    // Set default values when no data is available
                    userEmail.value = "Not logged in"
                    sipAddress.value = "N/A"

                    Log.w("$TAG No VoxNode login data found")
                }
            }
        } catch (e: Exception) {
            Log.e("$TAG Failed to load VoxNode data: ${e.message}")
            coreContext.postOnMainThread {
                isLoggedIn.value = false
                userEmail.value = "Error loading data"
            }
        }
    }

    @UiThread
    fun logout() {
        Log.i("$TAG User requested logout, clearing VoxNode data and removing account")

        coreContext.postOnCoreThread { core ->
            VoxNodeDataManager.clearLoginData()

            val voxNodeAccount = core.accountList.first()

            if (voxNodeAccount != null) {
                Log.i("$TAG Found VoxNode account, removing it")
                val identity = voxNodeAccount.params.identityAddress?.asStringUriOnly()
                    
                // Clear call logs for this account
                voxNodeAccount.clearCallLogs()

                // Remove meetings related to this account
                for (meeting in voxNodeAccount.conferenceInformationList) {
                    core.deleteConferenceInformation(meeting)
                }

                // Remove auth info
                val authInfo = voxNodeAccount.findAuthInfo()
                if (authInfo != null) {
                    Log.i("$TAG Removing auth info for account [$identity]")
                    core.removeAuthInfo(authInfo)
                }

                // Remove the account
                core.removeAccount(voxNodeAccount)
                Log.i("$TAG VoxNode account [$identity] has been removed")
            }

            // Update UI on main thread
            coreContext.postOnMainThread {
                loadVoxNodeData() // Reload to show logged out state
                Log.i("$TAG User logout completed")
            }
        }
    }

    @UiThread
    override fun filter() {
        // No filtering needed for this simple settings page
    }
}

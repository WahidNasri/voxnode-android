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

import android.content.Intent
import android.net.Uri
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.core.tools.Log
import org.linphone.ui.main.viewmodel.AbstractMainViewModel
import org.linphone.utils.Event
import org.linphone.utils.LanguageManager
import org.voxnode.voxnode.api.VoxnodeRepository
import org.voxnode.voxnode.storage.VoxNodeDataManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@UiThread
class VoxSettingsViewModel : AbstractMainViewModel() {
    companion object {
        private const val TAG = "[VoxSettings ViewModel]"
    }

    val voxSettingsTitle = MutableLiveData<String>()

    // VoxNode user data
    val userEmail = MutableLiveData<String>()
    val sipAddress = MutableLiveData<String>()
    val callerIdNumber = MutableLiveData<String>()
    val isLoggedIn = MutableLiveData<Boolean>()
    val isCallerIdLoading = MutableLiveData<Boolean>()
    val avatarPath = MutableLiveData<String?>()
    
    // Caller ID data
    private var allCallerIds: List<org.voxnode.voxnode.models.CallerId> = emptyList()
    private var currentCallerId: org.voxnode.voxnode.models.CallerId? = null
    
    // Language settings
    val currentLanguage = MutableLiveData<String>()
    val currentLanguageDisplayName = MutableLiveData<String>()
    val showLanguageBottomSheetEvent = MutableLiveData<Event<String>>()
    
    // Navigation events
    val navigateToPermissionsEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData<Event<Boolean>>()
    }

    @UiThread
    fun openTermsOfUse() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.voxnode.com/app-cgv.html"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            coreContext.context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("$TAG Failed to open terms URL: ${e.message}")
        }
    }

    @UiThread
    fun openPrivacyPolicy() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.voxnode.com/app-privacy.html"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            coreContext.context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("$TAG Failed to open privacy URL: ${e.message}")
        }
    }

    // Caller ID events
    val showCallerIdBottomSheetEvent:
        MutableLiveData<Event<Pair<List<org.voxnode.voxnode.models.CallerId>, org.voxnode.voxnode.models.CallerId?>>> by lazy {
        MutableLiveData<Event<Pair<List<org.voxnode.voxnode.models.CallerId>, org.voxnode.voxnode.models.CallerId?>>>()
    }
    
    private val voxnodeRepository = VoxnodeRepository()

    init {
        title.value = "VoxSettings"
        voxSettingsTitle.value = "VoxSettings Configuration"
        Log.i("$TAG VoxSettings ViewModel initialized")

        // Load VoxNode data
        loadVoxNodeData()
        // Load local avatar if any
        loadLocalAvatar()
        
        // Initialize language settings
        initializeLanguageSettings()
    }

    @UiThread
    fun saveAvatarFromUri(uri: Uri) {
        coreContext.postOnMainThread {
            try {
                val inputStream: InputStream? = coreContext.context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    showFormattedRedToast("Failed to read image", org.linphone.R.drawable.warning_circle)
                    return@postOnMainThread
                }

                val avatarsDir = File(coreContext.context.filesDir, "avatars")
                if (!avatarsDir.exists()) avatarsDir.mkdirs()

                val avatarFile = File(avatarsDir, "profile_avatar.jpg")
                FileOutputStream(avatarFile).use { out ->
                    inputStream.copyTo(out)
                }
                inputStream.close()

                // Bump a version to force data binding to rebind (change reference)
                avatarPath.value = avatarFile.absolutePath + "?ts=" + System.currentTimeMillis()
                showFormattedGreenToast("Avatar updated", org.linphone.R.drawable.check)
            } catch (e: Exception) {
                Log.e("$TAG Failed to save avatar: ${e.message}")
                showFormattedRedToast("Failed to save avatar", org.linphone.R.drawable.warning_circle)
            }
        }
    }

    @UiThread
    private fun loadLocalAvatar() {
        val avatarFile = File(File(coreContext.context.filesDir, "avatars"), "profile_avatar.jpg")
        if (avatarFile.exists()) {
            avatarPath.value = avatarFile.absolutePath
        }
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
                    
                    // First try to load caller ID from local storage
                    val storedCallerId = VoxNodeDataManager.getCurrentCallerId()
                    if (storedCallerId.isNotEmpty()) {
                        callerIdNumber.value = storedCallerId
                        Log.i("$TAG Loaded caller ID from local storage: $storedCallerId")
                    }   // If no stored caller ID, fetch from API
                    fetchCurrentCallerId(loginResult)

                } else {
                    // Set default values when no data is available
                    userEmail.value = "Not logged in"
                    sipAddress.value = "N/A"
                    callerIdNumber.value = "N/A"

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
    private fun fetchCurrentCallerId(loginResult: org.voxnode.voxnode.models.LoginResult) {
        val clientId = loginResult.clientId ?: return
        val clientKey = loginResult.clientKey ?: return
        
        Log.i("$TAG Fetching caller ID for client ID: $clientId")
        isCallerIdLoading.value = true
        
        voxnodeRepository.getCallerIds(
            providerId = 1, // VoipPhone provider ID
            clientId = clientId,
            clientKey = clientKey,
            onSuccess = { callerIds ->
                Log.i("$TAG Successfully fetched ${callerIds.size} caller IDs")
                
                // Store all caller IDs
                allCallerIds = callerIds
                
                // Find the current/selected caller ID
                currentCallerId = callerIds.find { it.isSelected }
                
                coreContext.postOnMainThread {
                    isCallerIdLoading.value = false
                    if (currentCallerId != null) {
                        callerIdNumber.value = currentCallerId?.callerID ?: "Not available"
                        Log.i("$TAG Current caller ID: ${currentCallerId?.callerID}")
                        
                        // Save the current caller ID locally
                        coreContext.postOnCoreThread {
                            VoxNodeDataManager.saveCurrentCallerId(currentCallerId!!)
                        }
                    } else {
                        // If no current caller ID is selected, show the first one or fallback
                        val firstCallerId = callerIds.firstOrNull()
                        callerIdNumber.value = firstCallerId?.callerID ?: sipAddress.value ?: "Not available"
                        Log.w("$TAG No current caller ID selected, using: ${callerIdNumber.value}")
                        
                        // Save the first caller ID locally if available
                        if (firstCallerId != null) {
                            coreContext.postOnCoreThread {
                                VoxNodeDataManager.saveCurrentCallerId(firstCallerId)
                            }
                        }
                    }
                }
            },
            onError = { error ->
                Log.e("$TAG Failed to fetch caller IDs: $error")
                coreContext.postOnMainThread {
                    isCallerIdLoading.value = false
                    // Fallback to SIP address if caller ID fetch fails
                    callerIdNumber.value = sipAddress.value ?: "Not available"
                    // showRedToast("Failed to load caller ID: $error", org.linphone.R.drawable.warning_circle)
                }
            }
        )
    }

    @UiThread
    fun logout() {
        Log.i("$TAG User requested logout, clearing VoxNode data and removing account")

        coreContext.postOnCoreThread { core ->
            VoxNodeDataManager.clearLoginData()

            // Preserve local avatar by tagging it with current client email (if available)
            try {
                val avatarsDir = File(coreContext.context.filesDir, "avatars")
                val avatarFile = File(avatarsDir, "profile_avatar.jpg")
                if (avatarFile.exists()) {
                    val email = try {
                        org.voxnode.voxnode.storage.VoxNodeDataManager.getLoginResult()?.clientEmail
                    } catch (e: Exception) { null }
                    val sanitizedEmail = (email ?: "unknown").replace("[^A-Za-z0-9@._-]".toRegex(), "_")
                    val target = File(avatarsDir, "profile_avatar_$sanitizedEmail.jpg")
                    if (!target.exists()) {
                        avatarFile.copyTo(target, overwrite = false)
                    }
                    // Do not delete original to avoid data loss if needed elsewhere
                }
                coreContext.postOnMainThread { avatarPath.value = null }
            } catch (e: Exception) {
                Log.w("$TAG Failed to preserve avatar on logout: ${e.message}")
            }

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
    fun initializeLanguageSettings() {
        val currentLang = LanguageManager.getSavedLanguage()
        currentLanguage.value = currentLang
        updateLanguageDisplayName(currentLang)
        Log.i("$TAG Current language initialized: $currentLang")
    }

    @UiThread
    fun onLanguageCardClicked() {
        Log.i("$TAG Language card clicked, showing bottom sheet")
        showLanguageBottomSheetEvent.value = Event(currentLanguage.value ?: "en")
    }

    @UiThread
    fun openPermissions() {
        Log.i("$TAG Opening permissions fragment")
        navigateToPermissionsEvent.value = Event(true)
    }

    @UiThread
    fun onCallerIdCardClicked() {
        Log.i("$TAG Caller ID card clicked, showing bottom sheet")
        if (allCallerIds.isNotEmpty()) {
            showCallerIdBottomSheetEvent.value = Event(Pair(allCallerIds, currentCallerId))
        } else {
            Log.w("$TAG No caller IDs available to show")
            showFormattedRedToast("No caller IDs available", org.linphone.R.drawable.warning_circle)
        }
    }

    @UiThread
    fun chooseCallerId(callerId: org.voxnode.voxnode.models.CallerId) {
        Log.i("$TAG Choosing caller ID: ${callerId.callerID}")
        
        val loginResult = VoxNodeDataManager.getLoginResult()
        val clientId = loginResult?.clientId
        val clientKey = loginResult?.clientKey
        
        if (clientId == null || clientKey == null) {
            Log.e("$TAG Missing client ID or client key for caller ID selection")
            showFormattedRedToast("Failed to update caller ID: Missing credentials", org.linphone.R.drawable.warning_circle)
            return
        }
        
        isCallerIdLoading.value = true
        
        voxnodeRepository.chooseCallerId(
            clientId = clientId,
            callerIDId = callerId.callerIDId,
            clientKey = clientKey,
            onSuccess = { response ->
                Log.i("$TAG Successfully chose caller ID: ${callerId.callerID}")
                
                // Save the selected caller ID locally immediately after API success
                coreContext.postOnCoreThread {
                    VoxNodeDataManager.saveCurrentCallerId(callerId)
                    Log.i("$TAG Local caller ID storage updated with: ${callerId.callerID}")
                    
                    // Verify the save was successful
                    val verificationResult = VoxNodeDataManager.verifyCurrentCallerId(callerId)
                    if (verificationResult) {
                        Log.i("$TAG Caller ID local storage verification successful")
                    } else {
                        Log.w("$TAG Caller ID local storage verification failed - retrying save")
                        VoxNodeDataManager.saveCurrentCallerId(callerId)
                    }
                }
                
                coreContext.postOnMainThread {
                    isCallerIdLoading.value = false
                    currentCallerId = callerId
                    callerIdNumber.value = callerId.callerID ?: "Not available"
                    
                    // Update the isSelected status in the list
                    allCallerIds.forEach { it.isCurrentCallerID = 0 }
                    callerId.isCurrentCallerID = 1
                    
                    showFormattedGreenToast("Caller ID updated successfully", org.linphone.R.drawable.check)
                }
            },
            onError = { error ->
                Log.e("$TAG Failed to choose caller ID: $error")
                
                coreContext.postOnMainThread {
                    isCallerIdLoading.value = false
                    showFormattedRedToast("Failed to update caller ID: $error", org.linphone.R.drawable.warning_circle)
                }
            }
        )
    }

    @UiThread
    fun changeLanguage(languageCode: String) {
        Log.i("$TAG Changing language to: $languageCode")
        
        if (currentLanguage.value == languageCode) {
            Log.i("$TAG Language is already set to $languageCode")
            return
        }
        
        try {
            // Use the global language manager to change language app-wide
            LanguageManager.changeLanguageGlobally(languageCode)
            
            // Update local UI state
            currentLanguage.value = languageCode
            updateLanguageDisplayName(languageCode)
            
            Log.i("$TAG Language successfully changed globally to: $languageCode")
            
            // Show confirmation based on selected language
            val confirmationMessage = when (languageCode) {
                "fr" -> coreContext.context.getString(org.linphone.R.string.language_change_success_french)
                "en" -> coreContext.context.getString(org.linphone.R.string.language_change_success_english)
                else -> coreContext.context.getString(org.linphone.R.string.language_change_success_english)
            }
            showFormattedGreenToast(confirmationMessage, org.linphone.R.drawable.check)
            
        } catch (e: Exception) {
            Log.e("$TAG Failed to change language: ${e.message}")
            showFormattedRedToast(coreContext.context.getString(org.linphone.R.string.language_change_error), org.linphone.R.drawable.warning_circle)
        }
    }

    @UiThread
    private fun updateLanguageDisplayName(languageCode: String) {
        currentLanguageDisplayName.value = getLanguageDisplayName(languageCode)
    }

    private fun getLanguageDisplayName(languageCode: String): String {
        return LanguageManager.getLanguageDisplayName(languageCode, coreContext.context)
    }

    @UiThread
    override fun filter() {
        // No filtering needed for this simple settings page
    }
}

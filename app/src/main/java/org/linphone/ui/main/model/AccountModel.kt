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
@file:Suppress("SameReturnValue")

package org.linphone.ui.main.model

import android.view.View
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.R
import org.linphone.contacts.AbstractAvatarModel
import org.linphone.core.Account
import org.linphone.core.AccountListenerStub
import org.linphone.core.ChatMessage
import org.linphone.core.ChatRoom
import org.linphone.core.ConsolidatedPresence
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.MessageWaitingIndication
import org.linphone.core.RegistrationState
import org.linphone.core.SecurityLevel
import org.linphone.core.tools.Log
import org.linphone.utils.AppUtils
import org.linphone.utils.LinphoneUtils
import org.voxnode.voxnode.storage.VoxNodeDataManager

class AccountModel
    @WorkerThread
    constructor(
    val account: Account,
    private val onMenuClicked: ((view: View, account: Account) -> Unit)? = null
) : AbstractAvatarModel() {
    companion object {
        private const val TAG = "[Account Model]"
    }

    private var providerLogoSet = false

    val displayName = MutableLiveData<String>()

    val registrationState = MutableLiveData<RegistrationState>()

    val registrationStateLabel = MutableLiveData<String>()

    val registrationStateSummary = MutableLiveData<String>()

    val isDefault = MutableLiveData<Boolean>()

    val notificationsCount = MutableLiveData<Int>()

    val showMwi = MutableLiveData<Boolean>()

    val voicemailCount = MutableLiveData<String>()

    private val accountListener = object : AccountListenerStub() {
        @WorkerThread
        override fun onRegistrationStateChanged(
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            Log.i(
                "$TAG Account [${account.params.identityAddress?.asStringUriOnly()}] registration state changed: [$state]($message)"
            )
            update()
        }

        override fun onMessageWaitingIndicationChanged(
            account: Account,
            mwi: MessageWaitingIndication
        ) {
            Log.i(
                "$TAG Account [${account.params.identityAddress?.asStringUriOnly()}] has received a MWI NOTIFY. ${if (mwi.hasMessageWaiting()) "Message(s) are waiting." else "No message is waiting."}}"
            )
            showMwi.postValue(mwi.hasMessageWaiting())
            for (summary in mwi.summaries) {
                val context = summary.contextClass
                val nbNew = summary.nbNew
                val nbNewUrgent = summary.nbNewUrgent
                val nbOld = summary.nbOld
                val nbOldUrgent = summary.nbOldUrgent
                Log.i(
                    "$TAG [MWI] [$context]: new [$nbNew] urgent ($nbNewUrgent), old [$nbOld] urgent ($nbOldUrgent)"
                )

                voicemailCount.postValue(nbNew.toString())
            }
        }
    }

    private val coreListener = object : CoreListenerStub() {
        @WorkerThread
        override fun onChatRoomRead(core: Core, chatRoom: ChatRoom) {
            computeNotificationsCount()
        }

        @WorkerThread
        override fun onMessagesReceived(
            core: Core,
            chatRoom: ChatRoom,
            messages: Array<out ChatMessage>
        ) {
            computeNotificationsCount()
        }
    }

    init {
        account.addListener(accountListener)
        coreContext.core.addListener(coreListener)

        isDefault.postValue(false)
        presenceStatus.postValue(ConsolidatedPresence.Offline)
        showMwi.postValue(false)
        voicemailCount.postValue("")

        update()
    }

    @WorkerThread
    fun destroy() {
        coreContext.core.removeListener(coreListener)
        account.removeListener(accountListener)
    }

    @UiThread
    fun setAsDefault() {
        coreContext.postOnCoreThread { core ->
            if (core.defaultAccount != account) {
                core.defaultAccount = account

                for (friendList in core.friendsLists) {
                    if (friendList.isSubscriptionsEnabled) {
                        Log.i(
                            "$TAG Default account has changed, refreshing friend list [${friendList.displayName}] subscriptions"
                        )
                        // friendList.updateSubscriptions() won't trigger a refresh unless a friend has changed
                        friendList.isSubscriptionsEnabled = false
                        friendList.isSubscriptionsEnabled = true
                    }
                }
            }
        }

        isDefault.value = true
    }

    @UiThread
    fun openMenu(view: View) {
        onMenuClicked?.invoke(view, account)
    }

    @UiThread
    fun refreshRegister() {
        coreContext.postOnCoreThread { core ->
            core.refreshRegisters()
        }
    }

    @UiThread
    fun callVoicemailUri() {
        coreContext.postOnCoreThread {
            val voicemail = account.params.voicemailAddress
            if (voicemail != null) {
                Log.i("$TAG Calling voicemail address [${voicemail.asStringUriOnly()}]")
                coreContext.startAudioCall(voicemail)
            }
        }
    }

    @WorkerThread
    private fun update() {
        Log.i(
            "$TAG Refreshing info for account [${account.params.identityAddress?.asStringUriOnly()}]"
        )

        trust.postValue(SecurityLevel.EndToEndEncryptedAndVerified)
        showTrust.postValue(isEndToEndEncryptionMandatory())

        val name = LinphoneUtils.getDisplayName(account.params.identityAddress)
        displayName.postValue(name)

        // Check if we have a provider logo before setting initials
        val voxNodeLoginResult = VoxNodeDataManager.getLoginResult()
        val providerLogoUrl = voxNodeLoginResult?.providerLogo
        
        if (!providerLogoSet && providerLogoUrl.isNullOrEmpty()) {
            // Only set initials if we don't have a provider logo
            initials.postValue(AppUtils.getInitials(name))
            Log.d("$TAG Setting initials: ${AppUtils.getInitials(name)}")
        } else {
            // Clear initials when we have a provider logo
            initials.postValue("")
            Log.d("$TAG Clearing initials because provider logo is available")
        }

        // Check if we already have a provider logo set (from VoxNode)
        val currentPicturePath = picturePath.value.orEmpty()
        
        // Only update picture if we don't already have a provider logo set
        if (!providerLogoSet && (currentPicturePath.isEmpty())) {
            if (!providerLogoUrl.isNullOrEmpty()) {
                try {
                    picturePath.postValue(providerLogoUrl)
                    providerLogoSet = true
                    // Clear initials when provider logo is set
                    initials.postValue("")
                    Log.d("$TAG Using provider logo from VoxNode: [$providerLogoUrl] and cleared initials")
                } catch (e: Exception) {
                    Log.w("$TAG Failed to load provider logo, falling back to account picture: ${e.message}")
                    // Fall back to account picture
                    val pictureUri = account.params.pictureUri.orEmpty()
                    if (pictureUri != picturePath.value.orEmpty()) {
                        picturePath.postValue(pictureUri)
                        Log.d("$TAG Account picture URI is [$pictureUri]")
                    }
                }
            } else {
                // No provider logo available, use account picture
                val pictureUri = account.params.pictureUri.orEmpty()
                if (pictureUri != picturePath.value.orEmpty()) {
                    picturePath.postValue(pictureUri)
                    Log.d("$TAG Account picture URI is [$pictureUri]")
                }
            }
        } else {
            Log.d("$TAG Provider logo already set (flag: $providerLogoSet), keeping current picture: [$currentPicturePath]")
        }

        isDefault.postValue(coreContext.core.defaultAccount == account)
        computeNotificationsCount()

        val state = account.state
        registrationState.postValue(state)

        val label = when (state) {
            RegistrationState.None, RegistrationState.Cleared -> {
                AppUtils.getString(
                    R.string.drawer_menu_account_connection_status_cleared
                )
            }
            RegistrationState.Progress -> AppUtils.getString(
                R.string.drawer_menu_account_connection_status_progress
            )
            RegistrationState.Failed -> {
                AppUtils.getString(
                    R.string.drawer_menu_account_connection_status_failed
                )
            }
            RegistrationState.Ok -> {
                AppUtils.getString(
                    R.string.drawer_menu_account_connection_status_connected
                )
            }
            RegistrationState.Refreshing -> AppUtils.getString(
                R.string.drawer_menu_account_connection_status_refreshing
            )
            else -> "${account.state}"
        }
        registrationStateLabel.postValue(label)

        val summary = when (account.state) {
            RegistrationState.None, RegistrationState.Cleared -> AppUtils.getString(
                R.string.manage_account_status_cleared_summary
            )
            RegistrationState.Refreshing, RegistrationState.Progress -> AppUtils.getString(
                R.string.manage_account_status_progress_summary
            )
            RegistrationState.Failed -> AppUtils.getString(
                R.string.manage_account_status_failed_summary
            )
            RegistrationState.Ok -> AppUtils.getString(
                R.string.manage_account_status_connected_summary
            )
            else -> "${account.state}"
        }
        registrationStateSummary.postValue(summary)
    }

    @WorkerThread
    fun computeNotificationsCount() {
        notificationsCount.postValue(account.unreadChatMessageCount + account.missedCallsCount)
    }

    /**
     * Refreshes the avatar with the latest VoxNode provider logo
     */
    @UiThread
    fun refreshAvatarWithProviderLogo() {
        Log.d("$TAG AccountModel.refreshAvatarWithProviderLogo() called")
        coreContext.postOnCoreThread {
            try {
                Log.d("$TAG Checking VoxNode login result...")
                val voxNodeLoginResult = VoxNodeDataManager.getLoginResult()
                val providerLogoUrl = voxNodeLoginResult?.providerLogo
                
                Log.d("$TAG VoxNode login result: ${voxNodeLoginResult != null}, provider logo: $providerLogoUrl")
                
                if (!providerLogoUrl.isNullOrEmpty()) {
                    picturePath.postValue(providerLogoUrl)
                    providerLogoSet = true
                    // Clear initials when provider logo is set
                    initials.postValue("")
                    Log.d("$TAG Refreshed avatar with provider logo: [$providerLogoUrl] and cleared initials")
                } else {
                    Log.d("$TAG No provider logo available, keeping current avatar")
                }
            } catch (e: Exception) {
                Log.w("$TAG Failed to refresh avatar with provider logo: ${e.message}")
                // Keep current avatar on error
            }
        }
    }
}

@WorkerThread
fun isEndToEndEncryptionMandatory(): Boolean {
    return false // TODO: Will be done later in SDK
}

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
package org.linphone.ui.assistant.viewmodel

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import java.util.Locale
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.LinphoneApplication.Companion.corePreferences
import org.linphone.R
import org.linphone.core.Account
import org.linphone.core.AuthInfo
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.Reason
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import org.linphone.core.tools.Log
import org.linphone.ui.GenericViewModel
import org.linphone.utils.AppUtils
import org.linphone.utils.Event
import org.voxnode.voxnode.api.VoxnodeRepository

class ThirdPartySipAccountLoginViewModel
    @UiThread
    constructor() : GenericViewModel() {
    companion object {
        private const val TAG = "[Third Party SIP Account Login ViewModel]"
    }

    val username = MutableLiveData<String>()

    val password = MutableLiveData<String>()

    val showPassword = MutableLiveData<Boolean>()

    val loginEnabled = MediatorLiveData<Boolean>()

    val registrationInProgress = MutableLiveData<Boolean>()

    val accountLoggedInEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData<Event<Boolean>>()
    }

    val accountLoginErrorEvent: MutableLiveData<Event<String>> by lazy {
        MutableLiveData<Event<String>>()
    }

    val defaultTransportIndexEvent: MutableLiveData<Event<Int>> by lazy {
        MutableLiveData<Event<Int>>()
    }

    val availableTransports = arrayListOf<String>()

    private lateinit var newlyCreatedAuthInfo: AuthInfo
    private lateinit var newlyCreatedAccount: Account
    
    private val voxnodeRepository = VoxnodeRepository()

    private val coreListener = object : CoreListenerStub() {
        @WorkerThread
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            if (account == newlyCreatedAccount) {
                Log.i("$TAG Newly created account registration state is [$state] ($message)")

                if (state == RegistrationState.Ok) {
                    registrationInProgress.postValue(false)
                    core.removeListener(this)

                    // Set new account as default
                    core.defaultAccount = newlyCreatedAccount
                    accountLoggedInEvent.postValue(Event(true))
                } else if (state == RegistrationState.Failed) {
                    registrationInProgress.postValue(false)
                    core.removeListener(this)

                    val error = when (account.error) {
                        Reason.Forbidden -> {
                            AppUtils.getString(R.string.assistant_account_login_forbidden_error)
                        }
                        else -> {
                            AppUtils.getFormattedString(
                                R.string.assistant_account_login_error,
                                account.error.toString()
                            )
                        }
                    }
                    accountLoginErrorEvent.postValue(Event(error))

                    Log.e("$TAG Account failed to REGISTER [$message], removing it")
                    core.removeAuthInfo(newlyCreatedAuthInfo)
                    core.removeAccount(newlyCreatedAccount)
                }
            }
        }
    }

    init {
        showPassword.value = false
        registrationInProgress.value = false

        loginEnabled.addSource(username) {
            loginEnabled.value = isLoginButtonEnabled()
        }

        // TODO: handle formatting errors ?

        availableTransports.add(TransportType.Udp.name.uppercase(Locale.getDefault()))
        availableTransports.add(TransportType.Tcp.name.uppercase(Locale.getDefault()))
        availableTransports.add(TransportType.Tls.name.uppercase(Locale.getDefault()))

        coreContext.postOnCoreThread {

            val defaultTransport = corePreferences.thirdPartySipAccountDefaultTransport.uppercase(
                Locale.getDefault()
            )
            val index = if (defaultTransport.isNotEmpty()) {
                availableTransports.indexOf(defaultTransport)
            } else {
                availableTransports.size - 1
            }
            defaultTransportIndexEvent.postValue(Event(index))
        }
    }

    @UiThread
    fun login() {
        val usernameValue = username.value.orEmpty().trim()
        val passwordValue = password.value.orEmpty().trim()
        
        if (usernameValue.isEmpty() || passwordValue.isEmpty()) {
            accountLoginErrorEvent.postValue(Event("Username and password are required"))
            return
        }
        
        // Set loading state
        registrationInProgress.postValue(true)
        
        // Call VoxNode API login with hardcoded providerId = 1
        voxnodeRepository.login(
            email = usernameValue,
            password = passwordValue,
            providerId = 1L,
            onSuccess = { loginResult ->
                registrationInProgress.postValue(false)
                if (loginResult.status) {
                    Log.i("$TAG VoxNode login successful for user: $usernameValue")

                    // Store login credentials in preferences for future API calls

                    sipLogin(
                        sipUsername = loginResult.clientSipAddress?.split("@")?.get(0) ?: "",
                        domain = loginResult.clientSipAddress?.split("@")?.get(1) ?: "",
                        authId = "",
                        displayName = loginResult.clientSipAddress?.split("@")?.get(0) ?: "",
                        sipPassword = loginResult.clientSipPassword ?: ""
                    )
                    // Trigger success event
                    accountLoggedInEvent.postValue(Event(true))
                } else {
                    val errorMessage = loginResult.message ?: "Login failed"
                    Log.e("$TAG VoxNode login failed: $errorMessage")
                    accountLoginErrorEvent.postValue(Event(errorMessage))
                }
            },
            onError = { error ->
                registrationInProgress.postValue(false)
                Log.e("$TAG VoxNode API error: $error")
                accountLoginErrorEvent.postValue(Event(error))
            }
        )
    }

    fun sipLogin(domain: String, sipUsername: String, authId: String, displayName: String, sipPassword: String) {
        coreContext.postOnCoreThread { core ->
            core.loadConfigFromXml(corePreferences.thirdPartyDefaultValuesPath)

            // Remove sip: in front of domain, just in case...
            val domainValue = domain.trim()
            val domainWithoutSip = if (domainValue.startsWith("sip:")) {
                domainValue.substring("sip:".length)
            } else {
                domainValue
            }
            val domainAddress = Factory.instance().createAddress("sip:$domainWithoutSip")
            val port = domainAddress?.port ?: -1
            if (port != -1) {
                Log.w("$TAG It seems a port [$port] was set in the domain [$domainValue], removing it from SIP identity but setting it to proxy server URI")
            }
            val domain = domainAddress?.domain ?: domainWithoutSip

            // Allow to enter SIP identity instead of simply username
            // in case identity domain doesn't match proxy domain
            var user = sipUsername.trim()
            if (user.startsWith("sip:")) {
                user = user.substring("sip:".length)
            } else if (user.startsWith("sips:")) {
                user = user.substring("sips:".length)
            }
            if (user.contains("@")) {
                user = user.split("@")[0]
            }

            val userId = authId.trim()

            Log.i("$TAG Parsed username is [$user], user ID [$userId] and domain [$domain]")
            val identity = "sip:$user@$domain"
            val identityAddress = Factory.instance().createAddress(identity)
            if (identityAddress == null) {
                Log.e("$TAG Can't parse [$identity] as Address!")
                showRedToast(R.string.assistant_login_cant_parse_address_toast, R.drawable.warning_circle)
                return@postOnCoreThread
            }
            Log.i("$TAG Computed SIP identity is [${identityAddress.asStringUriOnly()}]")

            val accounts = core.accountList
            val found = accounts.find {
                it.params.identityAddress?.weakEqual(identityAddress) == true
            }
            if (found != null) {
                Log.w("$TAG An account with the same identity address [${found.params.identityAddress?.asStringUriOnly()}] already exists, do not add it again!")
                showRedToast(R.string.assistant_account_login_already_connected_error, R.drawable.warning_circle)
                return@postOnCoreThread
            }

            newlyCreatedAuthInfo = Factory.instance().createAuthInfo(
                user,
                userId,
                sipPassword.trim(),
                null,
                null,
                domainAddress?.domain ?: domainValue
            )
            core.addAuthInfo(newlyCreatedAuthInfo)

            val accountParams = core.createAccountParams()

            if (displayName.isNotEmpty()) {
                identityAddress.displayName = displayName.trim()
            }
            accountParams.identityAddress = identityAddress

            val serverAddress = domainAddress ?: Factory.instance().createAddress("sip:$domainWithoutSip")

            serverAddress?.transport = TransportType.Udp
            Log.i("$TAG Created proxy server SIP address [${serverAddress?.asStringUriOnly()}]")
            accountParams.serverAddress = serverAddress

            newlyCreatedAccount = core.createAccount(accountParams)

            registrationInProgress.postValue(true)
            core.addListener(coreListener)
            core.addAccount(newlyCreatedAccount)
        }
    }

    @UiThread
    fun toggleShowPassword() {
        showPassword.value = showPassword.value == false
    }

    @UiThread
    private fun isLoginButtonEnabled(): Boolean {
        // Password isn't mandatory as authentication could be Bearer
        return username.value.orEmpty().isNotEmpty() && password.value.orEmpty().isNotEmpty()
    }
}

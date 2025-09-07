package org.voxnode.voxnode.storage

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.linphone.LinphoneApplication.Companion.corePreferences
import org.linphone.core.tools.Log
import org.voxnode.voxnode.api.VoxnodeRepository
import org.voxnode.voxnode.models.CallerId
import org.voxnode.voxnode.models.LoginResult
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

/**
 * VoxNodeDataManager handles the storage and retrieval of VoxNode user data.
 * It uses both CorePreferences for essential data and JSON file storage for complete LoginResult.
 */
class VoxNodeDataManager {
    companion object {
        private const val TAG = "[VoxNode Data Manager]"
        
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        /**
         * Saves the complete LoginResult to both CorePreferences (for quick access) and JSON file (for complete data)
         */
        @WorkerThread
        fun saveLoginResult(loginResult: LoginResult) {
            try {
                Log.i("$TAG Saving VoxNode login result")
                
                // Save essential data to CorePreferences for quick access
                corePreferences.voxnodeUserEmail = loginResult.clientEmail ?: ""
                corePreferences.voxnodeClientId = loginResult.clientId ?: -1
                corePreferences.voxnodeClientKey = loginResult.clientKey ?: ""
                corePreferences.voxnodeSipAddress = loginResult.clientSipAddress ?: ""
                corePreferences.voxnodeUrlRecharge = loginResult.urlRecharge ?: ""
                
                // Save complete LoginResult as JSON
                val jsonFile = File(corePreferences.voxnodeDataFile)
                val jsonString = gson.toJson(loginResult)
                
                FileWriter(jsonFile).use { writer ->
                    writer.write(jsonString)
                }
                
                Log.i("$TAG VoxNode login result saved successfully")
            } catch (e: IOException) {
                Log.e("$TAG Failed to save VoxNode login result: ${e.message}")
            } catch (e: Exception) {
                Log.e("$TAG Unexpected error saving VoxNode login result: ${e.message}")
            }
        }

        /**
         * Retrieves the complete LoginResult from JSON file storage
         */
        @WorkerThread
        fun getLoginResult(): LoginResult? {
            return try {
                val jsonFile = File(corePreferences.voxnodeDataFile)
                if (!jsonFile.exists()) {
                    Log.w("$TAG VoxNode data file does not exist")
                    return null
                }
                
                FileReader(jsonFile).use { reader ->
                    val loginResult = gson.fromJson(reader, LoginResult::class.java)
                    Log.i("$TAG VoxNode login result retrieved successfully")
                    loginResult
                }
            } catch (e: IOException) {
                Log.e("$TAG Failed to read VoxNode login result: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e("$TAG Unexpected error reading VoxNode login result: ${e.message}")
                null
            }
        }

        /**
         * Checks if user is logged in to VoxNode
         */
        @WorkerThread
        fun isUserLoggedIn(): Boolean {
            return corePreferences.voxnodeClientId > 0 && 
                   corePreferences.voxnodeUserEmail.isNotEmpty() &&
                   corePreferences.voxnodeClientKey.isNotEmpty()
        }

        /**
         * Gets the user's email from preferences (quick access)
         */
        @WorkerThread
        fun getUserEmail(): String {
            return corePreferences.voxnodeUserEmail
        }

        /**
         * Gets the client ID from preferences (quick access)
         */
        @WorkerThread
        fun getClientId(): Int {
            return corePreferences.voxnodeClientId
        }

        /**
         * Gets the client key from preferences (quick access)
         */
        @WorkerThread
        fun getClientKey(): String {
            return corePreferences.voxnodeClientKey
        }

        /**
         * Gets the SIP address from preferences (quick access)
         */
        @WorkerThread
        fun getSipAddress(): String {
            return corePreferences.voxnodeSipAddress
        }

        /**
         * Gets the recharge URL from preferences (quick access)
         */
        @WorkerThread
        fun getRechargeUrl(): String {
            return corePreferences.voxnodeUrlRecharge
        }

        /**
         * Clears all VoxNode data (logout)
         */
        @WorkerThread
        fun clearLoginData() {
            try {
                Log.i("$TAG Clearing VoxNode login data")
                
                // Clear CorePreferences
                corePreferences.voxnodeUserEmail = ""
                corePreferences.voxnodeClientId = -1
                corePreferences.voxnodeClientKey = ""
                corePreferences.voxnodeSipAddress = ""
                corePreferences.voxnodeUrlRecharge = ""
                corePreferences.voxnodeCurrentCallerId = ""
                corePreferences.voxnodeCurrentCallerIdId = -1
                
                // Delete JSON file
                val jsonFile = File(corePreferences.voxnodeDataFile)
                if (jsonFile.exists()) {
                    jsonFile.delete()
                }
                
                Log.i("$TAG VoxNode login data cleared successfully")
            } catch (e: Exception) {
                Log.e("$TAG Failed to clear VoxNode login data: ${e.message}")
            }
        }

        /**
         * Updates the client balance in the stored LoginResult
         */
        @WorkerThread
        fun updateClientBalance(newBalance: Double) {
            try {
                val loginResult = getLoginResult()
                if (loginResult != null) {
                    loginResult.clientBalance = newBalance
                    saveLoginResult(loginResult)
                    Log.i("$TAG Client balance updated to $newBalance")
                } else {
                    Log.w("$TAG Cannot update balance - no login result found")
                }
            } catch (e: Exception) {
                Log.e("$TAG Failed to update client balance: ${e.message}")
            }
        }

        /**
         * Saves the current caller ID to CorePreferences for quick access
         */
        @WorkerThread
        fun saveCurrentCallerId(callerId: CallerId) {
            try {
                Log.i("$TAG Saving current caller ID: ${callerId.callerID} (ID: ${callerId.callerIDId})")
                corePreferences.voxnodeCurrentCallerId = callerId.callerID ?: ""
                corePreferences.voxnodeCurrentCallerIdId = callerId.callerIDId
                
                // Verify the save was successful
                val savedCallerId = corePreferences.voxnodeCurrentCallerId
                val savedCallerIdId = corePreferences.voxnodeCurrentCallerIdId
                Log.i("$TAG Current caller ID saved successfully - Verified: $savedCallerId (ID: $savedCallerIdId)")
            } catch (e: Exception) {
                Log.e("$TAG Failed to save current caller ID: ${e.message}")
            }
        }

        /**
         * Gets the current caller ID from CorePreferences (quick access)
         */
        @WorkerThread
        fun getCurrentCallerId(): String {
            return corePreferences.voxnodeCurrentCallerId
        }

        /**
         * Gets the current caller ID ID from CorePreferences (quick access)
         */
        @WorkerThread
        fun getCurrentCallerIdId(): Int {
            return corePreferences.voxnodeCurrentCallerIdId
        }

        /**
         * Gets the current caller ID as a CallerId object
         */
        @WorkerThread
        fun getCurrentCallerIdObject(): CallerId? {
            val callerIdString = getCurrentCallerId()
            val callerIdId = getCurrentCallerIdId()
            
            if (callerIdString.isNotEmpty() && callerIdId != -1) {
                val callerId = CallerId()
                callerId.callerID = callerIdString
                callerId.callerIDId = callerIdId
                callerId.isCurrentCallerID = 1
                return callerId
            }
            return null
        }

        /**
         * Clears the current caller ID data
         */
        @WorkerThread
        fun clearCurrentCallerId() {
            try {
                Log.i("$TAG Clearing current caller ID data")
                corePreferences.voxnodeCurrentCallerId = ""
                corePreferences.voxnodeCurrentCallerIdId = -1
                Log.i("$TAG Current caller ID data cleared successfully")
            } catch (e: Exception) {
                Log.e("$TAG Failed to clear current caller ID data: ${e.message}")
            }
        }

        /**
         * Verifies that the current caller ID matches the expected caller ID
         */
        @WorkerThread
        fun verifyCurrentCallerId(expectedCallerId: CallerId): Boolean {
            val storedCallerId = getCurrentCallerId()
            val storedCallerIdId = getCurrentCallerIdId()
            
            val matches = storedCallerId == (expectedCallerId.callerID ?: "") && 
                         storedCallerIdId == expectedCallerId.callerIDId
            
            Log.i(
                "$TAG Caller ID verification: Expected ${expectedCallerId.callerID} (ID: ${expectedCallerId.callerIDId}), " +
                  "Stored $storedCallerId (ID: $storedCallerIdId), Match: $matches"
            )
            
            return matches
        }

        /**
         * Fetches caller IDs from API and saves the current one locally
         * This should be called after successful login
         */
        @WorkerThread
        fun fetchAndSaveCurrentCallerId(
            loginResult: LoginResult,
            onSuccess: (() -> Unit)? = null,
            onError: ((String) -> Unit)? = null
        ) {
            try {
                val clientId = loginResult.clientId ?: -1
                val clientKey = loginResult.clientKey ?: ""
                val providerId = loginResult.providerId ?: 1

                if (clientId == -1 || clientKey.isEmpty()) {
                    Log.e("$TAG Missing client ID or client key for caller ID fetch")
                    onError?.invoke("Missing client credentials")
                    return
                }

                Log.i("$TAG Fetching caller IDs for client ID: $clientId")

                val voxnodeRepository = VoxnodeRepository()
                voxnodeRepository.getCallerIds(
                    providerId = providerId,
                    clientId = clientId,
                    clientKey = clientKey,
                    onSuccess = { callerIds ->
                        Log.i("$TAG Successfully fetched ${callerIds.size} caller IDs")
                        
                        // Find the current/selected caller ID
                        val currentCallerId = callerIds.find { it.isSelected }
                        
                        if (currentCallerId != null) {
                            saveCurrentCallerId(currentCallerId)
                            Log.i("$TAG Current caller ID saved: ${currentCallerId.callerID}")
                        } else {
                            // If no current caller ID is selected, use the first one
                            val firstCallerId = callerIds.firstOrNull()
                            if (firstCallerId != null) {
                                saveCurrentCallerId(firstCallerId)
                                Log.i("$TAG No current caller ID selected, saved first one: ${firstCallerId.callerID}")
                            } else {
                                Log.w("$TAG No caller IDs available")
                            }
                        }
                        
                        onSuccess?.invoke()
                    },
                    onError = { error ->
                        Log.e("$TAG Failed to fetch caller IDs: $error")
                        onError?.invoke(error)
                    }
                )
            } catch (e: Exception) {
                Log.e("$TAG Failed to fetch and save caller ID: ${e.message}")
                onError?.invoke("Failed to fetch caller ID: ${e.message}")
            }
        }
    }
}

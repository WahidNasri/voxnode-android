package org.voxnode.voxnode.api

/**
 * Example usage of the VoxNode API
 * 
 * This class demonstrates how to use the VoxNode API with Retrofit.
 * Remove this file after you understand how to use the API.
 */
class VoxnodeApiExample {
    
    private val repository = VoxnodeRepository()
    
    fun exampleUsage() {
        // Example 1: Get available providers
        getProviders()
        
        // Example 2: Login with credentials
        loginUser()
        
        // Example 3: Make an outbound call
        makeCall()
        
        // Example 4: Get caller IDs
        getCallerIds()
        
        // Example 5: Send SMS
        sendSms()
    }
    
    private fun getProviders() {
        repository.getProviders(
            onSuccess = { providers ->
                // Handle successful response
                println("Retrieved ${providers.size} providers")
                providers.forEach { provider ->
                    println("Provider: ${provider.name} - ${provider.displayName}")
                }
            },
            onError = { error ->
                // Handle error
                println("Error getting providers: $error")
            }
        )
    }
    
    private fun loginUser() {
        repository.login(
            email = "user@example.com",
            password = "password123",
            providerId = 1L,
            onSuccess = { loginResult ->
                if (loginResult.success) {
                    // Login successful
                    val clientId = loginResult.clientId
                    val clientKey = loginResult.clientKey
                    val providerId = loginResult.providerId
                    
                    // Store these values for future API calls
                    println("Login successful! Client ID: $clientId")
                } else {
                    println("Login failed: ${loginResult.message}")
                }
            },
            onError = { error ->
                println("Login error: $error")
            }
        )
    }
    
    private fun makeCall() {
        repository.makeOutboundCall(
            providerId = 1,
            clientId = 12345,
            clientKey = "your_client_key",
            number = "+1234567890",
            cli = "+0987654321",
            onSuccess = { response ->
                if (response.success) {
                    println("Call initiated successfully! Call ID: ${response.callId}")
                } else {
                    println("Call failed: ${response.message}")
                }
            },
            onError = { error ->
                println("Call error: $error")
            }
        )
    }
    
    private fun getCallerIds() {
        repository.getCallerIds(
            providerId = 1,
            clientId = 12345,
            clientKey = "your_client_key",
            onSuccess = { callerIds ->
                println("Retrieved ${callerIds.size} caller IDs")
                callerIds.forEach { callerId ->
                    println("Caller ID: ${callerId.callerId} - Verified: ${callerId.isVerified}")
                }
            },
            onError = { error ->
                println("Error getting caller IDs: $error")
            }
        )
    }
    
    private fun sendSms() {
        repository.sendSms(
            providerId = 1,
            clientId = 12345,
            clientKey = "your_client_key",
            number = "+1234567890",
            message = "Hello from VoxNode!",
            onSuccess = { response ->
                if (response.success) {
                    println("SMS sent successfully!")
                } else {
                    println("SMS failed: ${response.message}")
                }
            },
            onError = { error ->
                println("SMS error: $error")
            }
        )
    }
}

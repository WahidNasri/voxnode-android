package org.voxnode.voxnode.api

import org.voxnode.voxnode.models.BaseApiResponse
import org.voxnode.voxnode.models.CallerId
import org.voxnode.voxnode.models.LoginResult
import org.voxnode.voxnode.models.OutbountResponse
import org.voxnode.voxnode.models.VerifyCallerIdResponse
import org.voxnode.voxnode.models.VoipProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VoxnodeRepository {
    private val apiService = ApiClient.apiService

    fun getProviders(
        onSuccess: (List<VoipProvider>) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.getProviders().enqueue(object : Callback<List<VoipProvider>> {
            override fun onResponse(call: Call<List<VoipProvider>>, response: Response<List<VoipProvider>>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) }
                } else {
                    onError("Failed to get providers: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<VoipProvider>>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }

    fun login(
        email: String,
        password: String,
        providerId: Long,
        onSuccess: (LoginResult) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.loginApi(email, password, providerId).enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) }
                } else {
                    onError("Login failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }

    fun makeOutboundCall(
        providerId: Int,
        clientId: Int,
        clientKey: String,
        number: String,
        cli: String,
        onSuccess: (OutbountResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.outboundApi(providerId, clientId, clientKey, number, cli)
            .enqueue(object : Callback<OutbountResponse> {
                override fun onResponse(call: Call<OutbountResponse>, response: Response<OutbountResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("Outbound call failed: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<OutbountResponse>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
    }

    fun getCallerIds(
        providerId: Int,
        clientId: Int,
        clientKey: String,
        onSuccess: (List<CallerId>) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.getCallerIdApi(providerId, clientId, clientKey)
            .enqueue(object : Callback<List<CallerId>> {
                override fun onResponse(call: Call<List<CallerId>>, response: Response<List<CallerId>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("Failed to get caller IDs: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<List<CallerId>>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
    }

    fun chooseCallerId(
        clientId: Int,
        callerIDId: Int,
        clientKey: String,
        onSuccess: (BaseApiResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.chooseCallerIdApi(clientId, callerIDId, clientKey)
            .enqueue(object : Callback<BaseApiResponse> {
                override fun onResponse(call: Call<BaseApiResponse>, response: Response<BaseApiResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("Failed to choose caller ID: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<BaseApiResponse>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
    }

    fun addCallerId(
        clientId: Int,
        callerId: String,
        clientKey: String,
        onSuccess: (CallerId) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.addCallerIdApi(clientId, callerId, clientKey)
            .enqueue(object : Callback<CallerId> {
                override fun onResponse(call: Call<CallerId>, response: Response<CallerId>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("Failed to add caller ID: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<CallerId>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
    }

    fun verifyCallerId(
        code: String,
        clientId: Int,
        callerIDId: Int,
        clientKey: String,
        onSuccess: (VerifyCallerIdResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.verifyCallerIdApi(code, clientId, callerIDId, clientKey)
            .enqueue(object : Callback<VerifyCallerIdResponse> {
                override fun onResponse(call: Call<VerifyCallerIdResponse>, response: Response<VerifyCallerIdResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("Failed to verify caller ID: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<VerifyCallerIdResponse>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
    }

    fun deleteCallerId(
        clientId: Int,
        callerIDId: Int,
        clientKey: String,
        onSuccess: (BaseApiResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.deleteCallerIdApi(clientId, callerIDId, clientKey)
            .enqueue(object : Callback<BaseApiResponse> {
                override fun onResponse(call: Call<BaseApiResponse>, response: Response<BaseApiResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("Failed to delete caller ID: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<BaseApiResponse>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
    }

    fun sendSms(
        providerId: Int,
        clientId: Int,
        clientKey: String,
        number: String,
        message: String,
        onSuccess: (BaseApiResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.sendSmsApi(providerId, clientId, clientKey, number, message)
            .enqueue(object : Callback<BaseApiResponse> {
                override fun onResponse(call: Call<BaseApiResponse>, response: Response<BaseApiResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("Failed to send SMS: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<BaseApiResponse>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
    }
}

package org.voxnode.voxnode.api

import org.voxnode.voxnode.models.BaseApiResponse
import org.voxnode.voxnode.models.CallerId
import org.voxnode.voxnode.models.LoginResult
import org.voxnode.voxnode.models.OutbountResponse
import org.voxnode.voxnode.models.VerifyCallerIdResponse
import org.voxnode.voxnode.models.VoipProvider
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {
    @GET("getProviders")
    fun getProviders(): Call<List<VoipProvider>>

    @FormUrlEncoded
    @POST("login")
    fun loginApi(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("providerId") providerId: Long
    ): Call<LoginResult>

    @FormUrlEncoded
    @POST("outbound")
    fun outboundApi(
        @Field("providerId") providerId: Int,
        @Field("clientId") clientId: Int,
        @Field("clientKey") clientKey: String,
        @Field("number") number: String,
        @Field("cli") cli: String
    ): Call<OutbountResponse>

    @FormUrlEncoded
    @POST("getCallerIDs")
    fun getCallerIdApi(
        @Field("providerId") providerId: Int,
        @Field("clientId") clientId: Int,
        @Field("clientKey") clientKey: String
    ): Call<List<CallerId>>

    @FormUrlEncoded
    @POST("chooseCallerID")
    fun chooseCallerIdApi(
        @Field("clientId") clientId: Int,
        @Field("callerIDId") callerIDId: Int,
        @Field("clientKey") clientKey: String
    ): Call<BaseApiResponse>

    @FormUrlEncoded
    @POST("addCallerID")
    fun addCallerIdApi(
        @Field("clientId") clientId: Int,
        @Field("callerID") callerId: String,
        @Field("clientKey") clientKey: String
    ): Call<CallerId>

    @FormUrlEncoded
    @POST("verifyCallerID")
    fun verifyCallerIdApi(
        @Field("code") code: String,
        @Field("clientId") clientId: Int,
        @Field("callerIDId") callerIDId: Int,
        @Field("clientKey") clientKey: String
    ): Call<VerifyCallerIdResponse>

    @FormUrlEncoded
    @POST("deleteCallerID")
    fun deleteCallerIdApi(
        @Field("clientId") clientId: Int,
        @Field("callerIDId") callerIDId: Int,
        @Field("clientKey") clientKey: String
    ): Call<BaseApiResponse>

    @FormUrlEncoded
    @POST("sms")
    fun sendSmsApi(
        @Field("providerId") providerId: Int,
        @Field("clientId") clientId: Int,
        @Field("clientKey") clientKey: String,
        @Field("number") number: String,
        @Field("message") message: String
    ): Call<BaseApiResponse>
}

package org.voxnode.voxnode.models

import com.google.gson.annotations.SerializedName

data class LoginResult(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("clientId")
    val clientId: Int = 0,

    @SerializedName("clientKey")
    val clientKey: String? = null,

    @SerializedName("providerId")
    val providerId: Int = 0,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("userInfo")
    val userInfo: UserInfo? = null
)

data class UserInfo(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null
)

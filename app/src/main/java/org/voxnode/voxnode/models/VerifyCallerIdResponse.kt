package org.voxnode.voxnode.models

import com.google.gson.annotations.SerializedName

data class VerifyCallerIdResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("verified")
    val verified: Boolean = false,

    @SerializedName("callerId")
    val callerId: CallerId? = null
)

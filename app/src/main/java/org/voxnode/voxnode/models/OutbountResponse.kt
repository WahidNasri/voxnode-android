package org.voxnode.voxnode.models

import com.google.gson.annotations.SerializedName

data class OutbountResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("callId")
    val callId: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("duration")
    val duration: Int = 0,

    @SerializedName("cost")
    val cost: Double = 0.0
)

package org.voxnode.voxnode.models

import com.google.gson.annotations.SerializedName

data class BaseApiResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null
)

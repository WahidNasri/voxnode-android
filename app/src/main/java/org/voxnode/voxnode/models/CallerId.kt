package org.voxnode.voxnode.models

import com.google.gson.annotations.SerializedName

data class CallerId(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("callerId")
    val callerId: String? = null,

    @SerializedName("isVerified")
    val isVerified: Boolean = false,

    @SerializedName("isDefault")
    val isDefault: Boolean = false,

    @SerializedName("clientId")
    val clientId: Int = 0,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

package org.voxnode.voxnode.models

import com.google.gson.annotations.SerializedName

data class VoipProvider(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("logoUrl")
    val logoUrl: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean = false,

    @SerializedName("supportedFeatures")
    val supportedFeatures: List<String>? = null,

    @SerializedName("settings")
    val settings: ProviderSettings? = null
)

data class ProviderSettings(
    @SerializedName("domain")
    val domain: String? = null,

    @SerializedName("port")
    val port: Int = 0,

    @SerializedName("transport")
    val transport: String? = null,

    @SerializedName("codec")
    val codec: String? = null
)

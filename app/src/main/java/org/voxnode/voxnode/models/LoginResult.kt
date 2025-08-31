package org.voxnode.voxnode.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginResult {
    @SerializedName("status")
    @Expose
    var status: Boolean = false

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("language")
    @Expose
    var language: String? = null

    @SerializedName("clientId")
    @Expose
    var clientId: Int? = null

    @SerializedName("clientEmail")
    @Expose
    var clientEmail: String? = null

    @SerializedName("clientKey")
    @Expose
    var clientKey: String? = null

    @SerializedName("clientSmsEnabled")
    @Expose
    var clientSmsEnabled: Int? = null

    @SerializedName("clientBalanceEnabled")
    @Expose
    var clientBalanceEnabled: Int? = null

    @SerializedName("clientInboundEnabled")
    @Expose
    var clientInboundEnabled: Int? = null

    @SerializedName("clientOutboundEnabled")
    @Expose
    var clientOutboundEnabled: Int? = null

    @SerializedName("clientRecordingEnabled")
    @Expose
    var clientRecordingEnabled: Int? = null

    @SerializedName("clientBalance")
    @Expose
    var clientBalance: Double? = null
        get() = if (field != null) field else 0.0

    @SerializedName("clientSipAddress")
    @Expose
    var clientSipAddress: String? = null

    @SerializedName("clientSipPassword")
    @Expose
    var clientSipPassword: String? = null

    @SerializedName("providerId")
    @Expose
    var providerId: Int? = null

    @SerializedName("providerName")
    @Expose
    var providerName: String? = null

    @SerializedName("providerSite")
    @Expose
    var providerSite: String? = null

    @SerializedName("providerPhone")
    @Expose
    var providerPhone: String? = null

    @SerializedName("providerEmail")
    @Expose
    var providerEmail: String? = null

    @SerializedName("providerLogo")
    @Expose
    var providerLogo: String? = null

    @SerializedName("providerColor1")
    @Expose
    var providerColor1: String? = null

    @SerializedName("providerColor2")
    @Expose
    var providerColor2: String? = null

    @SerializedName("urlRecharge")
    @Expose
    var urlRecharge: String? = null
}

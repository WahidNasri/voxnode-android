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

    @SerializedName("urlRecharge")
    @Expose
    var urlRecharge: String? = null
}

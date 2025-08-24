package org.voxnode.voxnode.models

class CallerId {
    var callerIDId: Int = 0
    var callerID: String? = null
    var authorized: Int = 0
    var isCurrentCallerID: Int = 0
    var isStatus: Boolean = false
    var message: String? = null

    fun isAuthorized(): Boolean {
        return authorized == 1
    }

    val isSelected: Boolean
        get() = isCurrentCallerID == 1
}

package me.proton.android.pass.notifications.fakes

import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType

class TestSnackbarMessage : SnackbarMessage {
    override val id: Int
        get() = 1
    override val type: SnackbarType
        get() = SnackbarType.SUCCESS
    override val isClipboard: Boolean
        get() = false

}

package proton.android.pass.notifications.fakes

import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

class TestSnackbarMessage : SnackbarMessage {
    override val id: Int
        get() = 1
    override val type: SnackbarType
        get() = SnackbarType.SUCCESS
    override val isClipboard: Boolean
        get() = false

}

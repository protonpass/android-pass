package proton.android.pass.notifications.fakes

import proton.android.pass.notifications.api.ToastManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestToastManager @Inject constructor() : ToastManager {

    override fun showToast(message: String) {
        // no op
    }

    override fun showToast(message: Int) {
        // no op
    }
}

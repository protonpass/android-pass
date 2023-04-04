package proton.android.pass.notifications.implementation

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.notifications.api.ToastManager
import javax.inject.Inject

class ToastManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ToastManager {

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun showToast(message: Int) {
        context.resources.getString(message).let {
            showToast(it)
        }
    }
}

package me.proton.pass.autofill

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import me.proton.android.pass.autofill.api.AutofillManager
import me.proton.android.pass.autofill.api.AutofillStatus
import me.proton.android.pass.autofill.api.AutofillSupportedStatus.Supported
import me.proton.android.pass.autofill.api.AutofillSupportedStatus.Unsupported
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class AutofillManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AutofillManager {

    override fun getAutofillStatus() = flow {
        val autofillManager =
            context.getSystemService(android.view.autofill.AutofillManager::class.java)
        if (autofillManager == null) {
            emit(Unsupported)
            return@flow
        }

        if (!autofillManager.isAutofillSupported) {
            emit(Unsupported)
            return@flow
        }

        while (currentCoroutineContext().isActive) {
            if (autofillManager.hasEnabledAutofillServices()) {
                emit(Supported(AutofillStatus.EnabledByOurService))
            } else if (autofillManager.isEnabled) {
                emit(Supported(AutofillStatus.EnabledByOtherService))
            } else {
                emit(Supported(AutofillStatus.Disabled))
            }

            delay(UPDATE_TIME)
        }
    }

    override fun openAutofillSelector() {
        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    }

    override fun disableAutofill() {
        val autofillManager =
            context.getSystemService(android.view.autofill.AutofillManager::class.java)
        autofillManager?.disableAutofillServices()
    }

    companion object {
        private val UPDATE_TIME = 2L.seconds
    }
}

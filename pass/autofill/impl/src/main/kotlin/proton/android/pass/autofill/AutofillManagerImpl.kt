package proton.android.pass.autofill

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus.Supported
import proton.android.pass.autofill.api.AutofillSupportedStatus.Unsupported
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class AutofillManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AutofillManager {

    override fun getAutofillStatus(): Flow<AutofillSupportedStatus> = flow {
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
    }.distinctUntilChanged()

    override fun openAutofillSelector() {
        try {
            if (canOpenAutofillSelector()) {
                val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                PassLogger.i(TAG, "Could not open autofill selector")
            }
        } catch (e: ActivityNotFoundException) {
            PassLogger.i(TAG, e, "Could not open autofill selector")
        }
    }

    private fun canOpenAutofillSelector(): Boolean {
        val autofillManager: android.view.autofill.AutofillManager? =
            context.getSystemService(android.view.autofill.AutofillManager::class.java)
        val hasEnabledAutofillServices = autofillManager?.hasEnabledAutofillServices() ?: false
        val isAutofillSupported = autofillManager?.isAutofillSupported ?: false
        return !hasEnabledAutofillServices && isAutofillSupported
    }

    override fun disableAutofill() {
        val autofillManager =
            context.getSystemService(android.view.autofill.AutofillManager::class.java)
        autofillManager?.disableAutofillServices()
    }

    companion object {
        private const val TAG = "AutofillManagerImpl"
        private val UPDATE_TIME = 2L.seconds
    }
}

/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus.Supported
import proton.android.pass.autofill.api.AutofillSupportedStatus.Unsupported
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import android.view.autofill.AutofillManager as AndroidAutofillManager

@Singleton
class AutofillManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AutofillManager {

    private val autofillManager: AndroidAutofillManager? =
        context.getSystemService(AndroidAutofillManager::class.java)

    override fun getAutofillStatus(): Flow<AutofillSupportedStatus> = flow {
        when {
            autofillManager == null -> {
                PassLogger.d(TAG, "AutofillManager is null")
                emit(Unsupported)
            }

            !autofillManager.isAutofillSupported -> {
                PassLogger.d(TAG, "Autofill is not supported")
                emit(Unsupported)
            }

            else -> while (currentCoroutineContext().isActive) {
                runCatching {
                    withTimeout(UPDATE_TIME.inWholeMilliseconds) {
                        if (getHasEnabledAutofillServices()) {
                            emit(Supported(AutofillStatus.EnabledByOurService))
                        } else if (autofillManager.isEnabled) {
                            emit(Supported(AutofillStatus.EnabledByOtherService))
                        } else {
                            emit(Supported(AutofillStatus.Disabled))
                        }
                    }
                }.onFailure {
                    PassLogger.d(TAG, it, "Exception while retrieving hasEnabledAutofillServices")
                    emit(Unsupported)
                }
                delay(UPDATE_TIME.inWholeMilliseconds)
            }
        }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    private suspend fun getHasEnabledAutofillServices(): Boolean =
        runInterruptible { autofillManager?.hasEnabledAutofillServices() ?: false }

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

    private fun canOpenAutofillSelector(): Boolean = runCatching {
        val hasEnabledAutofillServices = runBlocking {
            withTimeout(UPDATE_TIME.inWholeMilliseconds) {
                getHasEnabledAutofillServices()
            }
        }
        val isAutofillSupported = autofillManager?.isAutofillSupported ?: false
        !hasEnabledAutofillServices && isAutofillSupported
    }.getOrElse {
        PassLogger.w(TAG, "Error while checking if autofill selector can be opened")
        PassLogger.w(TAG, it)
        false
    }

    override fun disableAutofill() {
        runCatching {
            autofillManager?.disableAutofillServices()
        }.onSuccess {
            PassLogger.i(TAG, "Disabled autofill services")
        }.onFailure {
            PassLogger.w(TAG, "Could not disable autofill services")
            PassLogger.w(TAG, it)
        }
    }

    companion object {
        private const val TAG = "AutofillManagerImpl"
        private val UPDATE_TIME = 2L.seconds
    }
}

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

import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.autofill.debug.AutofillDebugSaver
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@AndroidEntryPoint
class ProtonPassAutofillService : AutofillService() {

    @Inject
    lateinit var autofillServiceManager: AutofillServiceManager

    @Inject
    lateinit var telemetryManager: TelemetryManager

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var thirdPartyModeProvider: ThirdPartyModeProvider

    @Inject
    lateinit var ffRepo: FeatureFlagsPreferencesRepository

    override fun onConnected() {
        super.onConnected()
        PassLogger.i(TAG, "Autofill service connected")
    }

    override fun onDisconnected() {
        PassLogger.i(TAG, "Autofill service disconnected")
        super.onDisconnected()
    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val requestFlags: List<RequestFlags> = RequestFlags.fromValue(request.flags)
        if (requestFlags.isNotEmpty()) {
            PassLogger.i(TAG, "onFillRequest request flags: $requestFlags")
        }
        runBlocking {
            val isDebugMode = ffRepo.get<Boolean>(FeatureFlag.AUTOFILL_DEBUG_MODE).first()
            if (isDebugMode) {
                AutofillDebugSaver.save(this@ProtonPassAutofillService, request)
            }
        }

        AutoFillHandler.handleAutofill(
            context = this@ProtonPassAutofillService,
            request = request,
            callback = callback,
            cancellationSignal = cancellationSignal,
            autofillServiceManager = autofillServiceManager,
            telemetryManager = telemetryManager,
            accountManager = accountManager,
            thirdPartyModeProvider = thirdPartyModeProvider
        )
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        AutoSaveHandler.handleOnSave(this, request, callback)
    }

    companion object {
        private const val TAG = "ProtonPassAutofillService"
    }
}

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

package proton.android.pass.autofill.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAutofillManager @Inject constructor() : AutofillManager {

    private val statusFlow: MutableStateFlow<AutofillSupportedStatus> =
        MutableStateFlow(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService))

    override fun getAutofillStatus(): Flow<AutofillSupportedStatus> = statusFlow

    fun emitStatus(status: AutofillSupportedStatus) {
        statusFlow.tryEmit(status)
    }

    override fun openAutofillSelector() = Unit

    override fun disableAutofill() = Unit
}

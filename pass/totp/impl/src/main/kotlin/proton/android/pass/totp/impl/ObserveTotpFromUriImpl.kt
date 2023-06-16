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

package proton.android.pass.totp.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class ObserveTotpFromUriImpl @Inject constructor(
    private val totpManager: TotpManager
) : ObserveTotpFromUri {

    override fun invoke(uri: String): Flow<TotpManager.TotpWrapper> =
        totpManager.parse(uri)
            .fold(
                onSuccess = { totpManager.observeCode(it) },
                onFailure = {
                    flow { throw it }
                }
            )
}


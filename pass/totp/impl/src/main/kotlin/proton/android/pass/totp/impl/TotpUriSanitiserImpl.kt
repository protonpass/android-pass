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

import proton.android.pass.commonrust.TotpException
import proton.android.pass.commonrust.TotpUriSanitizer
import javax.inject.Inject

class TotpUriSanitiserImpl @Inject constructor() : TotpUriSanitiser {

    private val totpUriSanitizer by lazy { TotpUriSanitizer() }

    override fun sanitiseToEdit(originalUri: String): Result<String> =
        runCatching { totpUriSanitizer.uriForEditing(originalUri) }
            .fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure((it as TotpException).toTotpUriException()) }
            )

    override fun sanitiseToSave(originalUri: String, editedUri: String): Result<String> =
        runCatching { totpUriSanitizer.uriForSaving(originalUri, editedUri) }
            .fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure((it as TotpException).toTotpUriException()) }
            )
}

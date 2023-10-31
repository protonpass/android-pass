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

import proton.android.pass.common.api.toOption
import proton.android.pass.commonrust.Totp
import proton.android.pass.commonrust.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits
import proton.android.pass.totp.api.TotpSpec

fun TotpSpec.toRustTotpSpec(): Totp {
    return Totp(
        label = label.value(),
        secret = secret,
        issuer = issuer.value(),
        algorithm = when (algorithm) {
            proton.android.pass.totp.api.TotpAlgorithm.Sha1 -> TotpAlgorithm.SHA1
            proton.android.pass.totp.api.TotpAlgorithm.Sha256 -> TotpAlgorithm.SHA256
            proton.android.pass.totp.api.TotpAlgorithm.Sha512 -> TotpAlgorithm.SHA512
        },
        digits = when (digits) {
            TotpDigits.Six -> digits.digits.toUByte()
            TotpDigits.Eight -> digits.digits.toUByte()
        },
        period = validPeriodSeconds.toUShort()
    )
}

fun Totp.toTotpSpec(): TotpSpec = TotpSpec(
    secret = secret,
    label = label.toOption(),
    issuer = issuer.toOption(),
    algorithm = when (algorithm) {
        TotpAlgorithm.SHA1 -> proton.android.pass.totp.api.TotpAlgorithm.Sha1
        TotpAlgorithm.SHA256 -> proton.android.pass.totp.api.TotpAlgorithm.Sha256
        TotpAlgorithm.SHA512 -> proton.android.pass.totp.api.TotpAlgorithm.Sha512
        null -> proton.android.pass.totp.api.TotpAlgorithm.Sha1
    },
    digits = when (digits?.toInt()) {
        TotpDigits.Six.digits -> TotpDigits.Six
        TotpDigits.Eight.digits -> TotpDigits.Eight
        else -> TotpDigits.Six
    },
    validPeriodSeconds = period?.toInt()
        ?: TotpSpec.DEFAULT_VALID_PERIOD_SECONDS
)

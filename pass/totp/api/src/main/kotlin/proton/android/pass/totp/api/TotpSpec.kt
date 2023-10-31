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

package proton.android.pass.totp.api

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option

data class TotpSpec(
    val secret: String,
    val label: Option<String> = None,
    val issuer: Option<String> = None,
    val algorithm: TotpAlgorithm = TotpAlgorithm.Sha1,
    val digits: TotpDigits = TotpDigits.Six,
    val validPeriodSeconds: Int = DEFAULT_VALID_PERIOD_SECONDS
) {
    fun isUsingDefaultParameters(): Boolean =
        algorithm == TotpAlgorithm.Sha1 &&
            digits == TotpDigits.Six &&
            validPeriodSeconds == DEFAULT_VALID_PERIOD_SECONDS

    companion object {
        const val DEFAULT_VALID_PERIOD_SECONDS = 30
    }
}

enum class TotpAlgorithm {
    Sha1,
    Sha256,
    Sha512
}

enum class TotpDigits(val digits: Int) {
    Six(6),
    Eight(8)
}

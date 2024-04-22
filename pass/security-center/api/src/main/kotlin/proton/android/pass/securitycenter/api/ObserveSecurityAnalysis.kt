/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.securitycenter.api

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.LoadingResult

data class ReusedPasswordsResult(
    val reusedPasswordsCount: Int
)

data class InsecurePasswordsResult(
    val insecurePasswordsCount: Int
)

data class Missing2faResult(
    val missing2faCount: Int
)

data class BreachDataResult(
    val exposedEmailCount: Int,
    val exposedPasswordCount: Int
)

data class SecurityAnalysis(
    val breachedData: LoadingResult<BreachDataResult>,
    val reusedPasswords: LoadingResult<ReusedPasswordsResult>,
    val insecurePasswords: LoadingResult<InsecurePasswordsResult>,
    val missing2fa: LoadingResult<Missing2faResult>
) {

    private val hasReusedPasswords: Boolean = when (reusedPasswords) {
        is LoadingResult.Error,
        LoadingResult.Loading -> false

        is LoadingResult.Success -> reusedPasswords.data.reusedPasswordsCount > 0
    }

    private val hasInsecurePasswords: Boolean = when (insecurePasswords) {
        is LoadingResult.Error,
        LoadingResult.Loading -> false

        is LoadingResult.Success -> insecurePasswords.data.insecurePasswordsCount > 0
    }

    private val hasMissing2fas: Boolean = when (missing2fa) {
        is LoadingResult.Error,
        LoadingResult.Loading -> false

        is LoadingResult.Success -> missing2fa.data.missing2faCount > 0
    }

    val hasSecurityIssues: Boolean = hasReusedPasswords || hasInsecurePasswords || hasMissing2fas

}

interface ObserveSecurityAnalysis {
    operator fun invoke(): Flow<SecurityAnalysis>
}

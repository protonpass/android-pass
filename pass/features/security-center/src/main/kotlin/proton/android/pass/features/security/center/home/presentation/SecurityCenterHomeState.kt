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

package proton.android.pass.features.security.center.home.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.securitycenter.api.InsecurePasswordsResult
import proton.android.pass.securitycenter.api.ReusedPasswordsResult

@Stable
internal data class SecurityCenterHomeState(
    private val insecurePasswordsLoadingResult: LoadingResult<InsecurePasswordsResult>,
    private val reusedPasswordsLoadingResult: LoadingResult<ReusedPasswordsResult>
) {

    internal val insecurePasswordsCount: Int? = when (insecurePasswordsLoadingResult) {
        is LoadingResult.Error,
        LoadingResult.Loading -> null

        is LoadingResult.Success -> insecurePasswordsLoadingResult.data.insecurePasswordsCount
    }

    internal val reusedPasswordsCount: Int? = when (reusedPasswordsLoadingResult) {
        is LoadingResult.Error,
        LoadingResult.Loading -> null

        is LoadingResult.Success -> reusedPasswordsLoadingResult.data.reusedPasswordsCount
    }

    internal companion object {

        val Initial: SecurityCenterHomeState = SecurityCenterHomeState(
            insecurePasswordsLoadingResult = LoadingResult.Loading,
            reusedPasswordsLoadingResult = LoadingResult.Loading
        )

    }

}

/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.account

import androidx.compose.runtime.Stable
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserRecovery
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

sealed interface PlanSection {

    fun name(): String = ""
    fun isLoading(): Boolean

    data object Hide : PlanSection {
        override fun isLoading(): Boolean = false
    }

    data object Loading : PlanSection {
        override fun isLoading(): Boolean = true
    }

    data class Data(val planName: String) : PlanSection {
        override fun name(): String = planName
        override fun isLoading(): Boolean = false
    }
}

@Stable
data class AccountUiState(
    val userId: UserId?,
    val email: String?,
    val recoveryEmail: String?,
    val recoveryState: UserRecovery.State?,
    val plan: PlanSection,
    val isLoadingState: IsLoadingState,
    val showChangePassword: Boolean,
    val showRecoveryEmail: Boolean,
    val showSecurityKeys: Boolean,
    val showUpgradeButton: Boolean,
    val showSubscriptionButton: Boolean,
    val isExtraPasswordEnabled: Boolean,
    val registeredSecurityKeys: List<Fido2RegisteredKey>?
) {
    companion object {
        val Initial = AccountUiState(
            userId = null,
            email = null,
            recoveryEmail = null,
            recoveryState = null,
            plan = PlanSection.Hide,
            isLoadingState = IsLoadingState.Loading,
            showChangePassword = false,
            showRecoveryEmail = false,
            showSecurityKeys = false,
            showUpgradeButton = false,
            showSubscriptionButton = false,
            isExtraPasswordEnabled = false,
            registeredSecurityKeys = emptyList()
        )
    }
}


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

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Composable
fun SubscriptionInfo(modifier: Modifier = Modifier, state: AccountUiState) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        if (state.plan != PlanSection.Hide) {
            SettingOption(
                text = state.plan.name(),
                label = stringResource(R.string.account_subscription_label),
                isLoading = state.plan.isLoading()
            )
        }
    }
}

@Preview
@Composable
fun SubscriptionInfoPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SubscriptionInfo(
                state = AccountUiState(
                    userId = null,
                    email = "myemail@proton.me",
                    recoveryEmail = "myrecoveryemail@proton.me",
                    recoveryState = null,
                    plan = PlanSection.Data("Free"),
                    isLoadingState = IsLoadingState.NotLoading,
                    showChangePassword = false,
                    showRecoveryEmail = false,
                    showSecurityKeys = false,
                    showUpgradeButton = true,
                    showSubscriptionButton = true,
                    showExtraPasswordButton = false,
                    isExtraPasswordEnabled = false,
                    registeredSecurityKeys = emptyList()
                )
            )
        }
    }
}

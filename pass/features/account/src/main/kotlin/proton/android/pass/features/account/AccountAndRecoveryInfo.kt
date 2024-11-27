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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserRecovery
import me.proton.core.util.kotlin.orEmpty
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Composable
internal fun AccountAndRecoveryInfo(
    modifier: Modifier = Modifier,
    state: AccountUiState,
    onEvent: (AccountContentEvent) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        Text(
            text = stringResource(R.string.account_recovery_title),
            style = ProtonTheme.typography.defaultSmallWeak
        )
        AccountAndRecoveryInfoContent(
            state = state,
            onEvent = onEvent
        )
    }
}

@Composable
internal fun AccountAndRecoveryInfoContent(
    modifier: Modifier = Modifier,
    state: AccountUiState,
    onEvent: (AccountContentEvent) -> Unit
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        SettingOption(
            text = state.email ?: "",
            label = stringResource(R.string.account_username_label),
            isLoading = state.isLoadingState.value()
        )

        PassDivider()

        AccountPasswordAndRecoveryInfo(
            state = state,
            onEvent = onEvent
        )
    }
}

@Composable
internal fun AccountPasswordAndRecoveryInfo(state: AccountUiState, onEvent: (AccountContentEvent) -> Unit) {
    val recoveryHint = state.recoveryEmail
        ?: stringResource(R.string.account_settings_list_item_recovery_hint_not_set)

    val passwordHint = when (state.recoveryState) {
        null -> null
        UserRecovery.State.None -> null
        UserRecovery.State.Cancelled -> null
        UserRecovery.State.Expired -> null
        UserRecovery.State.Grace -> stringResource(R.string.account_settings_list_item_password_hint_grace)
        UserRecovery.State.Insecure -> stringResource(R.string.account_settings_list_item_password_hint_insecure)
    }

    if (state.showChangePassword) {
        SettingOption(
            label = stringResource(R.string.account_settings_list_item_password_header),
            text = passwordHint.orEmpty(),
            onClick = { onEvent(AccountContentEvent.PasswordManagement) }
        )

        Divider(color = PassTheme.colors.inputBorderNorm)
    }

    if (state.showRecoveryEmail) {
        SettingOption(
            label = stringResource(R.string.account_settings_list_item_recovery_header),
            text = recoveryHint,
            onClick = { onEvent(AccountContentEvent.RecoveryEmail) }
        )
    }

    if (state.showSecurityKeys) {
        Divider(color = PassTheme.colors.inputBorderNorm)

        SettingOption(
            label = stringResource(R.string.account_settings_list_item_security_keys),
            text = securityKeysSettingsItemHint(state.registeredSecurityKeys),
            onClick = { onEvent(AccountContentEvent.SecurityKeys) }
        )
    }
}

@Composable
fun securityKeysSettingsItemHint(registeredSecurityKeys: List<Fido2RegisteredKey>?): String = when {
    registeredSecurityKeys.isNullOrEmpty() ->
        stringResource(R.string.account_settings_list_item_security_keys_hint_not_set)

    registeredSecurityKeys.size == 1 -> stringResource(
        R.string.account_settings_list_item_security_keys_hint_single,
        registeredSecurityKeys.first().name
    )

    else -> pluralStringResource(
        id = R.plurals.account_settings_list_item_security_keys_hint_many,
        count = registeredSecurityKeys.size,
        registeredSecurityKeys.size
    )
}

@Preview
@Composable
fun AccountInfoPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AccountAndRecoveryInfo(
                state = AccountUiState(
                    userId = UserId(""),
                    email = "myemail@proton.me",
                    recoveryEmail = "myrecoveryemail@proton.me",
                    recoveryState = UserRecovery.State.Grace,
                    plan = PlanSection.Data("Free"),
                    isLoadingState = IsLoadingState.NotLoading,
                    showChangePassword = true,
                    showRecoveryEmail = true,
                    showSecurityKeys = true,
                    showUpgradeButton = true,
                    showSubscriptionButton = true,
                    showExtraPasswordButton = false,
                    isExtraPasswordEnabled = false,
                    registeredSecurityKeys = emptyList()
                ),
                onEvent = {}
            )
        }
    }
}

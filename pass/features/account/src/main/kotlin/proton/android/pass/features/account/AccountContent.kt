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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.UpgradeButton
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar

@Composable
@Suppress("LongParameterList")
internal fun AccountContent(
    modifier: Modifier = Modifier,
    state: AccountUiState,
    onEvent: (AccountContentEvent) -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.account_title),
                actions = {
                    if (state.showUpgradeButton) {
                        UpgradeButton(
                            modifier = Modifier
                                .testTag(AccountContentTestTag.UPGRADE),
                            onUpgradeClick = { onEvent(AccountContentEvent.Upgrade) }
                        )
                    }
                },
                onUpClick = { onEvent(AccountContentEvent.Back) }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(PassTheme.colors.backgroundStrong)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
        ) {
            SubscriptionInfo(state = state)
            AccountAndRecoveryInfo(
                state = state,
                onEvent = onEvent
            )
            if (state.showSubscriptionButton) {
                ManageSubscription(
                    modifier = Modifier.testTag(AccountContentTestTag.SUBSCRIPTION),
                    onSubscriptionClick = { onEvent(AccountContentEvent.Subscription) }
                )
            }
            ManageAccount(onManageAccountClick = { onEvent(AccountContentEvent.ManageAccount) })

            if (state.userId != null && state.showExtraPasswordButton) {
                ExtraPassword(
                    userId = state.userId,
                    isExtraPasswordEnabled = state.isExtraPasswordEnabled,
                    onEvent = onEvent
                )
            }
            SignOut(
                onSignOutClick = {
                    val userId = state.userId ?: return@SignOut
                    onEvent(AccountContentEvent.SignOut(userId))
                }
            )
            DeleteAccount(onDeleteAccountClick = { onEvent(AccountContentEvent.DeleteAccount) })
            Text(
                text = stringResource(R.string.account_permanently_delete_warning),
                style = ProtonTheme.typography.captionWeak
            )
        }
    }
}

object AccountContentTestTag {
    const val UPGRADE = "upgrade"
    const val SUBSCRIPTION = "subscription"
}

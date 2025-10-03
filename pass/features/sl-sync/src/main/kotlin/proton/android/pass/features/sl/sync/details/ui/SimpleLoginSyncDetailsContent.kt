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

package proton.android.pass.features.sl.sync.details.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.composecomponents.impl.loading.PassFullScreenLoading
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.details.presentation.SimpleLoginSyncDetailsState
import proton.android.pass.features.sl.sync.details.ui.dialogs.SimpleLoginSyncDetailsOptionType
import proton.android.pass.features.sl.sync.details.ui.dialogs.SimpleLoginSyncDetailsOptionsDialog

@Composable
internal fun SimpleLoginSyncDetailsContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SimpleLoginSyncDetailsUiEvent) -> Unit,
    state: SimpleLoginSyncDetailsState,
    dialogOptionTypeOption: Option<SimpleLoginSyncDetailsOptionType>
) = with(state) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            PassExtendedTopBar(
                modifier = modifier,
                backButton = PassTopBarBackButtonType.BackArrow,
                title = stringResource(id = R.string.simple_login_sync_management_title),
                onUpClick = { onUiEvent(SimpleLoginSyncDetailsUiEvent.OnBackClicked) }
            )
        }
    ) { innerPaddingValue ->
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PassFullScreenLoading()
        }

        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SimpleLoginSyncDetailsSections(
                modifier = Modifier.padding(paddingValues = innerPaddingValue),
                defaultDomain = defaultDomain,
                defaultMailboxEmail = defaultMailboxEmail,
                defaultVault = defaultVault,
                isSyncEnabled = isSyncEnabled,
                hasPendingAliases = hasPendingAliases,
                pendingAliasesCount = pendingAliasesCount,
                canSelectDomain = canSelectDomain,
                canSelectMailbox = canSelectMailbox,
                onUiEvent = onUiEvent
            )
        }
    }

    if (dialogOptionTypeOption is Some) {
        when (dialogOptionTypeOption.value) {
            SimpleLoginSyncDetailsOptionType.Domain -> {
                SimpleLoginSyncDetailsOptionsDialog(
                    titleResId = R.string.simple_login_sync_management_dialog_title_domains,
                    selectedOption = selectedAliasDomain,
                    options = aliasDomainOptions,
                    isLoading = isUpdating,
                    onSelectOption = { selectedOptionIndex ->
                        SimpleLoginSyncDetailsUiEvent.OnDomainSelected(
                            aliasDomain = getAliasDomain(selectedOptionIndex)
                        ).also(onUiEvent)
                    },
                    onDismiss = { onUiEvent(SimpleLoginSyncDetailsUiEvent.OnOptionsDialogDismissed) },
                    onUpdate = { onUiEvent(SimpleLoginSyncDetailsUiEvent.OnUpdateDomainClicked) },
                    canUpdate = canUpdateDomain
                )
            }

            SimpleLoginSyncDetailsOptionType.Mailbox -> {
                SimpleLoginSyncDetailsOptionsDialog(
                    titleResId = R.string.simple_login_sync_management_dialog_title_mailboxes,
                    selectedOption = selectedAliasMailboxEmail,
                    options = aliasMailboxOptions,
                    isLoading = isUpdating,
                    onSelectOption = { selectedOptionIndex ->
                        SimpleLoginSyncDetailsUiEvent.OnMailboxSelected(
                            aliasMailbox = getAliasMailbox(selectedOptionIndex)
                        ).also(onUiEvent)
                    },
                    onDismiss = { onUiEvent(SimpleLoginSyncDetailsUiEvent.OnOptionsDialogDismissed) },
                    onUpdate = { onUiEvent(SimpleLoginSyncDetailsUiEvent.OnUpdateMailboxClicked) },
                    canUpdate = canUpdateMailbox
                )
            }
        }
    }
}

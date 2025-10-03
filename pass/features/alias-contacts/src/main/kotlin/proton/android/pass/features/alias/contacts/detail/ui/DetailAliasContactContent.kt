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

package proton.android.pass.features.alias.contacts.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.buttons.UpgradeButton
import proton.android.pass.composecomponents.impl.loading.PassFullScreenLoading
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.IconTopAppBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactUIEvent
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactUIState
import proton.android.pass.features.aliascontacts.R

@Composable
fun DetailAliasContactContent(
    modifier: Modifier = Modifier,
    state: DetailAliasContactUIState,
    onEvent: (DetailAliasContactUIEvent) -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            IconTopAppBar(
                actions = {
                    if (!state.canManageContacts) {
                        UpgradeButton(
                            backgroundColor = PassTheme.colors.aliasInteractionNormMajor1,
                            contentColor = PassTheme.colors.textInvert,
                            onUpgradeClick = { onEvent(DetailAliasContactUIEvent.Upgrade) }
                        )
                    } else {
                        LoadingCircleButton(
                            color = PassTheme.colors.aliasInteractionNormMajor1,
                            isLoading = false,
                            text = {
                                Text.Body2Regular(
                                    text = stringResource(R.string.create_contact_button),
                                    color = PassTheme.colors.textInvert
                                )
                            },
                            onClick = { onEvent(DetailAliasContactUIEvent.CreateContact) }
                        )
                    }
                },
                navigationIcon = {
                    BackArrowCircleIconButton(
                        modifier = Modifier.padding(
                            horizontal = Spacing.medium - Spacing.extraSmall,
                            vertical = Spacing.extraSmall
                        ),
                        color = PassTheme.colors.aliasInteractionNormMajor2,
                        backgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                        onUpClick = { onEvent(DetailAliasContactUIEvent.Back) }
                    )
                }
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text.Hero(stringResource(R.string.detail_contact_title))
                QuestionMarkRoundedIcon(onEvent = onEvent)
            }
            if (state.displayName.isNotBlank()) {
                Text.Body1Regular(stringResource(R.string.detail_contact_display_name, state.displayName))
            }
            if (!state.aliasContactsListUIState.isLoading) {
                if (state.aliasContactsListUIState.hasContacts) {
                    ContactList(
                        blockedContacts = state.aliasContactsListUIState.blockedContacts,
                        forwardingContacts = state.aliasContactsListUIState.forwardingContacts,
                        contactBlockIsLoading = state.contactBlockIsLoading,
                        onEvent = onEvent
                    )
                } else {
                    EmptyContacts(onEvent = onEvent)
                }
            } else {
                PassFullScreenLoading()
            }
        }
    }
}

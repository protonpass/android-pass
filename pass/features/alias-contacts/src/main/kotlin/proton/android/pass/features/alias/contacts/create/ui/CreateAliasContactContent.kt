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

package proton.android.pass.features.alias.contacts.create.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.IconTopAppBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.CrossBackCircleIconButton
import proton.android.pass.features.alias.contacts.create.presentation.CreateAliasContactUIEvent
import proton.android.pass.features.alias.contacts.create.presentation.CreateAliasContactUIState
import proton.android.pass.features.aliascontacts.R

@Composable
fun CreateAliasContactContent(
    modifier: Modifier,
    email: String,
    state: CreateAliasContactUIState,
    onEvent: (CreateAliasContactUIEvent) -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            IconTopAppBar(
                actions = {
                    LoadingCircleButton(
                        color = PassTheme.colors.aliasInteractionNormMajor1,
                        isLoading = state.isLoading,
                        text = {
                            Text.Body2Regular(
                                text = stringResource(R.string.save_contact_alias),
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = { onEvent(CreateAliasContactUIEvent.Create) }
                    )
                },
                navigationIcon = {
                    CrossBackCircleIconButton(
                        modifier = Modifier.padding(
                            horizontal = Spacing.medium - Spacing.extraSmall,
                            vertical = Spacing.extraSmall
                        ),
                        color = PassTheme.colors.aliasInteractionNormMajor2,
                        backgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                        onUpClick = { onEvent(CreateAliasContactUIEvent.Back) }
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
            Text.Hero(stringResource(R.string.create_contact_title))
            EmailInput(
                value = email,
                enabled = !state.isLoading,
                isError = state.isEmailInvalid,
                onChange = { onEvent(CreateAliasContactUIEvent.EmailChanged(it)) }
            )
        }
    }
}

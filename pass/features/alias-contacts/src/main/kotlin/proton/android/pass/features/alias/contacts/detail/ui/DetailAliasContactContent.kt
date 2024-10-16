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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallInverted
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.topbar.IconTopAppBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactUIEvent
import proton.android.pass.features.aliascontacts.R

@Composable
fun DetailAliasContactContent(modifier: Modifier = Modifier, onEvent: (DetailAliasContactUIEvent) -> Unit) {
    Scaffold(
        modifier = modifier,
        topBar = {
            IconTopAppBar(
                actions = {
                    LoadingCircleButton(
                        color = PassTheme.colors.aliasInteractionNormMajor1,
                        isLoading = false,
                        text = {
                            Text(
                                text = stringResource(R.string.create_contact_button),
                                style = ProtonTheme.typography.defaultSmallInverted,
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = { onEvent(DetailAliasContactUIEvent.CreateContact) }
                    )
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

        }
    }
}

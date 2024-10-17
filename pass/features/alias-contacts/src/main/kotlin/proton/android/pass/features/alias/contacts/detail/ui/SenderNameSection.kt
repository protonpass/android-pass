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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactUIEvent
import proton.android.pass.features.alias.contacts.detail.presentation.SenderNameMode
import proton.android.pass.features.alias.contacts.detail.presentation.SenderNameMode.Edit
import proton.android.pass.features.alias.contacts.detail.presentation.SenderNameMode.Idle
import proton.android.pass.features.alias.contacts.detail.presentation.SenderNameMode.Loading
import proton.android.pass.features.alias.contacts.detail.presentation.SenderNameUIState
import proton.android.pass.features.aliascontacts.R
import me.proton.core.presentation.R as CoreR

@Composable
fun SenderNameSection(
    modifier: Modifier = Modifier,
    name: String,
    state: SenderNameUIState,
    onEvent: (DetailAliasContactUIEvent) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(state.nameMode) {
        when (state.nameMode) {
            Edit -> focusRequester.requestFocus()
            Idle,
            Loading -> Unit
        }
    }
    ProtonTextField(
        modifier = modifier
            .roundedContainer(
                backgroundColor = Color.Transparent,
                borderColor = ProtonTheme.colors.separatorNorm
            )
            .fillMaxWidth()
            .padding(
                start = Spacing.medium,
                top = Spacing.medium,
                end = Spacing.small,
                bottom = Spacing.medium
            )
            .focusRequester(focusRequester),
        value = name,
        onChange = { onEvent(DetailAliasContactUIEvent.OnSenderNameChanged(it)) },
        moveToNextOnEnter = false,
        onDoneClick = { onEvent(DetailAliasContactUIEvent.UpdateSenderName) },
        textStyle = ProtonTheme.typography.defaultNorm(state.nameMode == Edit),
        editable = state.nameMode == Edit,
        label = {
            ProtonTextFieldLabel(
                text = stringResource(id = R.string.detail_contact_sender_name)
            )
        },
        trailingIcon = {
            when (state.nameMode) {
                Idle -> IconButton(onClick = { onEvent(DetailAliasContactUIEvent.EditSenderName) }) {
                    Icon.Default(CoreR.drawable.ic_proton_pencil, tint = PassTheme.colors.textWeak)
                }

                Edit -> IconButton(onClick = { onEvent(DetailAliasContactUIEvent.UpdateSenderName) }) {
                    Icon.Default(
                        CoreR.drawable.ic_proton_checkmark,
                        tint = PassTheme.colors.signalSuccess
                    )
                }

                Loading -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PassTheme.colors.textWeak
                )
            }
        }
    )
}

class NameModePreviewProvider : PreviewParameterProvider<SenderNameMode> {
    override val values: Sequence<SenderNameMode>
        get() = SenderNameMode.entries.asIterable().asSequence()
}

class ThemedNameModePreviewProvider :
    ThemePairPreviewProvider<SenderNameMode>(NameModePreviewProvider())

@Preview
@Composable
fun SenderNameSectionPreview(
    @PreviewParameter(ThemedNameModePreviewProvider::class) input: Pair<Boolean, SenderNameMode>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SenderNameSection(
                name = "John Doe",
                state = SenderNameUIState(input.second),
                onEvent = {}
            )
        }
    }
}

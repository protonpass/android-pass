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

package proton.android.pass.features.item.options.aliases.trash.dialogs.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.features.item.options.R
import proton.android.pass.features.item.options.aliases.trash.dialogs.presentation.ItemOptionsAliasTrashDialogEvent
import proton.android.pass.features.item.options.aliases.trash.dialogs.presentation.ItemOptionsAliasTrashDialogState
import proton.android.pass.preferences.AliasTrashDialogStatusPreference
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ItemOptionsAliasTrashDialogContent(
    modifier: Modifier = Modifier,
    state: ItemOptionsAliasTrashDialogState,
    onUiEvent: (ItemOptionsAliasTrashDialogUiEvent) -> Unit
) = with(state) {
    NoPaddingDialog(
        modifier = modifier.padding(horizontal = Spacing.medium),
        backgroundColor = PassTheme.colors.backgroundStrong,
        onDismissRequest = {
            if (!isLoading) {
                onUiEvent(ItemOptionsAliasTrashDialogUiEvent.OnDismiss)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
        ) {
            ProtonDialogTitle(
                modifier = Modifier.padding(
                    top = Spacing.large,
                    bottom = Spacing.medium
                ),
                title = stringResource(id = R.string.item_options_alias_trash_dialog_title)
            )

            Text(
                text = stringResource(id = R.string.item_options_alias_trash_dialog_message),
                style = ProtonTheme.typography.body1Regular
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        ItemOptionsAliasTrashDialogUiEvent.OnRemindMeChange(!isRemindMeEnabled)
                            .also(onUiEvent)
                    }
                    .offset(x = -Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    colors = CheckboxDefaults.colors(
                        checkedColor = PassTheme.colors.interactionNormMajor2
                    ),
                    checked = isRemindMeEnabled,
                    onCheckedChange = { isChecked ->
                        onUiEvent(ItemOptionsAliasTrashDialogUiEvent.OnRemindMeChange(isChecked))
                    }
                )

                Text(
                    text = stringResource(id = R.string.item_options_alias_trash_dialog_checkbox_text),
                    style = ProtonTheme.typography.body2Regular,
                    color = PassTheme.colors.textNorm
                )
            }

            Column(
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .padding(bottom = Spacing.medium)
            ) {
                AliasDisableOrTrashDialogButton(
                    textResId = R.string.item_options_alias_trash_dialog_button_text_disable,
                    onClick = {
                        onUiEvent(ItemOptionsAliasTrashDialogUiEvent.OnDisable)
                    }
                )

                AliasDisableOrTrashDialogButton(
                    textResId = R.string.item_options_alias_trash_dialog_button_text_trash,
                    onClick = {
                        onUiEvent(ItemOptionsAliasTrashDialogUiEvent.OnTrash)
                    }
                )

                AliasDisableOrTrashDialogButton(
                    textResId = CompR.string.action_cancel,
                    onClick = {
                        onUiEvent(ItemOptionsAliasTrashDialogUiEvent.OnDismiss)
                    }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.AliasDisableOrTrashDialogButton(
    modifier: Modifier = Modifier,
    @StringRes textResId: Int,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier.align(alignment = Alignment.End),
        onClick = onClick
    ) {
        Text(
            text = stringResource(id = textResId),
            style = ProtonTheme.typography.body2Regular,
            color = PassTheme.colors.interactionNormMajor2
        )
    }
}

@[Preview Composable]
internal fun AliasDisableOrTrashDialogPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isRemindMeEnabled) = input

    PassTheme(isDark = isDark) {
        Surface {
            ItemOptionsAliasTrashDialogState(
                isLoadingState = IsLoadingState.NotLoading,
                event = ItemOptionsAliasTrashDialogEvent.Idle,
                preference = AliasTrashDialogStatusPreference.from(isRemindMeEnabled)
            )
        }
    }
}

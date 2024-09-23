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

package proton.android.pass.features.sl.sync.management.ui.dialogs

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.features.sl.sync.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncDetailsOptionsDialog(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int,
    selectedOption: String?,
    options: ImmutableList<String?>,
    onSelectOption: (Int) -> Unit,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    isLoading: Boolean,
    canUpdate: Boolean
) {
    NoPaddingDialog(
        modifier = modifier.padding(horizontal = Spacing.medium),
        backgroundColor = PassTheme.colors.backgroundStrong,
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ProtonDialogTitle(
                modifier = Modifier.padding(
                    start = Spacing.large,
                    top = Spacing.large,
                    bottom = Spacing.medium
                ),
                title = stringResource(id = titleResId)
            )

            LazyColumn(
                modifier = Modifier.weight(weight = 1f, fill = false)
            ) {
                items(options.size) { index ->
                    val currentOption = remember { options[index] }

                    SimpleLoginSyncDetailsOptionsDialogRow(
                        text = currentOption ?: stringResource(
                            id = R.string.simple_login_sync_management_domain_option_blank
                        ),
                        isSelected = currentOption == selectedOption,
                        onSelected = { onSelectOption(index) }
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(alignment = Alignment.End)
                        .padding(
                            end = Spacing.medium,
                            bottom = Spacing.medium
                        )
                )
            } else {
                Row(
                    modifier = Modifier
                        .align(alignment = Alignment.End)
                        .padding(
                            end = Spacing.medium,
                            bottom = Spacing.medium
                        )
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = stringResource(id = CompR.string.action_close),
                            style = ProtonTheme.typography.body2Regular,
                            color = PassTheme.colors.interactionNormMajor2
                        )
                    }

                    AnimatedVisibility(visible = canUpdate) {
                        TextButton(
                            onClick = {
                                if (canUpdate) {
                                    onUpdate()
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(id = CompR.string.action_update),
                                style = ProtonTheme.typography.body2Regular,
                                color = PassTheme.colors.interactionNormMajor2
                            )
                        }
                    }
                }
            }
        }
    }
}

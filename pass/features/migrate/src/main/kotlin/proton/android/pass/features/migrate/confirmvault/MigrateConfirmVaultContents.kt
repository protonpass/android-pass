/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.migrate.confirmvault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.container.PassInfoWarningBanner
import proton.android.pass.composecomponents.impl.text.Text as PassText
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.migrate.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun MigrateConfirmVaultContents(
    modifier: Modifier = Modifier,
    state: MigrateConfirmVaultUiState,
    onVaultSelected: (ShareId) -> Unit,
    onFolderSelected: (ShareId, FolderId) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    val hasSelection = state.selectedShareId is Some
    val folderSelected = state.selectedFolderId !is None

    val title = when (state.mode) {
        is MigrateMode.MigrateSelectedItems -> when {
            hasSelection && folderSelected -> pluralStringResource(
                R.plurals.migrate_item_to_folder_confirm_title_bottom_sheet,
                state.mode.number,
                state.mode.number
            )
            hasSelection -> pluralStringResource(
                R.plurals.migrate_item_confirm_title_bottom_sheet,
                state.mode.number,
                state.mode.number
            )
            else -> stringResource(R.string.migrate_select_vault_title)
        }

        MigrateMode.MigrateAll ->
            stringResource(R.string.migrate_select_destination_for_source_title, state.sourceName)

        MigrateMode.MoveFolder -> if (folderSelected) {
            stringResource(R.string.migrate_folder_to_folder_confirm_title_bottom_sheet)
        } else {
            stringResource(R.string.migrate_folder_confirm_title_bottom_sheet)
        }

        MigrateMode.MoveAllItemsInFolder ->
            stringResource(R.string.migrate_select_destination_for_source_title, state.sourceName)
    }

    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            ProtonTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {},
                navigationIcon = {
                    Circle(
                        modifier = Modifier.padding(Spacing.mediumSmall, Spacing.extraSmall),
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onClick = onClose
                    ) {
                        Icon(
                            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross),
                            contentDescription = stringResource(CompR.string.action_close),
                            tint = PassTheme.colors.interactionNormMajor2
                        )
                    }
                },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(
                            horizontal = Spacing.mediumSmall,
                            vertical = Spacing.extraSmall
                        ),
                        color = if (hasSelection) {
                            PassTheme.colors.interactionNormMajor1
                        } else {
                            PassTheme.colors.interactionNormMinor2
                        },
                        isLoading = state.isLoading.value(),
                        buttonEnabled = hasSelection,
                        text = {
                            Text(
                                text = stringResource(R.string.migrate_item_confirm_confirm_button),
                                color = PassTheme.colors.textInvert,
                                style = PassTheme.typography.body3Norm()
                            )
                        },
                        onClick = onConfirm
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            PassText.Body1Regular(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.medium),
                text = title,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                AnimatedVisibility(visible = state.showHistoryWarning) {
                    PassInfoWarningBanner(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(id = R.string.migrate_item_warning_history),
                        backgroundColor = PassTheme.colors.interactionNormMinor1
                    )
                }

                AnimatedVisibility(visible = state.showSecureLinkWarning) {
                    PassInfoWarningBanner(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(id = R.string.migrate_item_warning_secure_link),
                        backgroundColor = PassTheme.colors.interactionNormMinor1
                    )
                }
            }

            if (state.isLoadingVaults) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                MigrateVaultSelectorContents(
                    modifier = Modifier.weight(1f),
                    vaults = state.vaultList,
                    folderIdToExpand = state.folderIdToExpand,
                    selectedShareId = state.selectedShareId,
                    selectedFolderId = state.selectedFolderId,
                    disabledFolderId = state.disabledFolderId,
                    disabledFolderItemCount = state.disabledFolderItemCount,
                    disabledFolderReasonOverride = if (state.mode is MigrateMode.MoveFolder) {
                        stringResource(id = R.string.migrate_disabled_folder_reason_same_location)
                    } else {
                        null
                    },
                    disabledDescendantFolderIds = state.disabledDescendantFolderIds,
                    disabledDescendantFolderReason = if (state.mode is MigrateMode.MoveFolder) {
                        stringResource(id = R.string.migrate_disabled_folder_reason_descendant)
                    } else {
                        null
                    },
                    movingFolderId = state.movingFolderId,
                    movingFolderReason = stringResource(id = R.string.migrate_disabled_folder_reason_being_moved),
                    startWithVaultExpanded = state.mode is MigrateMode.MoveFolder,
                    onVaultSelected = onVaultSelected,
                    onFolderSelected = onFolderSelected
                )
            }
        }
    }
}

@[Preview Composable]
internal fun MigrateConfirmVaultContentsPreview(
    @PreviewParameter(ThemeMigrateConfirmVaultContentsPreviewProvider::class)
    input: Pair<Boolean, MigrateConfirmVaultUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            MigrateConfirmVaultContents(
                state = input.second,
                onVaultSelected = {},
                onFolderSelected = { _, _ -> },
                onClose = {},
                onConfirm = {}
            )
        }
    }
}

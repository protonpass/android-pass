/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.featurevault.impl.R
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun SelectVaultBottomsheetContent(
    modifier: Modifier = Modifier,
    state: SelectVaultUiState.Success,
    onVaultClick: (ShareId) -> Unit,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = modifier.bottomSheet(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.showUpgradeMessage) {
            InfoBanner(
                modifier = Modifier.padding(16.dp, 0.dp),
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                text = buildAnnotatedString {
                    append(stringResource(R.string.bottomsheet_cannot_select_not_primary_vault))
                    append(' ')
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = PassTheme.colors.loginInteractionNormMajor2
                        )
                    ) {
                        append(stringResource(CompR.string.action_upgrade_now))
                    }
                },
                onClick = onUpgrade
            )
        } else {
            BottomSheetTitle(title = stringResource(R.string.vault_title))
        }

        BottomSheetItemList(
            items = state.vaults
                .map {
                    val isSelected = it.vault.vault.shareId == state.selected.vault.shareId
                    val (subtitle, enabled) = when (it.status) {
                        is VaultStatus.Disabled -> when (it.status.reason) {
                            VaultStatus.Reason.ReadOnly ->
                                stringResource(R.string.bottomsheet_select_vault_read_only) to false
                            VaultStatus.Reason.Downgraded -> {
                                stringResource(R.string.bottomsheet_select_vault_only_oldest_vaults) to false
                            }
                        }

                        VaultStatus.Selectable -> null to true
                    }
                    BottomSheetVaultRow(
                        vault = it.vault,
                        isSelected = isSelected,
                        enabled = enabled,
                        customSubtitle = subtitle,
                        onVaultClick = onVaultClick
                    )
                }
                .withDividers()
                .toPersistentList()
        )
    }
}

@Preview
@Composable
fun SelectVaultBottomsheetContentPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val selectedVault = VaultWithItemCount(
        vault = Vault(
            shareId = ShareId("123"),
            name = "vault 1",
        ),
        activeItemCount = 12,
        trashedItemCount = 0,
    )
    PassTheme(isDark = input.first) {
        Surface {
            SelectVaultBottomsheetContent(
                state = SelectVaultUiState.Success(
                    vaults = persistentListOf(
                        VaultWithStatus(
                            vault = selectedVault,
                            status = VaultStatus.Selectable
                        ),
                        VaultWithStatus(
                            vault = VaultWithItemCount(
                                vault = Vault(
                                    shareId = ShareId("other"),
                                    name = "vault 2",
                                    color = ShareColor.Color2,
                                    icon = ShareIcon.Icon2,
                                ),
                                activeItemCount = 1,
                                trashedItemCount = 0,
                            ),
                            status = VaultStatus.Disabled(VaultStatus.Reason.ReadOnly)
                        ),
                        VaultWithStatus(
                            vault = VaultWithItemCount(
                                vault = Vault(
                                    shareId = ShareId("another"),
                                    name = "vault 3",
                                    color = ShareColor.Color3,
                                    icon = ShareIcon.Icon3,
                                ),
                                activeItemCount = 1,
                                trashedItemCount = 0,
                            ),
                            status = VaultStatus.Disabled(VaultStatus.Reason.Downgraded)
                        )
                    ),
                    selected = selectedVault,
                    showUpgradeMessage = input.second,
                ),
                onVaultClick = {},
                onUpgrade = {}
            )
        }
    }
}


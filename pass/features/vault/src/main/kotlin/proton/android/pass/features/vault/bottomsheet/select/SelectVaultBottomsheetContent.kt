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

package proton.android.pass.features.vault.bottomsheet.select

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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.vault.R
import java.util.Date
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
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        if (state.showUpgradeMessage) {
            InfoBanner(
                modifier = Modifier.padding(Spacing.medium, Spacing.none),
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
            userId = UserId(id = ""),
            shareId = ShareId("123"),
            vaultId = VaultId("123"),
            name = "vault 1",
            createTime = Date(),
            shareFlags = ShareFlags(0)
        ),
        activeItemCount = 12,
        trashedItemCount = 0
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
                                    userId = UserId(id = ""),
                                    shareId = ShareId("other"),
                                    vaultId = VaultId("123"),
                                    name = "vault 2",
                                    color = ShareColor.Color2,
                                    icon = ShareIcon.Icon2,
                                    createTime = Date(),
                                    shareFlags = ShareFlags(0)
                                ),
                                activeItemCount = 1,
                                trashedItemCount = 0
                            ),
                            status = VaultStatus.Disabled(VaultStatus.Reason.ReadOnly)
                        ),
                        VaultWithStatus(
                            vault = VaultWithItemCount(
                                vault = Vault(
                                    userId = UserId(id = ""),
                                    shareId = ShareId("another"),
                                    vaultId = VaultId("123"),
                                    name = "vault 3",
                                    color = ShareColor.Color3,
                                    icon = ShareIcon.Icon3,
                                    createTime = Date(),
                                    shareFlags = ShareFlags(0)
                                ),
                                activeItemCount = 1,
                                trashedItemCount = 0
                            ),
                            status = VaultStatus.Disabled(VaultStatus.Reason.Downgraded)
                        )
                    ),
                    selected = selectedVault,
                    showUpgradeMessage = input.second
                ),
                onVaultClick = {},
                onUpgrade = {}
            )
        }
    }
}


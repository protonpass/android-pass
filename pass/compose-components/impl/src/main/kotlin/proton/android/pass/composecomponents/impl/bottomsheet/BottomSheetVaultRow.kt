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

package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount

fun BottomSheetVaultRow(
    vault: VaultWithItemCount,
    isSelected: Boolean,
    customSubtitle: String? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onVaultClick: ((ShareId) -> Unit)?
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                val color = if (enabled) {
                    PassTheme.colors.textNorm
                } else {
                    PassTheme.colors.textHint
                }

                BottomSheetItemTitle(
                    text = vault.vault.name,
                    color = color
                )
            }
        override val subtitle: @Composable (() -> Unit)
            get() = {
                val color = if (enabled) {
                    PassTheme.colors.textWeak
                } else {
                    PassTheme.colors.textDisabled
                }
                val text = customSubtitle ?: pluralStringResource(
                    id = R.plurals.bottomsheet_select_vault_item_count,
                    count = vault.activeItemCount.toInt(),
                    vault.activeItemCount
                )
                Text(
                    text = text,
                    color = color
                )
            }
        override val leftIcon: @Composable (() -> Unit)
            get() = {
                VaultIcon(
                    backgroundColor = vault.vault.color.toColor(true),
                    iconColor = vault.vault.color.toColor(),
                    icon = vault.vault.icon.toResource()
                )
            }
        override val endIcon: (@Composable () -> Unit)?
            get() = if (isSelected) {
                {
                    BottomSheetItemIcon(
                        iconId = me.proton.core.presentation.R.drawable.ic_proton_checkmark,
                        tint = PassTheme.colors.loginInteractionNormMajor1
                    )
                }
            } else if (isLoading) {
                {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            } else null
        override val onClick: (() -> Unit)? = if (onVaultClick != null && enabled) {
            { onVaultClick(vault.vault.shareId) }
        } else null
        override val isDivider = false
    }

class ThemeBottomSheetVaultRowProvider :
    ThemePairPreviewProvider<VaultRowInput>(BottomSheetVaultRowPreviewProvider())

@Preview
@Composable
fun BottomSheetVaultRowPreview(
    @PreviewParameter(ThemeBottomSheetVaultRowProvider::class) input: Pair<Boolean, VaultRowInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            BottomSheetItemList(
                items = listOf(
                    BottomSheetVaultRow(
                        vault = input.second.vault,
                        isSelected = input.second.isSelected,
                        enabled = input.second.enabled,
                        isLoading = input.second.isLoading,
                        onVaultClick = {}
                    )
                ).toImmutableList()
            )
        }
    }
}

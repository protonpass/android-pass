/*
 * Copyright (c) 2023-2025 Proton AG
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

package proton.android.pass.features.itemcreate.alias.suffixes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.domain.AliasSuffix
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.banner.AliasCustomDomainBanner
import proton.android.pass.features.itemcreate.alias.suffixes.presentation.SelectSuffixEvent
import proton.android.pass.features.itemcreate.alias.suffixes.presentation.SelectSuffixUiState
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SelectSuffixContent(
    modifier: Modifier = Modifier,
    state: SelectSuffixUiState,
    onEvent: (SelectSuffixUiEvent) -> Unit
) {
    Column(
        modifier = modifier.bottomSheet()
    ) {
        BottomSheetTitle(
            title = stringResource(id = R.string.alias_bottomsheet_suffix_title)
        )

        val list = state.suffixList.map { suffix ->
            val isSelected = suffix == state.selectedSuffix.value()
            object : BottomSheetItem {
                override val title: @Composable () -> Unit
                    get() = {
                        BottomSheetItemTitle(
                            text = suffix.domain
                        )
                    }
                override val subtitle: @Composable (() -> Unit)? = null
                override val leftIcon: @Composable (() -> Unit)? = null
                override val endIcon: @Composable (() -> Unit)? = if (isSelected) {
                    {
                        BottomSheetItemIcon(
                            iconId = CoreR.drawable.ic_proton_checkmark,
                            tint = PassTheme.colors.interactionNorm
                        )
                    }
                } else null
                override val onClick: () -> Unit =
                    { onEvent(SelectSuffixUiEvent.SelectSuffixUi(suffix)) }
                override val isDivider: Boolean = false
            }
        }
        BottomSheetItemList(
            items = list.withDividers().toPersistentList()
        )
        AnimatedVisibility(state.shouldDisplayFeatureDiscoveryBanner) {
            AliasCustomDomainBanner(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                onClick = {
                    onEvent(SelectSuffixUiEvent.AddCustomDomain)
                },
                onClose = {
                    onEvent(SelectSuffixUiEvent.DismissFeatureDiscoveryBanner)
                }
            )
        }
    }
}

@Preview
@Composable
fun SelectSuffixContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    val suffixList = persistentSetOf(
        AliasSuffix(
            suffix = "first",
            signedSuffix = "",
            isCustom = false,
            domain = "proton.me"
        ),
        AliasSuffix(
            suffix = "second",
            signedSuffix = "",
            isCustom = false,
            domain = "proton.me"
        )
    )
    PassTheme(isDark = isDark) {
        Surface {
            SelectSuffixContent(
                state = SelectSuffixUiState(
                    suffixList = suffixList,
                    selectedSuffix = suffixList.firstOrNull().toOption(),
                    shouldDisplayFeatureDiscoveryBanner = false,
                    event = SelectSuffixEvent.Idle
                ),
                onEvent = {}
            )
        }
    }
}

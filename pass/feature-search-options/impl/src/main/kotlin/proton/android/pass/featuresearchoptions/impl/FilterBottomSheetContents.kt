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

package proton.android.pass.featuresearchoptions.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.featuresearchoptions.api.SearchFilterType
import proton.android.pass.featuresearchoptions.api.SearchFilterType.Alias
import proton.android.pass.featuresearchoptions.api.SearchFilterType.All
import proton.android.pass.featuresearchoptions.api.SearchFilterType.CreditCard
import proton.android.pass.featuresearchoptions.api.SearchFilterType.Identity
import proton.android.pass.featuresearchoptions.api.SearchFilterType.Login
import proton.android.pass.featuresearchoptions.api.SearchFilterType.LoginMFA
import proton.android.pass.featuresearchoptions.api.SearchFilterType.Note
import me.proton.core.presentation.R as CoreR

@ExperimentalMaterialApi
@Composable
fun FilterBottomSheetContents(
    modifier: Modifier = Modifier,
    state: FilterOptionsUIState,
    onSortingTypeSelected: (SearchFilterType) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = SearchFilterType.entries.toList()
            .mapToBottomSheetItem(state, onSortingTypeSelected)
            .withDividers()
            .toPersistentList()
    )
}

@Suppress("LongMethod", "ComplexMethod")
private fun List<SearchFilterType>.mapToBottomSheetItem(
    state: FilterOptionsUIState,
    onSortingTypeSelected: (SearchFilterType) -> Unit
): ImmutableList<BottomSheetItem> = map { item ->
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                val successState = state as? SuccessFilterOptionsUIState
                val color = if (item == successState?.filterType) {
                    PassTheme.colors.interactionNorm
                } else {
                    PassTheme.colors.textNorm
                }
                val title = when (item) {
                    All -> stringResource(id = R.string.item_type_filter_all) +
                        successState?.summary?.total?.let { " ($it)" }

                    Login -> stringResource(id = R.string.item_type_filter_login) +
                        successState?.summary?.login?.let { " ($it)" }

                    Alias -> stringResource(id = R.string.item_type_filter_alias) +
                        successState?.summary?.alias?.let { " ($it)" }

                    Note -> stringResource(id = R.string.item_type_filter_note) +
                        successState?.summary?.note?.let { " ($it)" }

                    CreditCard -> stringResource(id = R.string.item_type_filter_credit_card) +
                        successState?.summary?.creditCard?.let { " ($it)" }

                    Identity -> stringResource(id = R.string.item_type_filter_identity) +
                        successState?.summary?.identities?.let { " ($it)" }

                    LoginMFA -> stringResource(id = R.string.item_type_filter_login_mfa) +
                        successState?.summary?.login?.let { " ($it)" }
                }
                BottomSheetItemTitle(text = title, color = color)
            }
        override val subtitle: @Composable (() -> Unit)?
            get() = null
        override val leftIcon: @Composable (() -> Unit)
            get() = {
                val successState = state as? SuccessFilterOptionsUIState
                BottomSheetItemIcon(
                    iconId = when (item) {
                        All -> CoreR.drawable.ic_proton_list_bullets
                        Login -> CoreR.drawable.ic_proton_user
                        Alias -> CoreR.drawable.ic_proton_alias
                        Note -> CoreR.drawable.ic_proton_file_lines
                        CreditCard -> CoreR.drawable.ic_proton_credit_card
                        Identity -> CoreR.drawable.ic_proton_card_identity
                        LoginMFA -> CoreR.drawable.ic_proton_lock
                    },
                    tint = if (item == successState?.filterType) {
                        PassTheme.colors.interactionNorm
                    } else {
                        PassTheme.colors.textNorm
                    }
                )
            }
        override val endIcon: @Composable (() -> Unit)?
            get() = if (item == (state as? SuccessFilterOptionsUIState)?.filterType) {
                {
                    BottomSheetItemIcon(
                        iconId = CoreR.drawable.ic_proton_checkmark,
                        tint = PassTheme.colors.interactionNormMajor1
                    )
                }
            } else null
        override val onClick: () -> Unit
            get() = { onSortingTypeSelected(item) }
        override val isDivider = false
    }
}
    .toImmutableList()

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun FilterBottomSheetContentsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            FilterBottomSheetContents(
                state = SuccessFilterOptionsUIState(
                    filterType = All,
                    summary = ItemCountSummary(
                        total = 0,
                        login = 0,
                        note = 0,
                        alias = 0,
                        creditCard = 0,
                        identities = 0
                    )
                ),
                onSortingTypeSelected = {}
            )
        }
    }
}

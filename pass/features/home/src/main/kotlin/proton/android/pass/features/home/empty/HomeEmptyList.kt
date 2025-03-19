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

package proton.android.pass.features.home.empty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.features.home.R
import proton.android.pass.searchoptions.api.SearchFilterType
import me.proton.core.presentation.R as CoreR

@Composable
internal fun HomeEmptyList(
    modifier: Modifier = Modifier,
    canCreateItems: Boolean,
    isCustomItemEnabled: Boolean,
    filterType: SearchFilterType,
    onCreateLoginClick: () -> Unit,
    onCreateAliasClick: () -> Unit,
    onCreateNoteClick: () -> Unit,
    onCreateCreditCardClick: () -> Unit,
    onCreateIdentityClick: () -> Unit,
    onCreateCustomItemClick: () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.Center
    ) {
        HomeEmptyHeader(
            modifier = Modifier.padding(bottom = Spacing.large),
            filterType = filterType,
            canCreateItems = canCreateItems
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            val visibleButtons = remember(canCreateItems, filterType) {
                if (!canCreateItems) return@remember emptySet()

                when (filterType) {
                    SearchFilterType.All -> SearchFilterType.entries
                    SearchFilterType.Login, SearchFilterType.LoginMFA -> setOf(
                        SearchFilterType.Login,
                        SearchFilterType.LoginMFA
                    )

                    SearchFilterType.Alias -> setOf(SearchFilterType.Alias)
                    SearchFilterType.Note -> setOf(SearchFilterType.Note)
                    SearchFilterType.CreditCard -> setOf(SearchFilterType.CreditCard)
                    SearchFilterType.Identity -> setOf(SearchFilterType.Identity)
                    SearchFilterType.Custom -> setOf(SearchFilterType.Custom)
                    SearchFilterType.SharedWithMe,
                    SearchFilterType.SharedByMe -> emptySet()
                }
            }

            if (SearchFilterType.Login in visibleButtons || SearchFilterType.LoginMFA in visibleButtons) {
                HomeEmptyButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.home_empty_vault_create_login),
                    backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                    textColor = PassTheme.colors.loginInteractionNormMajor2,
                    icon = CoreR.drawable.ic_proton_user,
                    onClick = onCreateLoginClick
                )
            }

            if (SearchFilterType.Alias in visibleButtons) {
                HomeEmptyButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.home_empty_vault_create_alias),
                    backgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                    textColor = PassTheme.colors.aliasInteractionNormMajor2,
                    icon = CoreR.drawable.ic_proton_alias,
                    onClick = onCreateAliasClick
                )
            }

            if (SearchFilterType.Note in visibleButtons) {
                HomeEmptyButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.home_empty_vault_create_note),
                    backgroundColor = PassTheme.colors.noteInteractionNormMinor1,
                    textColor = PassTheme.colors.noteInteractionNormMajor2,
                    icon = CoreR.drawable.ic_proton_notepad_checklist,
                    onClick = onCreateNoteClick
                )
            }

            if (SearchFilterType.CreditCard in visibleButtons) {
                HomeEmptyButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.home_empty_vault_create_credit_card),
                    backgroundColor = PassTheme.colors.cardInteractionNormMinor1,
                    textColor = PassTheme.colors.cardInteractionNormMajor2,
                    icon = CoreR.drawable.ic_proton_credit_card,
                    onClick = onCreateCreditCardClick
                )
            }

            if (SearchFilterType.Identity in visibleButtons) {
                HomeEmptyButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.home_empty_vault_create_identity),
                    backgroundColor = PassTheme.colors.interactionNormMinor1,
                    textColor = PassTheme.colors.interactionNormMajor2,
                    icon = CoreR.drawable.ic_proton_card_identity,
                    onClick = onCreateIdentityClick
                )
            }
            if (isCustomItemEnabled && SearchFilterType.Custom in visibleButtons) {
                HomeEmptyButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.home_empty_vault_create_custom_item),
                    backgroundColor = PassPalette.SlateGray.copy(alpha = 0.3f),
                    textColor = ProtonTheme.colors.textNorm,
                    icon = CoreR.drawable.ic_proton_pencil,
                    onClick = onCreateCustomItemClick
                )
            }
        }
    }
}

internal class SearchFilterTypePreviewProvider : PreviewParameterProvider<SearchFilterType> {
    override val values: Sequence<SearchFilterType>
        get() = SearchFilterType.entries.asSequence()
}

internal class ThemedSearchFilterTypePreviewProvider : ThemePairPreviewProvider<SearchFilterType>(
    provider = SearchFilterTypePreviewProvider()
)

@[Preview Composable]
internal fun HomeEmptyListPreview(
    @PreviewParameter(ThemedSearchFilterTypePreviewProvider::class) input: Pair<Boolean, SearchFilterType>
) {
    PassTheme(isDark = input.first) {
        Surface {
            HomeEmptyList(
                filterType = input.second,
                canCreateItems = true,
                isCustomItemEnabled = true,
                onCreateLoginClick = {},
                onCreateAliasClick = {},
                onCreateNoteClick = {},
                onCreateCreditCardClick = {},
                onCreateIdentityClick = {},
                onCreateCustomItemClick = {}
            )
        }
    }
}

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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider

@Composable
internal fun CreateAliasSection(
    modifier: Modifier = Modifier,
    state: AliasItemFormState,
    canEdit: Boolean,
    canSelectSuffix: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    onAdvancedOptionsClicked: () -> Unit,
    onChange: (String) -> Unit,
    onSuffixClick: () -> Unit
) {
    Column(modifier) {
        AliasToBeCreated(
            prefix = state.prefix,
            suffix = state.selectedSuffix,
            isError = onAliasRequiredError || onInvalidAliasError
        )
        AliasAdvancedOptions(
            enabled = canEdit,
            prefix = state.prefix,
            suffix = state.selectedSuffix,
            isError = onAliasRequiredError || onInvalidAliasError,
            onAdvancedOptionsClicked = onAdvancedOptionsClicked,
            canSelectSuffix = canSelectSuffix,
            onPrefixChanged = onChange,
            onSuffixClicked = onSuffixClick
        )
    }
}

class ThemedCreateAliasSectionPreviewProvider :
    ThemePairPreviewProvider<CreateAliasSectionPreviewParameter>(CreateAliasSectionPreviewProvider())

@Preview
@Composable
fun CreateAliasSectionPreview(
    @PreviewParameter(ThemedCreateAliasSectionPreviewProvider::class)
    input: Pair<Boolean, CreateAliasSectionPreviewParameter>
) {
    val param = input.second
    PassTheme(isDark = input.first) {
        Surface {
            CreateAliasSection(
                state = param.aliasItemFormState,
                canEdit = param.canEdit,
                canSelectSuffix = true,
                onAliasRequiredError = param.onAliasRequiredError,
                onInvalidAliasError = param.onInvalidAliasError,
                onAdvancedOptionsClicked = {},
                onChange = {},
                onSuffixClick = {}
            )
        }
    }
}

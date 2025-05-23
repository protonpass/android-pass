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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class CreateAliasSectionPreviewProvider :
    PreviewParameterProvider<CreateAliasSectionPreviewParameter> {
    override val values: Sequence<CreateAliasSectionPreviewParameter>
        get() = sequenceOf(
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = false,
                onInvalidAliasError = false,
                aliasItemFormState = aliasItem("")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = false,
                onInvalidAliasError = false,
                aliasItemFormState = aliasItem("some.alias")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = false,
                onInvalidAliasError = false,
                aliasItemFormState = aliasItem("this.is.a.very.very.long.alias.that.should.appear.in.two.lines")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = false,
                onAliasRequiredError = false,
                onInvalidAliasError = false,
                aliasItemFormState = aliasItem("some.alias")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = true,
                onInvalidAliasError = false,
                aliasItemFormState = aliasItem("")
            ),
            CreateAliasSectionPreviewParameter(
                canEdit = true,
                onAliasRequiredError = false,
                onInvalidAliasError = true,
                aliasItemFormState = aliasItem("invalid!alias")
            )
        )


    private fun aliasItem(alias: String) = AliasItemFormState(
        prefix = alias,
        selectedSuffix = AliasSuffixUiModel(
            suffix = "@random.suffix",
            signedSuffix = "",
            isCustom = false,
            isPremium = false,
            domain = "random.suffix"
        ),
        aliasToBeCreated = "$alias@random.suffix",
        customFields = emptyList()
    )
}

data class CreateAliasSectionPreviewParameter(
    val aliasItemFormState: AliasItemFormState,
    val canEdit: Boolean,
    val onAliasRequiredError: Boolean,
    val onInvalidAliasError: Boolean
)

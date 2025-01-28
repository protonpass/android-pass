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

class AliasToBeCreatedPreviewProvider : PreviewParameterProvider<AliasToBeCreatedInput> {
    override val values: Sequence<AliasToBeCreatedInput>
        get() = sequenceOf(
            AliasToBeCreatedInput(prefix = "", suffix = null),
            AliasToBeCreatedInput(prefix = "prefix", suffix = null),
            AliasToBeCreatedInput(prefix = "", suffix = suffix(".some@suffix.test")),
            AliasToBeCreatedInput(prefix = "prefix", suffix = suffix(".some@suffix.test")),
            AliasToBeCreatedInput(
                prefix = "prefix.that.is.super.long.in.order.to.trigger.newlines",
                suffix = suffix(".some@suffix.test")
            )
        )

    private fun suffix(suffix: String) = AliasSuffixUiModel(
        suffix = suffix,
        signedSuffix = "",
        isCustom = false,
        isPremium = false,
        domain = ""
    )
}

data class AliasToBeCreatedInput(
    val prefix: String,
    val suffix: AliasSuffixUiModel?
)

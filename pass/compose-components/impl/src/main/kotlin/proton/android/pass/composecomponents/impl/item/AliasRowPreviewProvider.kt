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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

class AliasRowPreviewProvider : PreviewParameterProvider<AliasRowParameter> {
    override val values: Sequence<AliasRowParameter>
        get() = sequenceOf(
            with(title = "Empty alias", alias = ""),
            with(title = "Disabled alias", alias = "", disabled = true),
            with(title = "With content", alias = "somealias@random.local"),
            with(
                title = "With very long content",
                alias = "somealias.withsuffix.thatisverylong." +
                    "itwouldnotfit@please.ellipsize.this.alias.local"
            ),
            with(
                title = "With very long content to check highlight",
                alias = "somealias.withsuffix.thatisverylong." +
                    "itwouldnotfit@please.ellipsize.this.alias.local",
                note = "A note with a long text to verify that the word local is highlighted",
                highlight = "local"
            ),
            with(
                title = "With multiline content to check highlight",
                alias = "somealias.withsuffix.thatisverylong.",
                note = "A note \n with \n multiline \n text \n to \n verify " +
                    "\n that the \n word \n local \n is highlighted",
                highlight = "local"
            )
        )

    companion object {
        private fun with(
            title: String,
            alias: String,
            note: String = "",
            highlight: String = "",
            disabled: Boolean = false
        ) = AliasRowParameter(
            model = ItemUiModel(
                id = ItemId("123"),
                userId = UserId("user-id"),
                shareId = ShareId("456"),
                contents = ItemContents.Alias(title, note, emptyList(), alias, disabled),
                state = 0,
                createTime = Clock.System.now(),
                modificationTime = Clock.System.now(),
                lastAutofillTime = Clock.System.now(),
                isPinned = false,
                pinTime = Clock.System.now(),
                revision = 1,
                shareCount = 0,
                shareType = ShareType.Vault
            ),
            highlight = highlight
        )
    }
}

data class AliasRowParameter(
    val model: ItemUiModel,
    val highlight: String
)

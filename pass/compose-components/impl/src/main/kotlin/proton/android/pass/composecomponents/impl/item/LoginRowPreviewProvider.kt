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
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

class LoginRowPreviewProvider : PreviewParameterProvider<LoginRowParameter> {
    override val values: Sequence<LoginRowParameter>
        get() = sequenceOf(
            with(title = "Empty username", username = ""),
            with(title = "This is a login item", username = "the username"),
            with(
                title = "Very long text",
                username = "this is a very long username that should become " +
                    "ellipsized if the text does not fit properly"
            ),
            with(
                title = "Very long text",
                username = "this is a very long username that should " +
                    "highlight the word proton during the rendering",
                note = "this is a very long note that should " +
                    "highlight the word proton during the rendering",
                websites = listOf(
                    "https://somerandomwebsite.com/",
                    "https://proton.ch/",
                    "https://proton.me/",
                    "https://anotherrandomwebsite.com/"
                ),
                highlight = "proton"
            ),
            with(
                title = "With multiline content to check highlight",
                username = "username",
                note = "A note \n with \n multiline \n text \n to \n verify \n that" +
                    " the \n word \n local \n is highlighted",
                highlight = "local"
            )
        )

    companion object {
        private fun with(
            title: String,
            username: String,
            note: String = "Note content",
            websites: List<String> = emptyList(),
            highlight: String = ""
        ): LoginRowParameter =
            LoginRowParameter(
                model = ItemUiModel(
                    id = ItemId("123"),
                    shareId = ShareId("345"),
                    contents = ItemContents.Login(
                        title = title,
                        note = note,
                        username = username,
                        password = HiddenState.Concealed(""),
                        urls = websites,
                        packageInfoSet = emptySet(),
                        primaryTotp = HiddenState.Concealed(""),
                        customFields = emptyList()
                    ),
                    state = 0,
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now(),
                    isPinned = false,
                ),
                highlight = highlight
            )

    }
}

data class LoginRowParameter(
    val model: ItemUiModel,
    val highlight: String
)

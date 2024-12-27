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

package proton.android.pass.features.itemcreate.note

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class NoteTitlePreviewProvider : PreviewParameterProvider<NoteTitleInput> {
    override val values: Sequence<NoteTitleInput>
        get() = sequence {
            for (enabled in listOf(true, false)) {
                for (isError in listOf(true, false)) {
                    for (text in TEXTS) {
                        yield(NoteTitleInput(enabled = enabled, isError = isError, text = text))
                    }
                }
            }
        }

    companion object {
        private val TEXTS = listOf(
            "",
            "some text",
            "some text that should wrap into more than one line so we can see if it supports multiline"
        )
    }
}

data class NoteTitleInput(
    val enabled: Boolean,
    val isError: Boolean,
    val text: String
)

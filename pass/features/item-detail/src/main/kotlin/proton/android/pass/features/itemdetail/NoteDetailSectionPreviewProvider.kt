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

package proton.android.pass.features.itemdetail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class NoteDetailSectionPreviewProvider : PreviewParameterProvider<String> {

    override val values: Sequence<String> = sequenceOf(
        "",
        "Some note",
        "Some very long note that contains a lot of text in a single line and it should " +
            "be converted into multiline",
        """
            Some note
            That contains text
            In multiple lines
                
            And even contains a very long line that should also appear in a new line expecting
            nothing will break and the UI will be able to handle it
        """.trimIndent()
    )

}

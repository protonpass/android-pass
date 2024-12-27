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

package proton.android.pass.features.itemcreate.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import java.util.Date

class CreateUpdateTopBarPreviewProvider : PreviewParameterProvider<CreateUpdateTopBarPreview> {
    override val values: Sequence<CreateUpdateTopBarPreview>
        get() = sequenceOf(
            CreateUpdateTopBarPreview(
                isLoading = false,
                actionColor = PassPalette.Lavender100,
                closeIconColor = PassPalette.Lavender100,
                closeBackgroundColor = PassPalette.Lavender8,
                vault = null
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                actionColor = PassPalette.GreenSheen100,
                closeIconColor = PassPalette.GreenSheen100,
                closeBackgroundColor = PassPalette.GreenSheen8,
                vault = null
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                actionColor = PassPalette.MacaroniAndCheese100,
                closeIconColor = PassPalette.MacaroniAndCheese100,
                closeBackgroundColor = PassPalette.MacaroniAndCheese8,
                vault = null
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                actionColor = PassPalette.Lavender100,
                closeIconColor = PassPalette.Lavender100,
                closeBackgroundColor = PassPalette.Lavender8,
                vault = null
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                actionColor = PassPalette.GreenSheen100,
                closeIconColor = PassPalette.GreenSheen100,
                closeBackgroundColor = PassPalette.GreenSheen8,
                vault = null
            ),
            CreateUpdateTopBarPreview(
                isLoading = true,
                actionColor = PassPalette.MacaroniAndCheese100,
                closeIconColor = PassPalette.MacaroniAndCheese100,
                closeBackgroundColor = PassPalette.MacaroniAndCheese8,
                vault = null
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                actionColor = PassPalette.GreenSheen100,
                closeIconColor = PassPalette.GreenSheen100,
                closeBackgroundColor = PassPalette.GreenSheen8,
                showUpgrade = true,
                vault = null
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                actionColor = PassPalette.GreenSheen100,
                closeIconColor = PassPalette.GreenSheen100,
                closeBackgroundColor = PassPalette.GreenSheen8,
                vault = Vault(
                    userId = UserId(id = ""),
                    shareId = ShareId(id = ""),
                    vaultId = VaultId("123"),
                    name = "Personal",
                    color = ShareColor.Color2,
                    icon = ShareIcon.Icon13,
                    createTime = Date()
                )
            ),
            CreateUpdateTopBarPreview(
                isLoading = false,
                actionColor = PassPalette.GreenSheen100,
                closeIconColor = PassPalette.GreenSheen100,
                closeBackgroundColor = PassPalette.GreenSheen8,
                vault = Vault(
                    userId = UserId(id = ""),
                    shareId = ShareId(id = ""),
                    vaultId = VaultId("123"),
                    name = "Vault with a long title that should be truncated",
                    color = ShareColor.Color2,
                    icon = ShareIcon.Icon13,
                    createTime = Date()
                )
            )
        )
}

data class CreateUpdateTopBarPreview(
    val isLoading: Boolean,
    val showUpgrade: Boolean = false,
    val actionColor: Color,
    val closeBackgroundColor: Color,
    val closeIconColor: Color,
    val vault: Vault?
)

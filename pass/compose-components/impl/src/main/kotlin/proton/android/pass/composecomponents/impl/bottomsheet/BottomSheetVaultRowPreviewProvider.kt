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

package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import java.util.Date

class BottomSheetVaultRowPreviewProvider : PreviewParameterProvider<VaultRowInput> {
    override val values: Sequence<VaultRowInput>
        get() = sequence {
            for (isSelected in listOf(true, false)) {
                for (enabled in listOf(true, false)) {
                    for (isLoading in listOf(true, false)) {
                        for (isShared in listOf(true, false)) {
                            yield(
                                VaultRowInput(
                                    vault = VaultWithItemCount(
                                        vault = Vault(
                                            userId = UserId("123"),
                                            shareId = ShareId("123"),
                                            vaultId = VaultId("123"),
                                            name = "some vault",
                                            color = ShareColor.Color2,
                                            icon = ShareIcon.Icon10,
                                            shared = isShared,
                                            createTime = Date(),
                                            shareFlags = ShareFlags(0)
                                        ),
                                        activeItemCount = 2,
                                        trashedItemCount = 0
                                    ),
                                    isSelected = isSelected,
                                    enabled = enabled,
                                    isLoading = isLoading
                                )
                            )
                        }
                    }
                }
            }
        }
}

data class VaultRowInput(
    val vault: VaultWithItemCount,
    val isSelected: Boolean,
    val enabled: Boolean,
    val isLoading: Boolean
)

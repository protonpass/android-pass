/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.sync.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.features.sync.presentation.SyncDialogState
import java.util.Date

internal class SyncDialogContentPreviewProvider : PreviewParameterProvider<SyncDialogState> {

    override val values: Sequence<SyncDialogState> = sequence {
        for (hasInvalidAddressShares in listOf(false, true)) {
            for (hasInvalidGroupShares in listOf(false, true)) {
                yield(
                    SyncDialogState(
                        itemSyncStatus = ItemSyncStatus.SyncSuccess(
                            hasInactiveShares = false,
                            hasInvalidGroupShares = hasInvalidGroupShares,
                            hasInvalidAddressShares = hasInvalidAddressShares
                        ),
                        downloadedItemsMap = emptyMap(),
                        insertedItems = None,
                        vaultsLoadingResult = LoadingResult.Success(
                            listOf(
                                previewVault("share-1", "Personal"),
                                previewVault("share-2", "Work")
                            )
                        )
                    )
                )
            }
        }
    }

    private fun previewVault(id: String, name: String) = Vault(
        userId = UserId("user-id"),
        shareId = ShareId(id),
        vaultId = VaultId(id),
        name = name,
        color = ShareColor.Color1,
        icon = ShareIcon.Icon1,
        createTime = Date(),
        shareFlags = ShareFlags(0)
    )

}

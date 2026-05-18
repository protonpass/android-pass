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

package proton.android.pass.features.migrate.confirmvault

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import java.util.Date

internal class MigrateConfirmVaultContentsPreviewProvider :
    PreviewParameterProvider<MigrateConfirmVaultUiState> {

    override val values: Sequence<MigrateConfirmVaultUiState> = sequenceOf(
        MigrateConfirmVaultUiState.initial(MigrateMode.MigrateSelectedItems(3))
            .copy(
                isLoadingVaults = false,
                vaultList = persistentListOf(
                    MigrateVaultState(
                        vaultWithItemCount = VaultWithItemCount(
                            vault = Vault(
                                userId = UserId("user-id"),
                                shareId = ShareId("share-1"),
                                vaultId = VaultId("vault-1"),
                                name = "Personal",
                                createTime = Date(),
                                shareFlags = ShareFlags(0)
                            ),
                            activeItemCount = 12,
                            trashedItemCount = 0
                        ),
                        status = VaultStatus.Enabled
                    ),
                    MigrateVaultState(
                        vaultWithItemCount = VaultWithItemCount(
                            vault = Vault(
                                userId = UserId("user-id"),
                                shareId = ShareId("share-2"),
                                vaultId = VaultId("vault-2"),
                                name = "Work",
                                createTime = Date(),
                                shareFlags = ShareFlags(0)
                            ),
                            activeItemCount = 5,
                            trashedItemCount = 0
                        ),
                        status = VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault)
                    )
                )
            )
    )
}

internal class ThemeMigrateConfirmVaultContentsPreviewProvider :
    ThemePairPreviewProvider<MigrateConfirmVaultUiState>(MigrateConfirmVaultContentsPreviewProvider())

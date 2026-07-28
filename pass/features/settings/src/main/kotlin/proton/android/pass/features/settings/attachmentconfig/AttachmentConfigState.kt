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

package proton.android.pass.features.settings.attachmentconfig

import androidx.compose.runtime.Stable
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId

@Stable
internal data class AttachmentConfigState(
    val allVaultsEnabled: Boolean,
    val vaults: List<VaultConfigItem>,
    val sharedItemsEnabled: Boolean,
    val allowCellular: Boolean,
    val event: AttachmentConfigEvent
) {

    val canStartDownload: Boolean
        get() = allVaultsEnabled ||
            vaults.any { it.offlineEnabled } ||
            sharedItemsEnabled

    val totalSizeBytes: Long?
        get() = if (vaults.isEmpty() || vaults.any { it.totalSizeBytes == null }) {
            null
        } else {
            vaults.sumOf { it.totalSizeBytes ?: 0L }
        }

    internal companion object {

        internal val Initial = AttachmentConfigState(
            allVaultsEnabled = true,
            vaults = emptyList(),
            sharedItemsEnabled = true,
            allowCellular = false,
            event = AttachmentConfigEvent.None
        )
    }
}

@Stable
internal data class VaultConfigItem(
    val shareId: ShareId,
    val name: String,
    val color: ShareColor,
    val icon: ShareIcon,
    val offlineEnabled: Boolean,
    val totalSizeBytes: Long?
)

internal sealed interface AttachmentConfigEvent {
    data object None : AttachmentConfigEvent
    data object StartDownload : AttachmentConfigEvent
}

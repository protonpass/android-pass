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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId

internal class AttachmentConfigPreviewProvider :
    PreviewParameterProvider<Pair<Boolean, AttachmentConfigState>> {

    private val vaults = listOf(
        VaultConfigItem(
            shareId = ShareId("1"),
            name = "Personal",
            color = ShareColor.Color1,
            icon = ShareIcon.Icon1,
            offlineEnabled = true,
            totalSizeBytes = 1_572_864L
        ),
        VaultConfigItem(
            shareId = ShareId("2"),
            name = "Work",
            color = ShareColor.Color2,
            icon = ShareIcon.Icon2,
            offlineEnabled = false,
            totalSizeBytes = 4_718_592L
        )
    )

    private val states = sequenceOf(
        AttachmentConfigState(
            allVaultsEnabled = true,
            vaults = vaults,
            sharedItemsEnabled = true,
            allowCellular = false,
            event = AttachmentConfigEvent.None
        ),
        AttachmentConfigState(
            allVaultsEnabled = false,
            vaults = vaults.map { it.copy(offlineEnabled = false) },
            sharedItemsEnabled = false,
            allowCellular = true,
            event = AttachmentConfigEvent.None
        )
    )

    override val values: Sequence<Pair<Boolean, AttachmentConfigState>> =
        states.flatMap { state ->
            sequenceOf(false to state, true to state)
        }
}

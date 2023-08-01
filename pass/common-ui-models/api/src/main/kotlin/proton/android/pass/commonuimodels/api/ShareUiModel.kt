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

package proton.android.pass.commonuimodels.api

import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

data class ShareUiModel(
    val id: ShareId,
    val name: String,
    val color: ShareColor,
    val icon: ShareIcon,
    val isPrimary: Boolean,
    val isShared: Boolean
) {
    companion object {
        fun fromVault(vault: Vault) = ShareUiModel(
            id = vault.shareId,
            name = vault.name,
            color = vault.color,
            icon = vault.icon,
            isPrimary = vault.isPrimary,
            isShared = vault.isShared()
        )
    }
}

data class ShareUiModelWithItemCount(
    val id: ShareId,
    val name: String,
    val activeItemCount: Long,
    val trashedItemCount: Long,
    val color: ShareColor,
    val icon: ShareIcon,
    val isPrimary: Boolean,
    val isShared: Boolean
)

/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo

data class ShareKeyView(
    @ColumnInfo(name = ShareEntity.Columns.ID)
    val shareId: String,
    @ColumnInfo(name = ShareEntity.Columns.VAULT_ID)
    val vaultId: String,
    @ColumnInfo(name = ShareEntity.Columns.SHARE_TYPE)
    val targetType: Int,
    @ColumnInfo(name = ShareEntity.Columns.TARGET_ID)
    val targetId: String,
    @ColumnInfo(name = ShareEntity.Columns.SHARE_ROLE_ID)
    val roleId: String,
    @ColumnInfo(name = ShareEntity.Columns.PERMISSION)
    val permissions: Int
)

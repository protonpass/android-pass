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

package proton.android.pass.featurehome.impl

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId

val ShareUiModelWithItemCountSaver: Saver<ShareUiModelWithItemCount?, Any> = run {
    val shareId = "share_id"
    val name = "name"
    val icon = "icon"
    val color = "color"
    val activeItemCount = "active_item_count"
    val trashedItemCount = "trashed_item_count"
    val isPrimary = "is_primary"
    mapSaver(
        save = {
            if (it != null) {
                mapOf(
                    shareId to it.id.id,
                    name to it.name,
                    icon to it.icon.name,
                    color to it.color.name,
                    activeItemCount to it.activeItemCount,
                    trashedItemCount to it.trashedItemCount,
                    isPrimary to it.isPrimary

                )
            } else {
                emptyMap()
            }
        },
        restore = { values ->
            if (values.isNotEmpty()) {
                ShareUiModelWithItemCount(
                    id = ShareId(id = values[shareId] as String),
                    name = values[name] as String,
                    icon = ShareIcon.valueOf(values[icon] as String),
                    color = ShareColor.valueOf(values[color] as String),
                    activeItemCount = values[activeItemCount] as Long,
                    trashedItemCount = values[trashedItemCount] as Long,
                    isPrimary = values[isPrimary] as Boolean
                )
            } else {
                null
            }
        }
    )
}


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

package proton.android.pass.featurehome.impl.saver

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

val ItemUiModelSaver: Saver<ItemUiModel?, Any> = run {
    val itemId = "item_id"
    val shareId = "share_id"
    val itemContent = "item_content"
    val createTime = "create_time"
    val modificationTime = "modification_time"
    val lastAutofillTime = "last_autofill_time"
    val canModify = "can_modify"
    val isPinned = "is_pinned"
    val isExcludedFromMonitor = "is_excluded_from_monitor"
    mapSaver(
        save = {
            it?.let { itemUiModel ->
                mapOf(
                    itemId to itemUiModel.id.id,
                    shareId to itemUiModel.shareId.id,
                    itemContent to Json.encodeToString(itemUiModel.contents),
                    createTime to itemUiModel.createTime.toString(),
                    modificationTime to itemUiModel.modificationTime.toString(),
                    lastAutofillTime to itemUiModel.lastAutofillTime?.toString(),
                    canModify to itemUiModel.canModify,
                    isPinned to itemUiModel.isPinned,
                    isExcludedFromMonitor to itemUiModel.isExcludedFromMonitor
                )
            } ?: emptyMap()
        },
        restore = { values ->
            if (values.isNotEmpty()) {
                ItemUiModel(
                    id = ItemId(id = values[itemId] as String),
                    shareId = ShareId(id = values[shareId] as String),
                    contents = Json.decodeFromString(values[itemContent] as String),
                    state = 0,
                    createTime = (values[createTime] as String).let { Instant.parse(it) },
                    modificationTime = (values[modificationTime] as String).let { Instant.parse(it) },
                    lastAutofillTime = (values[modificationTime] as? String)?.let { Instant.parse(it) },
                    canModify = values[canModify] as Boolean,
                    isPinned = values[isPinned] as Boolean,
                    isExcludedFromMonitor = values[isExcludedFromMonitor] as Boolean
                )
            } else {
                null
            }
        }
    )
}


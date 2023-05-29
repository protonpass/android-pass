package proton.android.pass.featurehome.impl

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

val ItemUiModelSaver: Saver<ItemUiModel?, Any> = run {
    val itemId = "item_id"
    val shareId = "share_id"
    val itemContent = "item_content"
    val createTime = "create_time"
    val modificationTime = "modification_time"
    val lastAutofillTime = "last_autofill_time"
    mapSaver(
        save = {
            if (it != null) {
                mapOf(
                    itemId to it.id.id,
                    shareId to it.shareId.id,
                    itemContent to Json.encodeToString(it.contents),
                    createTime to it.createTime.toString(),
                    modificationTime to it.modificationTime.toString(),
                    lastAutofillTime to it.lastAutofillTime?.toString(),
                )
            } else {
                emptyMap()
            }
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
                    lastAutofillTime = (values[modificationTime] as? String)?.let { Instant.parse(it) }
                )
            } else {
                null
            }
        }
    )
}


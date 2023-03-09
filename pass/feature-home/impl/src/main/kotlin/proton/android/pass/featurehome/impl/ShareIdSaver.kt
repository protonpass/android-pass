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
    mapSaver(
        save = {
            if (it != null) {
                mapOf(
                    shareId to it.id,
                    name to it.name,
                    icon to it.icon.name,
                    color to it.color.name,
                    activeItemCount to it.activeItemCount,
                    trashedItemCount to it.trashedItemCount

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
                )
            } else {
                null
            }
        }
    )
}


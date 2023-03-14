package proton.android.pass.featureitemcreate.impl.alias.saver

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import proton.android.pass.featureitemcreate.impl.alias.AliasBottomSheetContentType

val AliasBottomSheetContentTypeSaver: Saver<AliasBottomSheetContentType, Any> = run {
    val variant = "variant"
    mapSaver(
        save = {
            mapOf(variant to it.name)
        },
        restore = { values ->
            if (values.isNotEmpty()) {
                AliasBottomSheetContentType.valueOf(values[variant] as String)
            } else {
                AliasBottomSheetContentType.AliasOptions
            }
        }
    )
}

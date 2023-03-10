package proton.android.pass.featurehome.impl.saver

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import proton.android.pass.featurehome.impl.HomeBottomSheetType

val HomeBottomSheetTypeSaver: Saver<HomeBottomSheetType, Any> = run {
    val variant = "variant"
    mapSaver(
        save = {
            mapOf(variant to it.name)
        },
        restore = { values ->
            if (values.isNotEmpty()) {
                HomeBottomSheetType.valueOf(values[variant] as String)
            } else {
                HomeBottomSheetType.Sorting
            }
        }
    )
}

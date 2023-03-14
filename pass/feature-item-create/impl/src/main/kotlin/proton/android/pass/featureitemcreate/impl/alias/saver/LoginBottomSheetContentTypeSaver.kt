package proton.android.pass.featureitemcreate.impl.alias.saver

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.LoginBottomSheetContentType

val LoginBottomSheetContentTypeSaver: Saver<LoginBottomSheetContentType, Any> = run {
    val variant = "variant"
    mapSaver(
        save = {
            mapOf(variant to it.name)
        },
        restore = { values ->
            if (values.isNotEmpty()) {
                LoginBottomSheetContentType.valueOf(values[variant] as String)
            } else {
                LoginBottomSheetContentType.GeneratePassword
            }
        }
    )
}

package proton.android.pass.featurecreateitem.impl.login.bottomsheet

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import proton.android.pass.featurecreateitem.impl.R

enum class AddTotpType(@StringRes val textId: Int, @DrawableRes val iconId: Int) {
    Camera(R.string.totp_add_via_camera, me.proton.core.presentation.R.drawable.ic_proton_camera),
    File(R.string.totp_add_via_file, me.proton.core.presentation.R.drawable.ic_proton_file),
    Manual(R.string.totp_add_via_manual, me.proton.core.presentation.R.drawable.ic_proton_pencil)
}

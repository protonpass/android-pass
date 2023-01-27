package proton.android.pass.featurecreateitem.impl.login.bottomsheet

import androidx.annotation.StringRes
import proton.android.pass.featurecreateitem.impl.R

enum class AddTotpType(@StringRes val textId: Int) {
    Camera(R.string.totp_add_via_camera),
    File(R.string.totp_add_via_file),
    Manual(R.string.totp_add_via_manual)
}

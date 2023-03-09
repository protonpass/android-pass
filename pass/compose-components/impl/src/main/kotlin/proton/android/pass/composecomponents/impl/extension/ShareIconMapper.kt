package proton.android.pass.composecomponents.impl.extension

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import me.proton.core.presentation.R
import proton.pass.domain.ShareIcon

@Composable
@DrawableRes
fun ShareIcon.toResource(): Int = when (this) {
    ShareIcon.Icon1 -> R.drawable.ic_proton_house
    ShareIcon.Icon2 -> R.drawable.ic_proton_briefcase
    ShareIcon.Icon3 -> R.drawable.ic_proton_pencil
    ShareIcon.Icon4 -> R.drawable.ic_proton_checkmark
    ShareIcon.Icon5 -> R.drawable.ic_proton_users
    ShareIcon.Icon6 -> R.drawable.ic_proton_cog_wheel
    ShareIcon.Icon7 -> R.drawable.ic_proton_alias
    ShareIcon.Icon8 -> R.drawable.ic_proton_brand_linux
    ShareIcon.Icon9 -> R.drawable.ic_proton_brand_apple
    ShareIcon.Icon10 -> R.drawable.ic_proton_brand_windows
}

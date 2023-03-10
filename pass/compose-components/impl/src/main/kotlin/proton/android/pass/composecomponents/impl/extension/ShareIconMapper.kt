package proton.android.pass.composecomponents.impl.extension

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import me.proton.core.presentation.R
import proton.pass.domain.ShareIcon

@Suppress("ComplexMethod")
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
    ShareIcon.Icon11 -> R.drawable.ic_proton_house
    ShareIcon.Icon12 -> R.drawable.ic_proton_house
    ShareIcon.Icon13 -> R.drawable.ic_proton_house
    ShareIcon.Icon14 -> R.drawable.ic_proton_house
    ShareIcon.Icon15 -> R.drawable.ic_proton_house
    ShareIcon.Icon16 -> R.drawable.ic_proton_house
    ShareIcon.Icon17 -> R.drawable.ic_proton_house
    ShareIcon.Icon18 -> R.drawable.ic_proton_house
    ShareIcon.Icon19 -> R.drawable.ic_proton_house
    ShareIcon.Icon20 -> R.drawable.ic_proton_house
    ShareIcon.Icon21 -> R.drawable.ic_proton_house
    ShareIcon.Icon22 -> R.drawable.ic_proton_house
    ShareIcon.Icon23 -> R.drawable.ic_proton_house
    ShareIcon.Icon24 -> R.drawable.ic_proton_house
    ShareIcon.Icon25 -> R.drawable.ic_proton_house
    ShareIcon.Icon26 -> R.drawable.ic_proton_house
    ShareIcon.Icon27 -> R.drawable.ic_proton_house
    ShareIcon.Icon28 -> R.drawable.ic_proton_house
    ShareIcon.Icon29 -> R.drawable.ic_proton_house
    ShareIcon.Icon30 -> R.drawable.ic_proton_house
}

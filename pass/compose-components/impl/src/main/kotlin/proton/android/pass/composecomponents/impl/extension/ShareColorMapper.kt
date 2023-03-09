package proton.android.pass.composecomponents.impl.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import proton.android.pass.commonui.api.PassPalette
import proton.pass.domain.ShareColor

@Suppress("MagicNumber")
@Composable
fun ShareColor.toIconColor(): Color = when (this) {
    ShareColor.Color1 -> PassPalette.Purple100
    ShareColor.Color2 -> Color(0xFFF29292)
    ShareColor.Color3 -> Color(0xFFF7D775)
    ShareColor.Color4 -> Color(0xFF91C799)
    ShareColor.Color5 -> Color(0xFF92B3F2)
    ShareColor.Color6 -> Color(0xFFEB8DD6)
    ShareColor.Color7 -> Color(0xFFCD5A6F)
    ShareColor.Color8 -> Color(0xFFE4A367)
    ShareColor.Color9 -> Color(0xFFE6E6E6)
    ShareColor.Color10 -> Color(0xFF9EE2E6)
}

@Suppress("MagicNumber")
@Composable
fun ShareColor.toBackgroundColor(): Color = when (this) {
    ShareColor.Color1 -> PassPalette.Purple16
    ShareColor.Color2 -> Color(0x29F29292)
    ShareColor.Color3 -> Color(0x29F7D775)
    ShareColor.Color4 -> Color(0x2991C799)
    ShareColor.Color5 -> Color(0x2992B3F2)
    ShareColor.Color6 -> Color(0x29EB8DD6)
    ShareColor.Color7 -> Color(0x29CD5A6F)
    ShareColor.Color8 -> Color(0x29E4A367)
    ShareColor.Color9 -> Color(0x29E6E6E6)
    ShareColor.Color10 -> Color(0x299EE2E6)
}



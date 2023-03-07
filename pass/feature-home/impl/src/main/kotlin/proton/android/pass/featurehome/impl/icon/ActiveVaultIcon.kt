package proton.android.pass.featurehome.impl.icon

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareProperties

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ActiveVaultIcon(
    modifier: Modifier = Modifier,
    size: Int = 40,
    viewModel: ActiveVaultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val properties = stateToProperties(state.properties)

    VaultIcon(
        modifier = modifier,
        backgroundColor = properties.backgroundColor,
        iconColor = properties.iconColor,
        size = size,
        icon = properties.icon
    )
}

private data class VaultIconProperties(
    val iconColor: Color,
    val backgroundColor: Color,
    @DrawableRes val icon: Int
)

@Suppress("MagicNumber")
@Composable
private fun stateToProperties(state: ShareProperties): VaultIconProperties {
    val (iconColor, backgroundColor) = when (state.shareColor) {
        ShareColor.Purple -> PassPalette.Purple100 to PassPalette.Purple16
        ShareColor.Yellow -> PassPalette.Yellow100 to PassPalette.Yellow16
        ShareColor.Blue -> Color(0x2992B3F2) to Color(0xFF92B3F2)
        ShareColor.Green -> PassPalette.Green100 to PassPalette.Green16
    }

    val icon = when (state.shareIcon) {
        ShareIcon.House -> R.drawable.ic_proton_house
        ShareIcon.Suitcase -> R.drawable.ic_proton_briefcase
    }

    return VaultIconProperties(
        iconColor = iconColor,
        backgroundColor = backgroundColor,
        icon = icon
    )
}

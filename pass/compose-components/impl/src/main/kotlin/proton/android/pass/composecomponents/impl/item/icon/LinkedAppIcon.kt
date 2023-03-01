package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.item.placeholder
import proton.pass.domain.entity.PackageName

@Composable
fun LinkedAppIcon(
    modifier: Modifier = Modifier,
    packageName: String,
    size: Int = 40,
    emptyContent: @Composable () -> Unit
) {
    LinkedAppIcon(
        modifier = modifier,
        packageName = PackageName(packageName),
        size = size,
        emptyContent = emptyContent
    )
}

@Composable
fun LinkedAppIcon(
    modifier: Modifier = Modifier,
    packageName: PackageName,
    size: Int = 40,
    emptyContent: @Composable () -> Unit
) {
    SubcomposeAsyncImage(
        modifier = modifier
            .clip(CircleShape)
            .size(size.dp)
            .background(ProtonTheme.colors.backgroundNorm),
        model = packageName,
        contentDescription = null
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = Modifier
                        .placeholder()
                        .fillMaxSize()
                )
            }
            is AsyncImagePainter.State.Success -> {
                SubcomposeAsyncImageContent(modifier = Modifier.fillMaxSize())
            }
            else -> {
                emptyContent()
            }
        }
    }
}

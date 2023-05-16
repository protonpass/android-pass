package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import proton.android.pass.commonui.api.PassTheme
import proton.pass.domain.entity.PackageName

@Composable
fun LinkedAppIcon(
    modifier: Modifier = Modifier,
    packageName: String,
    size: Int = 40,
    shape: Shape,
    emptyContent: @Composable () -> Unit
) {
    LinkedAppIcon(
        modifier = modifier,
        packageName = PackageName(packageName),
        size = size,
        shape = shape,
        emptyContent = emptyContent
    )
}

@Composable
fun LinkedAppIcon(
    modifier: Modifier = Modifier,
    packageName: PackageName,
    size: Int = 40,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    emptyContent: @Composable () -> Unit
) {
    SubcomposeAsyncImage(
        modifier = modifier
            .clip(shape)
            .size(size.dp),
        model = ImageRequest.Builder(LocalContext.current)
            .data(packageName)
            .size(size)
            .crossfade(true)
            .build(),
        loading = {
            emptyContent()
        },
        error = {
            emptyContent()
        },
        success = {
            SubcomposeAsyncImageContent(
                modifier = Modifier.fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = PassTheme.colors.loginIconBorder,
                        shape = shape
                    )
                    .background(Color.White)
                    .padding(8.dp)
            )
        },
        contentDescription = null
    )
}

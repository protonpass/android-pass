package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.BoxedIcon
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.pass.domain.ItemType
import proton.pass.domain.WebsiteUrl
import me.proton.core.presentation.R as CoreR

@Composable
fun LoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    itemType: ItemType.Login,
    size: Int = 40,
    canLoadExternalImages: Boolean,
) {
    val sortedPackages = itemType.packageInfoSet.sortedBy { it.packageName.value }
    val packageName = sortedPackages.firstOrNull()?.packageName?.value
    val website = itemType.websites.firstOrNull()
    LoginIcon(
        modifier = modifier,
        text = text,
        website = website,
        packageName = packageName,
        size = size,
        canLoadExternalImages = canLoadExternalImages
    )
}

@Composable
fun LoginIcon(modifier: Modifier = Modifier, shape: Shape = PassTheme.shapes.squircleMediumShape) {
    BoxedIcon(
        modifier = modifier,
        shape = shape,
        backgroundColor = PassTheme.colors.loginInteractionNormMinor2
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_user),
            contentDescription = stringResource(R.string.login_title_icon_content_description),
            tint = PassTheme.colors.loginInteractionNormMajor1
        )
    }
}

@Composable
fun LoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    website: String?,
    packageName: String?,
    size: Int = 40,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    canLoadExternalImages: Boolean,
) {
    if (website == null || !canLoadExternalImages) {
        FallbackLoginIcon(
            modifier = modifier,
            text = text,
            packageName = packageName,
            size = size,
            shape = shape
        )
    } else {
        var isLoaded by remember { mutableStateOf(false) }

        val backgroundColor: Color by animateColorAsState(
            targetValue = if (isLoaded) {
                Color.White
            } else PassTheme.colors.loginInteractionNormMinor2,
            animationSpec = tween(
                durationMillis = CROSSFADE_ANIMATION_MS
            )
        )

        SubcomposeAsyncImage(
            modifier = modifier
                .clip(shape)
                .size(size.dp),
            model = ImageRequest.Builder(LocalContext.current)
                .data(WebsiteUrl(website))
                .size(size)
                .crossfade(CROSSFADE_ANIMATION_MS)
                .build(),
            loading = {
                TwoLetterLoginIcon(
                    text = text,
                    shape = shape
                )
            },
            error = {
                FallbackLoginIcon(
                    text = text,
                    packageName = packageName,
                    size = size,
                    shape = shape
                )
            },
            onSuccess = {
                isLoaded = true
            },
            success = {
                SubcomposeAsyncImageContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = PassTheme.colors.loginIconBorder,
                            shape = shape
                        )
                        .background(backgroundColor)
                        .padding(8.dp)
                )
            },
            contentDescription = null
        )
    }
}

@Composable
private fun FallbackLoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    packageName: String?,
    size: Int = 40,
    shape: Shape
) {
    if (packageName == null) {
        TwoLetterLoginIcon(
            modifier = modifier,
            text = text,
            size = size,
            shape = shape
        )
    } else {
        LinkedAppIcon(
            packageName = packageName,
            size = size,
            shape = shape,
            emptyContent = {
                TwoLetterLoginIcon(
                    modifier = modifier,
                    text = text,
                    size = size,
                    shape = shape
                )
            }
        )
    }
}

@Composable
private fun TwoLetterLoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    size: Int = 40,
    shape: Shape
) {
    CircleTextIcon(
        modifier = modifier,
        text = text,
        backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
        textColor = PassTheme.colors.loginInteractionNormMajor2,
        size = size,
        shape = shape
    )
}

@Preview
@Composable
fun LoginIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginIcon(
                text = "login text",
                website = null,
                packageName = null,
                canLoadExternalImages = false
            )
        }
    }
}

private const val CROSSFADE_ANIMATION_MS = 150

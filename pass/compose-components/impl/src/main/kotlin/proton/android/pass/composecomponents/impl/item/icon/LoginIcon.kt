package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.squircle
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.composecomponents.impl.container.Squircle
import proton.android.pass.composecomponents.impl.item.placeholder
import proton.pass.domain.ItemType
import proton.pass.domain.WebsiteUrl
import me.proton.core.presentation.R as CoreR

@Composable
fun LoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    itemType: ItemType.Login,
    size: Int = 40
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
    )
}

@Composable
fun LoginIcon(modifier: Modifier = Modifier) {
    Squircle(modifier = modifier, backgroundColor = PassTheme.colors.loginInteractionNormMajor1) {
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
) {
    if (website == null) {
        FallbackLoginIcon(
            modifier = modifier,
            text = text,
            packageName = packageName,
            size = size
        )
    } else {
        SubcomposeAsyncImage(
            modifier = modifier
                .squircle()
                .size(size.dp),
            model = WebsiteUrl(website),
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
                    SubcomposeAsyncImageContent(
                        modifier = Modifier.fillMaxSize()
                            .border(
                                width = 3.dp,
                                color = PassTheme.colors.inputBorder,
                                shape = PassTheme.shapes.squircleShape
                            )
                            .background(Color.White)
                            .padding(8.dp)
                    )
                }
                else -> {
                    FallbackLoginIcon(
                        text = text,
                        packageName = packageName,
                        size = size
                    )
                }
            }
        }
    }
}

@Composable
private fun FallbackLoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    packageName: String?,
    size: Int = 40
) {
    if (packageName == null) {
        TwoLetterLoginIcon(
            modifier = modifier,
            text = text,
            size = size,
        )
    } else {
        LinkedAppIcon(
            packageName = packageName,
            size = size,
            emptyContent = {
                TwoLetterLoginIcon(
                    modifier = modifier,
                    text = text,
                    size = size,
                )
            }
        )
    }
}

@Composable
private fun TwoLetterLoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    size: Int = 40
) {
    CircleTextIcon(
        modifier = modifier,
        text = text,
        color = PassTheme.colors.loginInteractionNormMajor1,
        size = size
    )
}

@Preview
@Composable
fun LoginIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginIcon(text = "login text", website = null, packageName = null)
        }
    }
}

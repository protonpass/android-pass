package me.proton.pass.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmall
import me.proton.core.compose.theme.defaultStrong
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R

@OptIn(ExperimentalLifecycleComposeApi::class)
@Suppress("MagicNumber")
@Composable
fun AutofillCard(
    modifier: Modifier = Modifier,
    viewModel: AutofillCardViewModel = hiltViewModel()
) {

    val shouldShowAutofillCard by viewModel.state.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = shouldShowAutofillCard,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        AutofillCardContent(
            modifier = modifier,
            onClick = viewModel::onClick,
            onDismiss = viewModel::onDismiss
        )
    }
}

@Suppress("MagicNumber")
@Composable
fun AutofillCardContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val largeRadialGradient = object : ShaderBrush() {
        override fun createShader(size: Size): Shader =
            RadialGradientShader(
                colors = listOf(
                    Color(0xFFFFDA00),
                    Color(0xFFFB8F42),
                    Color(0xFFC266A7),
                    Color(0xFF6D4AFF)
                ),
                center = Offset(0f, size.height * 2.6f),
                radius = maxOf(size.height, size.width) / 1.1f,
                colorStops = listOf(0f, 0.43f, 0.68f, 0.88f)
            )
    }

    Card(
        modifier = modifier
            .padding(16.dp),
        elevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .clickable { onClick() }
                .background(largeRadialGradient)
                .fillMaxWidth()
        ) {
            Image(
                modifier = Modifier
                    .matchParentSize(),
                alignment = Alignment.BottomEnd,
                contentScale = FixedScale(0.9f),
                painter = painterResource(id = R.drawable.autofill_keyboard),
                contentDescription = ""
            )
            Column(
                modifier = Modifier
                    .padding(19.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.home_autofill_banner_title),
                    style = ProtonTheme.typography.defaultStrong,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = stringResource(id = R.string.home_autofill_banner_text),
                    style = ProtonTheme.typography.defaultSmall,
                    color = Color.White
                )
            }
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = { onDismiss() }
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross_circle_filled),
                    tint = Color.White,
                    contentDescription = ""
                )
            }
        }
    }
}

@Preview
@Composable
fun AutofillCardContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            AutofillCardContent(onClick = {}, onDismiss = {})
        }
    }
}

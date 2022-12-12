package me.proton.pass.presentation.components.common.item

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.theme.headline
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R

@Composable
fun EmptyList(
    modifier: Modifier = Modifier,
    emptyListMessage: String,
    emptyListTitle: String = stringResource(R.string.empty_list_title),
    @DrawableRes emptyListImage: Int = R.drawable.placeholder_bound_box
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = emptyListImage),
            contentDescription = ""
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = emptyListTitle,
            style = ProtonTheme.typography.headline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = emptyListMessage,
            style = ProtonTheme.typography.defaultWeak
        )
    }
}

@Preview
@Composable
fun EmptListPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            EmptyList(emptyListMessage = "Create a new item")
        }
    }
}

package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.featureitemdetail.impl.R

@Suppress("MagicNumber")
@Composable
fun MoreInfo(
    modifier: Modifier = Modifier,
    shouldShowMoreInfoInitially: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        var showMoreInfo by remember { mutableStateOf(shouldShowMoreInfoInitially) }
        Row(
            modifier = Modifier.clickable { showMoreInfo = !showMoreInfo },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_info_circle),
                contentDescription = stringResource(R.string.more_info_icon),
                tint = ProtonTheme.colors.iconWeak
            )
            MoreInfoText(
                modifier = Modifier.padding(8.dp),
                text = stringResource(R.string.more_info_title)
            )
        }
        AnimatedVisibility(visible = showMoreInfo) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier.weight(0.3f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoreInfoText(text = stringResource(R.string.more_info_autofilled))
                    MoreInfoText(text = stringResource(R.string.more_info_modified))
                    Spacer(modifier = Modifier.height(14.dp))
                    MoreInfoText(text = stringResource(R.string.more_info_created))
                }
                Column(
                    modifier = Modifier.weight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoreInfoText(text = "(Placeholder) Today 11:19")
                    MoreInfoText(text = "(Placeholder) 3 time(s)")
                    MoreInfoText(text = "(Placeholder) Last time, yesterday at 10:10 (-14s)")
                    MoreInfoText(text = "(Placeholder) 6 February at 11:07")
                }
            }
        }
    }
}

@Preview
@Composable
fun MoreInfoPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            MoreInfo(shouldShowMoreInfoInitially = input.second)
        }
    }
}

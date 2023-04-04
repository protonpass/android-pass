package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionStrong
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider

@Composable
fun SortingButton(
    modifier: Modifier = Modifier,
    sortingType: SortingType,
    onSortingOptionsClick: () -> Unit
) {
    val text = when (sortingType) {
        SortingType.MostRecent -> stringResource(id = sortingType.titleId)
        SortingType.TitleAsc -> stringResource(id = sortingType.titleId)
        SortingType.TitleDesc -> stringResource(id = sortingType.titleId)
        SortingType.CreationAsc -> stringResource(id = sortingType.titleId)
        SortingType.CreationDesc -> stringResource(id = sortingType.titleId)
    }
    Button(
        modifier = modifier,
        elevation = null,
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        onClick = onSortingOptionsClick
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.ic_sorting),
            contentDescription = stringResource(R.string.sorting_icon_content_description),
            tint = PassTheme.colors.interactionNormMajor2
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            style = ProtonTheme.typography.captionStrong,
            color = PassTheme.colors.interactionNormMajor2
        )
    }
}

class SortingTypePreviewProvider : PreviewParameterProvider<SortingType> {
    override val values: Sequence<SortingType>
        get() = SortingType.values().asSequence()
}

class ThemeAndSortingTypeProvider :
    ThemePairPreviewProvider<SortingType>(SortingTypePreviewProvider())

@Preview
@Composable
fun SortingButtonPreview(
    @PreviewParameter(ThemeAndSortingTypeProvider::class) input: Pair<Boolean, SortingType>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SortingButton(sortingType = input.second, onSortingOptionsClick = {})
        }
    }
}

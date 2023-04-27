package proton.android.pass.composecomponents.impl.item.header

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
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionStrongNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.featuresearchoptions.api.SearchSortingType

@Composable
fun SortingButton(
    modifier: Modifier = Modifier,
    sortingType: SearchSortingType,
    onSortingOptionsClick: () -> Unit
) {
    val text = when (sortingType) {
        SearchSortingType.MostRecent -> stringResource(id = R.string.sort_by_modification_date)
        SearchSortingType.TitleAsc -> stringResource(id = R.string.sort_by_title_asc)
        SearchSortingType.TitleDesc -> stringResource(id = R.string.sort_by_title_desc)
        SearchSortingType.CreationAsc -> stringResource(id = R.string.sort_by_creation_asc)
        SearchSortingType.CreationDesc -> stringResource(id = R.string.sort_by_creation_desc)
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
            style = ProtonTheme.typography.captionStrongNorm,
            color = PassTheme.colors.interactionNormMajor2,
            fontSize = 14.sp
        )
    }
}

class SortingTypePreviewProvider : PreviewParameterProvider<SearchSortingType> {
    override val values: Sequence<SearchSortingType>
        get() = SearchSortingType.values().asSequence()
}

class ThemeAndSortingTypeProvider :
    ThemePairPreviewProvider<SearchSortingType>(SortingTypePreviewProvider())

@Preview
@Composable
fun SortingButtonPreview(
    @PreviewParameter(ThemeAndSortingTypeProvider::class) input: Pair<Boolean, SearchSortingType>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SortingButton(sortingType = input.second, onSortingOptionsClick = {})
        }
    }
}

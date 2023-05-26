package proton.android.pass.composecomponents.impl.item.header

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
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
    TransparentTextButton(
        modifier = modifier,
        text = text,
        icon = R.drawable.ic_sorting,
        iconContentDescription = stringResource(R.string.sorting_icon_content_description),
        color = PassTheme.colors.interactionNormMajor2,
        onClick = onSortingOptionsClick
    )
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
